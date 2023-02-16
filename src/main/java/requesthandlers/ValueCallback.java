package requesthandlers;

import cern.japc.core.AcquiredParameterValue;
import cern.japc.core.ParameterException;
import cern.japc.core.ParameterValueListener;

import java.util.concurrent.LinkedBlockingQueue;

public class ValueCallback implements ParameterValueListener {
    private LinkedBlockingQueue<AcquiredParameterValue> acquiredValsQueue;
    public ValueCallback() {
        this.acquiredValsQueue = new LinkedBlockingQueue<>();
    }

    @Override
    public void valueReceived(String parameterName, AcquiredParameterValue value) {
        acquiredValsQueue.add(value);
    }

    @Override
    public void exceptionOccured(String parameterName, String description, ParameterException exception) {
        System.out.println("Exception occurred in subscription to parameter: " + parameterName + "\n Exception details: " + description);
    }
}
