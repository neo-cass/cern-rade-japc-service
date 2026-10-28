package cern.rade.services.japc.subscription;

import cern.japc.core.ParameterException;
import cern.rade.services.codec.CodecException;
import cern.rade.services.japc.ApiPaths;
import cern.rade.services.japc.ContextProvider;
import cern.rade.services.japc.Fields;
import cern.rade.services.japc.error.ErrorCode;
import cern.rade.services.japc.format.Format;
import cern.rade.services.japc.format.Formats;
import cern.rade.services.japc.rbac.RBACClient;
import cern.rade.services.japc.rbac.RBACManager;
import cern.rbac.client.authentication.AuthenticationException;
import cern.rbac.common.TokenFormatException;
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
            rejectClient(format, request, response, ErrorCode.httpStatus(e), ErrorCode.code(e), ErrorCode.message(e));
            return false;
        }
    }

    private boolean handleNewSubscription(Format format, ServerHttpRequest request, ServerHttpResponse response, Map<String, Object> attributes) throws Exception {
        HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();
        HttpHeaders headers = request.getHeaders();
        HttpHeaders retHeaders = response.getHeaders();
        String path = ApiPaths.stripWebSocketBase(request.getURI().getPath().substring(servletRequest.getContextPath().length()));
        String selector = servletRequest.getParameter("selector");
        String authStr = headers.getFirst("Rbac");

        String property;
        String field = null;
        SubscriptionService subscriptionService;
        if (path != null && FIELD_PATH.matches(path)){
            Map<String, String> vars = FIELD_PATH.match(path);
            property = vars.get("property");
            field = vars.get("field");
            subscriptionService = subscribeDevice_Property_Field(selector, vars.get("device"), property, field, authStr);
        }
        else if (path != null && PROPERTY_PATH.matches(path)){
            Map<String, String> vars = PROPERTY_PATH.match(path);
            property = vars.get("property");
            subscriptionService = subscribeDevice_Property(selector, vars.get("device"), property, authStr);
        }
        else if (path != null && DEVICE_PATH.matches(path) && (property = format.propertyFromHeaders(headers)) != null){
            subscriptionService = subscribeDevice_Property(selector, DEVICE_PATH.match(path).get("device"), property, authStr);
        }
        else{
            rejectClient(format, request, response, HttpStatus.NOT_FOUND, ErrorCode.BAD_REQUEST, "Subscription path not recognized for " + format.subprotocol() + ": " + request.getURI().getPath());
            return false;
        }

        subscriptionService.execute();
        try {
            attributes.put("subscriptionService", subscriptionService);
            attributes.put("format", format);
            byte[] ipAddr = request.getRemoteAddress().getAddress().getAddress();
            attributes.put("ipAddr", ipAddr);
            if(authStr!=null){
                retHeaders.set("Rbac","Cached " + subscriptionService.getRbacClient().getRbac_id());
            }
            List<String> fields = Fields.parse(servletRequest.getParameter("fields"));
            if (fields != null){
                attributes.put("fields", fields);
            }
            format.prepareSubscription(servletRequest, retHeaders, property, field, Fields.select(subscriptionService.getLatest(), fields), attributes);
            return true;
        } catch (Exception e) {
            subscriptionService.stopSubscription();
            throw e;
        }
    }

    protected SubscriptionService subscribeDevice_Property(String selector, String device, String property, String authStr)
            throws TokenFormatException, AuthenticationException {
        SubscriptionService subscriptionService;
        if (authStr!=null){
            RBACClient rbacClient = rbacManager.createClient(authStr);
            subscriptionService = new SubscriptionService(cleanSelector(selector), device, property, rbacClient);
        }
        else{
            subscriptionService = new SubscriptionService(cleanSelector(selector), device, property);
        }
        return subscriptionService;

    }

    protected SubscriptionService subscribeDevice_Property_Field(String selector, String device, String property, String field, String authStr)
            throws TokenFormatException, AuthenticationException {
        SubscriptionService subscriptionService;
        if (authStr!=null){
            RBACClient rbacClient = rbacManager.createClient(authStr);
            subscriptionService = new SubscriptionService(cleanSelector(selector), device, property, field, rbacClient);
        }
        else{
            subscriptionService = new SubscriptionService(cleanSelector(selector), device, property, field);
        }
        return subscriptionService;
    }

    protected String cleanSelector(String selector){
        selector = selector == null ? "" : selector;
        return selector;
    }

    protected void rejectClient(Format format, ServerHttpRequest request, ServerHttpResponse response, HttpStatusCode status, int errorCode, String message) throws CodecException, IOException {
        format.writeError(response, status, errorCode, "GET " + request.getURI().getPath(), message);
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {

    }
}
