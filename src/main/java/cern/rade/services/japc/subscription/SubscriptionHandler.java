package cern.rade.services.japc.subscription;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;

import java.io.IOException;

public interface SubscriptionHandler extends WebSocketHandler {
    void ping() throws IOException;
    void closeSession(CloseStatus closeStatus) throws IOException;
    String getId();
}
