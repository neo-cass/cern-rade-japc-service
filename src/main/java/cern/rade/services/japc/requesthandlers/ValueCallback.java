package cern.rade.services.japc.requesthandlers;

import cern.japc.core.AcquiredParameterValue;
import cern.japc.core.ParameterException;
import cern.japc.core.ParameterValueListener;

import java.util.concurrent.LinkedBlockingQueue;

public class ValueCallback implements ParameterValueListener {
    private LinkedBlockingQueue<AcquiredParameterValue> valueQueue;
    public ValueCallback() {
        this.valueQueue = new LinkedBlockingQueue<>();
    }

    public LinkedBlockingQueue<AcquiredParameterValue> valueQueue() {
        return valueQueue;
    }

    @Override
    public void valueReceived(String parameterName, AcquiredParameterValue value) {
        valueQueue.add(value);
    }

    @Override
    public void exceptionOccured(String parameterName, String description, ParameterException exception) {
        System.out.println("Exception occurred in subscription to parameter: " + parameterName + "\n Exception details: " + description);
    }
}
