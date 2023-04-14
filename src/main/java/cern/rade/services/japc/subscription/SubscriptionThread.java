package cern.rade.services.japc.subscription;

import cern.japc.core.ParameterException;
import cern.japc.core.SubscriptionHandle;
import cern.rade.services.japc.subscription.ValueCallback;
import jakarta.websocket.RemoteEndpoint;

import java.io.IOException;


public class SubscriptionThread extends Thread{
    private SubscriptionClient client;
    private RemoteEndpoint.Basic writer;
    public SubscriptionThread(SubscriptionClient client, RemoteEndpoint.Basic writer){
        this.client = client;
        this.writer=writer;
        this.setName("Subscription-Thread-" + client.getSessionID());
    }

    @Override
    public void run() {

    }

}
