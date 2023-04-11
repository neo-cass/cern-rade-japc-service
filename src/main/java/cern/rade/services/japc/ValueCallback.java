package cern.rade.services.japc;

import cern.japc.core.AcquiredParameterValue;
import cern.japc.core.ParameterException;
import cern.japc.core.ParameterValueListener;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class ValueCallback implements ParameterValueListener {
    //TODO: add conversion here
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

    public String getLatest(int timeout) throws InterruptedException {
        AcquiredParameterValue apv = valueQueue.poll(timeout, TimeUnit.SECONDS);
        String val;
        if (apv == null) {
            val = "TIMEOUT";
        }
        else{
            val = apv.getValue().castAsMap().getString();
        }
        return val;
    }

    @Override
    public void exceptionOccured(String parameterName, String description, ParameterException exception) {
        System.out.println("Exception occurred in subscription to parameter: " + parameterName + "\n Exception details: " + description);
    }
}
