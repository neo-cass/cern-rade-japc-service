package cern.rade.services.japc.subscription;

import cern.japc.core.ParameterException;
import cern.rade.services.codec.CodecException;
import cern.rade.services.common.containers.java.ContainerException;
import cern.rade.services.common.containers.java.DataContainer;
import cern.rade.services.japc.ContextProvider;
import cern.rade.services.japc.rbac.RBACClient;
import cern.rade.services.japc.rbac.RBACManager;
import cern.rbac.client.authentication.AuthenticationException;
import cern.rbac.common.TokenFormatException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import java.util.Map;

public abstract class SubscriptionHandshakeInterceptor implements HandshakeInterceptor {
    protected final RBACManager rbacManager = ContextProvider.getRbacManager();
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        boolean valid;
        try{
            valid = handleNewSubscription(request, response, wsHandler, attributes);
        }catch(TokenFormatException | AuthenticationException | CodecException | ParameterException e){
            rejectClient(response, response.getHeaders(), e.getMessage(), 400);
            return false;
        }
        return valid;
    }

    public abstract boolean handleNewSubscription(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws TokenFormatException, AuthenticationException, CodecException, ParameterException, ContainerException;
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

    protected SubscriptionService subscribeDevice_DataContainer(String selector, String device, DataContainer data, String authStr)
            throws TokenFormatException, AuthenticationException {
        SubscriptionService subscriptionService;
        if (authStr!=null){
            RBACClient rbacClient = rbacManager.createClient(authStr);
            subscriptionService = new SubscriptionService(cleanSelector(selector), device, data, rbacClient);
        }
        else{
            subscriptionService = new SubscriptionService(cleanSelector(selector), device, data);
        }
        return subscriptionService;
    }
    protected String cleanSelector(String selector){
        selector = selector.equals("NONE") ? "" : selector;
        return selector;
    }

    protected void rejectClient(ServerHttpResponse response, HttpHeaders retHeaders, String message, int statusCode){
        response.setStatusCode(HttpStatusCode.valueOf(statusCode));
        retHeaders.set("Subscription-Error", message);
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {

    }
}
