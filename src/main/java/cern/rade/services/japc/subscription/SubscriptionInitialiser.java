package cern.rade.services.japc.subscription;

import cern.japc.core.ParameterException;
import cern.rade.services.codec.CodecException;
import cern.rade.services.codec.javatolv.LVTypeEncoder;
import cern.rade.services.codec.lvtojava.LVDataContainerFactory;
import cern.rade.services.codec.lvtojava.LVTypeDecoder;
import cern.rade.services.common.BytesUtil;
import cern.rade.services.common.containers.java.DataContainer;
import cern.rade.services.common.containers.labview.LVTypeDescriptor;
import cern.rade.services.japc.ContextProvider;
import cern.rade.services.japc.rbac.RBACClient;
import cern.rade.services.japc.rbac.RBACManager;
import cern.rbac.client.authentication.AuthenticationException;
import cern.rbac.common.TokenFormatException;
import jakarta.servlet.ServletRequestEvent;
import jakarta.servlet.ServletRequestListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.websocket.HandshakeResponse;
import jakarta.websocket.server.HandshakeRequest;
import jakarta.websocket.server.ServerEndpointConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.server.standard.SpringConfigurator;

import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.List;
import java.util.Map;

public class SubscriptionInitialiser extends SpringConfigurator implements ServletRequestListener {
    private final Logger logger = LoggerFactory.getLogger(SubscriptionInitialiser.class);
    private final RBACManager rbacManager = ContextProvider.getRbacManager();
    @Override
    public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
        request.getHttpSession();
        HttpSession httpSession = (HttpSession) request.getHttpSession();
        if(httpSession==null){
            logger.error("HttpSession is null");
        }
        logger.error("pass");
        sec.getUserProperties().put("httpSession", httpSession);
        super.modifyHandshake(sec, request, response);
        try {
            handleNewSubscription(sec, request, response);
        } catch (TokenFormatException | ParameterException | CodecException |
                 InstantiationException e) {
            rejectClient(response.getHeaders(), e.getMessage());
        }
    }

    private void handleNewSubscription(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) throws TokenFormatException, CodecException, ParameterException, InstantiationException {
        //"{selector}/{device}/{property}/{field}"
        Map<String, List<String>> headers =  request.getHeaders();
        Map<String, List<String>> retHeaders = response.getHeaders();
        String url = getFirstHeaderValue(headers,"Subscription-Url");
        logger.info("remoteAddr: {}", getRemoteAddr(request));
        if(url==null){
            //TODO: maybe throw a new type of exception instead and handle in modifyHandshake
            rejectClient(retHeaders, "Subscription-Url header not provided");
            return;
        }

        String[] names = url.split("/");
        String authStr = getFirstHeaderValue(headers,"Rbac");
        String type_descriptor_str = getFirstHeaderValue(headers,"Type-Descriptor");
        SubscriptionService subscriptionService;
        if (names.length == 4){
            subscriptionService = subscribeDevice_Property_Field(names[0], names[1], names[2], names[3], authStr);
        }
        else if (names.length == 3){
            subscriptionService = subscribeDevice_Property(names[0], names[1], names[2], authStr);
        }
        else if (names.length == 2){
            if (type_descriptor_str==null){
                rejectClient(retHeaders, "Type-Descriptor header not provided");
                return;
            }
            subscriptionService = subscribeDevice_DataContainer(names[0], names[1], getDataContainer(type_descriptor_str), authStr);
        }
        else{
            rejectClient(retHeaders, "Subscription-Url format not recognized");
            return;
        }

        subscriptionService.init();
        subscriptionService.startSubscription();

        sec.getUserProperties().put("subscriptionService", subscriptionService);
        if(authStr!=null){
            setSingleHeaderValue(retHeaders, "Rbac","Cached " + subscriptionService.getRbacClient().getRbac_id());
        }
        if(type_descriptor_str==null){
            setSingleHeaderValue(retHeaders, "Type-Descriptor", getLVType(subscriptionService));
        }
    }

    private String getFirstHeaderValue(Map<String, List<String>> headers, String header) {
        if(headers.get(header)!=null && !headers.get(header).isEmpty()){
            return headers.get(header).get(0);
        }
        else{
            return null;
        }
    }

    private void setSingleHeaderValue(Map<String, List<String>> headers, String header, String value){
        headers.put(header, List.of(value));
    }

    private String getLVType(SubscriptionService subscriptionService) throws CodecException {
        subscriptionService.pushLatestIntoContainer(1);
        DataContainer dataContainer = subscriptionService.getLatest();
        byte[] type_desc = BytesUtil.shortArrToByteArr(LVTypeEncoder.encodeTypeDescriptor(dataContainer));
        return Base64.getEncoder().encodeToString(type_desc);
    }

    private DataContainer getDataContainer(String type_descriptor) throws CodecException {
        byte[] type_desc = Base64.getDecoder().decode(type_descriptor);
        LVTypeDescriptor lvTypeDescriptor = LVTypeDecoder.decode(ByteBuffer.wrap(type_desc));
        return LVDataContainerFactory.createDataContainer(lvTypeDescriptor);
    }

    public SubscriptionService subscribeDevice_Property(String selector, String device, String property, String authStr)
            throws TokenFormatException, AuthenticationException {
        SubscriptionService subscriptionService;
        if (authStr!=null){
            RBACClient rbacClient = rbacManager.createClient(authStr);
            subscriptionService = new SubscriptionService(selector, device, property, rbacClient);
        }
        else{
            subscriptionService = new SubscriptionService(selector, device, property);
        }
        return subscriptionService;

    }

    public SubscriptionService subscribeDevice_Property_Field(String selector, String device, String property, String field, String authStr)
            throws TokenFormatException, AuthenticationException {
        SubscriptionService subscriptionService;
        if (authStr!=null){
            RBACClient rbacClient = rbacManager.createClient(authStr);
            subscriptionService = new SubscriptionService(selector, device, property, field, rbacClient);
        }
        else{
            subscriptionService = new SubscriptionService(selector, device, property, field);
        }
        return subscriptionService;
    }

    public SubscriptionService subscribeDevice_DataContainer(String selector, String device, DataContainer data, String authStr)
            throws TokenFormatException, AuthenticationException {
        SubscriptionService subscriptionService;
        if (authStr!=null){
            RBACClient rbacClient = rbacManager.createClient(authStr);
            subscriptionService = new SubscriptionService(selector, device, data, rbacClient);
        }
        else{
            subscriptionService = new SubscriptionService(selector, device, data);
        }
        return subscriptionService;
    }

    private void rejectClient(Map<String, List<String>> retHeaders, String message){
        retHeaders.clear();
        setSingleHeaderValue(retHeaders, "Error", message);
    }

    @Override
    public void requestDestroyed(ServletRequestEvent sre) {

    }

    @Override
    public void requestInitialized(ServletRequestEvent sre) {
        HttpServletRequest request = (HttpServletRequest) sre.getServletRequest();
        HttpSession session = request.getSession(true); // This ensures a session is created if it doesn't exist
        request.setAttribute("HttpSession", session);
        request.getParameterMap().put("remoteAddr", new String[]{request.getRemoteAddr()});
        session.setAttribute("remoteAddr", request.getRemoteAddr());
        request.getServletContext().setAttribute("remoteAddr", request.getRemoteAddr());
        request.setAttribute("remoteAddr", request.getRemoteAddr());
    }

    private String getRemoteAddr(HandshakeRequest request){
        HttpSession httpSession = (HttpSession) request.getHttpSession();
//        request.getParameterMap().get("remoteAddr").get(0);
        HttpServletRequest httpServletRequest = (HttpServletRequest) httpSession.getServletContext().getAttribute("jakarta.servlet.http.HttpServletRequest");
        return (String) httpServletRequest.getAttribute("remoteAddr");
    }
}
