package cern.rade.services.japc.subscription;

import cern.rade.services.codec.CodecException;
import cern.rade.services.codec.javatolv.LVDataEncoder;
import cern.rade.services.common.containers.java.ContainerException;
import cern.rade.services.common.containers.java.DataContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.*;

import java.io.IOException;


public class LVSubscriptionWebSocketHandler extends SubscriptionWebSocketHandler {
    private final Logger logger = LoggerFactory.getLogger(LVSubscriptionWebSocketHandler.class);
    boolean sendData() throws IOException, CodecException, ContainerException {
        //TODO what about the value we miss for generating the type_desc?
        this.newValue = subscriptionService.pushLatestIntoContainer(waitTimeForNewValueFromSub);
        if(!newValue){
            return false;
        }
        DataContainer data = subscriptionService.getLatest();
        byte[] bytes = LVDataEncoder.encode(data);
        session.sendMessage(new BinaryMessage(bytes));
        return this.newValue;
    }
}
