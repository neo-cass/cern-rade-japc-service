package cern.rade.services.japc.subscription;

import cern.rade.services.codec.CodecException;
import cern.rade.services.japc.ApiPaths;
import cern.rade.services.japc.Fields;
import cern.rade.services.japc.error.ErrorCode;
import cern.rade.services.japc.error.ErrorLog;
import cern.rade.services.japc.format.Format;
import cern.rade.services.japc.format.Formats;
import cern.rade.services.japc.ratelimit.RateLimiter;
import cern.rade.services.japc.rbac.RbacHeaders;
import cern.rade.services.japc.security.RbacAuthentication;
import cern.rade.services.japc.stats.Client;
import cern.rade.services.japc.stats.Operation;
import cern.rade.services.japc.stats.RequestMetrics;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriTemplate;

import java.io.IOException;
import java.time.Clock;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SubscriptionHandshakeInterceptor implements HandshakeInterceptor {
    private static final UriTemplate FIELD_PATH = new UriTemplate("/{device:[^/]+}/{property:[^/]+}/{field:[^/]+}");
    private static final UriTemplate PROPERTY_PATH = new UriTemplate("/{device:[^/]+}/{property:[^/]+}");
    private static final UriTemplate DEVICE_PATH = new UriTemplate("/{device:[^/]+}");
    private final RequestMetrics requestMetrics;
    private final RateLimiter rateLimiter;
    private final SubscriptionRegistry subscriptions;
    private final Clock clock;
    private final Logger logger = LoggerFactory.getLogger(SubscriptionHandshakeInterceptor.class);

    public SubscriptionHandshakeInterceptor(RequestMetrics requestMetrics, RateLimiter rateLimiter,
                                            SubscriptionRegistry subscriptions, Clock clock) {
        this.requestMetrics = requestMetrics;
        this.rateLimiter = rateLimiter;
        this.subscriptions = subscriptions;
        this.clock = clock;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        long start = System.nanoTime();
        Client client = Client.of(((ServletServerHttpRequest) request).getServletRequest());
        boolean accepted = false;
        try {
            accepted = handshake(request, response, attributes, client);
            return accepted;
        } finally {
            int status = accepted ? HttpStatus.SWITCHING_PROTOCOLS.value() : ((ServletServerHttpResponse) response).getServletResponse().getStatus();
            requestMetrics.record(client, Operation.SUBSCRIBE, request.getURI().getPath(), status, start);
        }
    }

    private boolean handshake(ServerHttpRequest request, ServerHttpResponse response, Map<String, Object> attributes, Client client) throws Exception {
        Format format = Formats.forHandshake(request.getHeaders());
        if (format == null){
            rejectClient(Formats.preferred(), request, response, HttpStatus.BAD_REQUEST, ErrorCode.BAD_REQUEST, "Sec-WebSocket-Protocol must be one of " + Formats.subprotocols(), null);
            return false;
        }
        try{
            rateLimiter.check(client.user(), ((ServletServerHttpResponse) response).getServletResponse());
            rateLimiter.checkSubscriptions(client.user(), subscriptions.subscriptions().stream().filter(s -> s.user().equals(client.user())).count());
            return handleNewSubscription(format, request, response, attributes, client);
        }catch(Exception e){
            rejectClient(format, request, response, ErrorCode.httpStatus(e), ErrorCode.of(e), ErrorCode.message(e), e);
            return false;
        }
    }

    private boolean handleNewSubscription(Format format, ServerHttpRequest request, ServerHttpResponse response, Map<String, Object> attributes, Client client) throws Exception {
        HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();
        HttpHeaders headers = request.getHeaders();
        HttpHeaders retHeaders = response.getHeaders();
        String path = ApiPaths.stripWebSocketBase(request.getURI().getPath().substring(servletRequest.getContextPath().length()));
        String selector = servletRequest.getParameter("selector");

        String headerProperty = path != null && DEVICE_PATH.matches(path) ? format.propertyFromHeaders(headers) : null;
        Map<String, String> vars;
        if (path != null && FIELD_PATH.matches(path)){
            vars = FIELD_PATH.match(path);
        }
        else if (path != null && PROPERTY_PATH.matches(path)){
            vars = PROPERTY_PATH.match(path);
        }
        else if (headerProperty != null){
            vars = new HashMap<>(DEVICE_PATH.match(path));
            vars.put("property", headerProperty);
        }
        else{
            rejectClient(format, request, response, HttpStatus.NOT_FOUND, ErrorCode.BAD_REQUEST, "Subscription path not recognized for " + format.subprotocol() + ": " + request.getURI().getPath(), null);
            return false;
        }
        String property = vars.get("property");
        String field = vars.get("field");

        SubscriptionService subscriptionService = new SubscriptionService(selector, vars.get("device"), property, field, RbacAuthentication.current().orElse(null));
        subscriptionService.execute();
        try {
            attributes.put(SessionAttributes.SUBSCRIPTION_SERVICE, subscriptionService);
            servletRequest.setAttribute(SessionAttributes.SUBSCRIPTION_SERVICE, subscriptionService);
            attributes.put(SessionAttributes.FORMAT, format);
            attributes.put(SessionAttributes.CLIENT, client);
            attributes.put(SessionAttributes.OPENED_AT, clock.instant());
            RbacHeaders.setCached(retHeaders, subscriptionService.getRbacSession());
            List<String> fields = Fields.parse(servletRequest.getParameter("fields"));
            if (fields != null){
                attributes.put(SessionAttributes.FIELDS, fields);
            }
            format.prepareSubscription(servletRequest, retHeaders, property, field, Fields.select(subscriptionService.latest().value(), fields), attributes);
            return true;
        } catch (Exception e) {
            subscriptionService.stopSubscription();
            throw e;
        }
    }

    protected void rejectClient(Format format, ServerHttpRequest request, ServerHttpResponse response, HttpStatusCode status, ErrorCode errorCode, String message, Throwable cause) throws CodecException, IOException {
        String origin = "GET " + request.getURI().getPath();
        ErrorLog.log(logger, "Subscription handshake " + origin, status, message, cause);
        errorCode.count("websocket", status);
        format.writeError(response, status, errorCode, origin, message);
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
        // The JAPC subscription starts in beforeHandshake; if the upgrade then fails, no handler will ever stop it.
        if (exception != null && request instanceof ServletServerHttpRequest servletRequest
                && servletRequest.getServletRequest().getAttribute(SessionAttributes.SUBSCRIPTION_SERVICE) instanceof SubscriptionService subscriptionService) {
            logger.warn("Subscription handshake {} failed after subscribing: {}", request.getURI().getPath(), exception.getMessage());
            subscriptionService.stopSubscription();
        }
    }
}
