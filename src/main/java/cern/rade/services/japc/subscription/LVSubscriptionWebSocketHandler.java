package cern.rade.services.japc.subscription;

import cern.rade.services.codec.CodecException;
import cern.rade.services.codec.javatolv.LVDataEncoder;
import cern.rade.services.common.containers.java.DataContainer;
import cern.rade.services.japc.ContextProvider;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class LVSubscriptionWebSocketHandler extends SubscriptionWebSocketHandler {
    private final Logger logger = LoggerFactory.getLogger(LVSubscriptionWebSocketHandler.class);

    boolean sendData() throws IOException, CodecException {
        DataContainer data = subscriptionService.getLatest();
        byte[] bytes = LVDataEncoder.encode(data);
        session.sendMessage(new BinaryMessage(bytes));
        boolean timeout = subscriptionService.pushLatestIntoContainer(waitTimeForNewValueFromSub);
        return timeout;
    }
}
