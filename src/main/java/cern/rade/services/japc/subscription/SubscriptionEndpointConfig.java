package cern.rade.services.japc.subscription;

import cern.rade.services.common.communication.MetaData;
import jakarta.websocket.HandshakeResponse;
import jakarta.websocket.server.HandshakeRequest;
import jakarta.websocket.server.ServerEndpointConfig;
import jakarta.websocket.server.ServerEndpointConfig.Configurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;

public class SubscriptionEndpointConfig extends Configurator {
    Logger logger = LoggerFactory.getLogger(SubscriptionEndpointConfig.class);
    @Override
    public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {

        ArrayList<String> arr = new ArrayList<>();
        arr.add("value");
        request.getHeaders().forEach((h, v) -> logger.error(h + ": " + v));
        super.modifyHandshake(sec, request, response);
        response.getHeaders().put("header", arr);
        response.getHeaders().forEach((h, v) -> logger.error(h + ": " + v));

        Client client = RequestHandler.decodeRequest(request);
        if(!client.isValid()){
            rejectClient(client, response);
            return;
        }

        logger.error("Client: {}", client);
        SubscriptionInitialiser initialiser = new SubscriptionInitialiser(response.getHeaders(), client);
        initialiser.execute();
        try {
            sec.getConfigurator().getEndpointInstance(SubscriptionWebsocket.class).setSubHandler(initialiser.getHandler());
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        }
        return;
    }

    private void rejectClient(Client client, HandshakeResponse response) {
        MetaData metaData = new MetaData();
        metaData.setError(client.getInvalidationReason());
        response.getHeaders().put("MetaData", new ArrayList<String>(){{add(metaData.flatten());}});
    }


}
