package cern.rade.services.japc.subscription;

import cern.rade.services.codec.CodecException;
import cern.rade.services.japc.ApiPaths;
import cern.rade.services.japc.ContextProvider;
import cern.rade.services.japc.Fields;
import cern.rade.services.japc.error.ErrorCode;
import cern.rade.services.japc.format.Format;
import cern.rade.services.japc.format.Formats;
import cern.rade.services.japc.rbac.RBACHeaders;
import cern.rade.services.japc.rbac.RBACManager;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriTemplate;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SubscriptionHandshakeInterceptor implements HandshakeInterceptor {
    private static final UriTemplate FIELD_PATH = new UriTemplate("/{device:[^/]+}/{property:[^/]+}/{field:[^/]+}");
    private static final UriTemplate PROPERTY_PATH = new UriTemplate("/{device:[^/]+}/{property:[^/]+}");
    private static final UriTemplate DEVICE_PATH = new UriTemplate("/{device:[^/]+}");
    protected final RBACManager rbacManager = ContextProvider.getRbacManager();
    private final Logger logger = LoggerFactory.getLogger(SubscriptionHandshakeInterceptor.class);

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        Format format = Formats.forHandshake(request.getHeaders());
        if (format == null){
            rejectClient(Formats.preferred(), request, response, HttpStatus.BAD_REQUEST, ErrorCode.BAD_REQUEST, "Sec-WebSocket-Protocol must be one of " + Formats.subprotocols());
            return false;
        }
        try{
            return handleNewSubscription(format, request, response, attributes);
        }catch(Exception e){
            logger.error("Error handling subscription handshake for {}", request.getURI().getPath(), e);
            rejectClient(format, request, response, ErrorCode.httpStatus(e), ErrorCode.of(e), ErrorCode.message(e));
            return false;
        }
    }

    private boolean handleNewSubscription(Format format, ServerHttpRequest request, ServerHttpResponse response, Map<String, Object> attributes) throws Exception {
        HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();
        HttpHeaders headers = request.getHeaders();
        HttpHeaders retHeaders = response.getHeaders();
        String path = ApiPaths.stripWebSocketBase(request.getURI().getPath().substring(servletRequest.getContextPath().length()));
        String selector = servletRequest.getParameter("selector");
        String authStr = headers.getFirst(RBACHeaders.HEADER);

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
            rejectClient(format, request, response, HttpStatus.NOT_FOUND, ErrorCode.BAD_REQUEST, "Subscription path not recognized for " + format.subprotocol() + ": " + request.getURI().getPath());
            return false;
        }
        String property = vars.get("property");
        String field = vars.get("field");

        SubscriptionService subscriptionService = new SubscriptionService(selector, vars.get("device"), property, field, rbacManager.optionalClient(authStr));
        subscriptionService.execute();
        try {
            attributes.put(SessionAttributes.SUBSCRIPTION_SERVICE, subscriptionService);
            attributes.put(SessionAttributes.FORMAT, format);
            RBACHeaders.setCached(retHeaders, subscriptionService.getRbacClient());
            List<String> fields = Fields.parse(servletRequest.getParameter("fields"));
            if (fields != null){
                attributes.put(SessionAttributes.FIELDS, fields);
            }
            format.prepareSubscription(servletRequest, retHeaders, property, field, Fields.select(subscriptionService.getLatest(), fields), attributes);
            return true;
        } catch (Exception e) {
            subscriptionService.stopSubscription();
            throw e;
        }
    }

    protected void rejectClient(Format format, ServerHttpRequest request, ServerHttpResponse response, HttpStatusCode status, ErrorCode errorCode, String message) throws CodecException, IOException {
        format.writeError(response, status, errorCode, "GET " + request.getURI().getPath(), message);
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {

    }
}
