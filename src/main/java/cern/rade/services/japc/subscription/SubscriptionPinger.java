package cern.rade.services.japc.subscription;

import cern.japc.core.AcquiredParameterValue;
import cern.japc.core.ParameterException;
import cern.japc.core.ParameterValueListener;
import cern.japc.core.SubscriptionHandle;
import cern.rade.services.japc.examples.SubscriptionCallback;
import jakarta.websocket.Session;

import java.io.IOException;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SubscriptionPinger implements Runnable {
    private ConcurrentHashMap<Integer, SubscriptionWebsocket> sockets;

    public SubscriptionPinger(ConcurrentHashMap<Integer, SubscriptionWebsocket> sockets){
        this.sockets=sockets;
    }

    @Override
    public void run() {
        sockets.forEach((id, socket) -> socket.ping());

        for(Map.Entry<Integer, SubscriptionWebsocket> entry : sockets.entrySet()){
            if(entry.getValue().hasTimedOut()){
                SubscriptionManager.closeSlowSubSocket(entry.getKey());
            }
        }

        //sockets.forEach((id, socket) -> socket.getLastPongReceived().get());
    }

}
