package cern.rade.services.japc.subscription;

import jakarta.websocket.RemoteEndpoint;


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
