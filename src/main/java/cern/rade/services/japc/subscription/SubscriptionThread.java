package cern.rade.services.japc.subscription;

import cern.japc.core.AcquiredParameterValue;
import cern.japc.core.ParameterException;
import cern.japc.core.ParameterValueListener;
import cern.japc.core.SubscriptionHandle;
import cern.rade.services.japc.examples.SubscriptionCallback;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class SubscriptionThread implements Runnable, ParameterValueListener {
    private SubscriptionHandler client;

    public SubscriptionThread(Socket client, SubscriptionHandle sh, SubscriptionCallback vc){

    }

    @Override
    public void run() {

    }

    private void pushData() throws IOException {

    }

    @Override
    public void valueReceived(String parameterName, AcquiredParameterValue value) {

    }

    @Override
    public void exceptionOccured(String parameterName, String description, ParameterException exception) {

    }
}
