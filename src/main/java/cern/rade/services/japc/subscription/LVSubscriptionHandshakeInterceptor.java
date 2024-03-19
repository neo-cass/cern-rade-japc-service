package cern.rade.services.japc.subscription;

import cern.japc.core.ParameterException;
import cern.rade.services.codec.CodecException;
import cern.rade.services.codec.javatolv.LVTypeEncoder;
import cern.rade.services.codec.lvtojava.LVDataContainerFactory;
import cern.rade.services.codec.lvtojava.LVTypeDecoder;
import cern.rade.services.common.BytesUtil;
import cern.rade.services.common.containers.java.ContainerException;
import cern.rade.services.common.containers.java.DataContainer;
import cern.rade.services.common.containers.labview.LVTypeDescriptor;
import cern.rbac.client.authentication.AuthenticationException;
import cern.rbac.common.TokenFormatException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;

import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.Map;

public class LVSubscriptionHandshakeInterceptor extends SubscriptionHandshakeInterceptor{
    public boolean handleNewSubscription(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws TokenFormatException, AuthenticationException, CodecException, ParameterException, ContainerException {
        //"{selector}/{device}/{property}/{field}"
        HttpHeaders headers =  request.getHeaders();
        HttpHeaders retHeaders = response.getHeaders();
        String url = headers.getFirst("Subscription-Url");
        if(url==null){
            //TODO: maybe throw a new type of exception instead and handle in modifyHandshake
            rejectClient(response, retHeaders,"Subscription-Url header not provided",400);
            return false;
        }

        String[] names = url.split("/");
        String authStr = headers.getFirst("Rbac");
        String type_descriptor_str = headers.getFirst("Type-Descriptor");
        SubscriptionService subscriptionService;
        if (names.length == 4){
            subscriptionService = subscribeDevice_Property_Field(names[0], names[1], names[2], names[3], authStr);
        }
        else if (names.length == 3){
            subscriptionService = subscribeDevice_Property(names[0], names[1], names[2], authStr);
        }
        else if (names.length == 2){
            if (type_descriptor_str==null){
                rejectClient(response, retHeaders, "Type-Descriptor header not provided", 400);
                return false;
            }
            subscriptionService = subscribeDevice_DataContainer(names[0], names[1], getDataContainer(type_descriptor_str), authStr);
        }
        else{
            rejectClient(response, retHeaders, "Subscription-Url format not recognized", 400);
            return false;
        }

        subscriptionService.init();
        subscriptionService.startSubscription();

        attributes.put("subscriptionService", subscriptionService);
        byte[] ipAddr = request.getRemoteAddress().getAddress().getAddress();
        attributes.put("ipAddr", ipAddr);
        if(authStr!=null){
            retHeaders.set("Rbac","Cached " + subscriptionService.getRbacClient().getRbac_id());
        }
        if(type_descriptor_str==null){
            retHeaders.set("Type-Descriptor", getLVType(subscriptionService));
        }
        return true;
    }

    private String getLVType(SubscriptionService subscriptionService) throws CodecException, ContainerException {
        //Push the latest value into the container, so we can generate the type descriptor to send back to the client
        subscriptionService.pushLatestIntoContainer(1000);
        DataContainer dataContainer = subscriptionService.getLatest();
        byte[] type_desc = BytesUtil.shortArrToByteArr(LVTypeEncoder.encodeTypeDescriptor(dataContainer));
        return Base64.getUrlEncoder().encodeToString(type_desc);
    }

    private DataContainer getDataContainer(String type_descriptor) throws CodecException {
        byte[] type_desc = Base64.getUrlDecoder().decode(type_descriptor);
        LVTypeDescriptor lvTypeDescriptor = LVTypeDecoder.decode(ByteBuffer.wrap(type_desc));
        return LVDataContainerFactory.createDataContainer(lvTypeDescriptor);
    }
}
