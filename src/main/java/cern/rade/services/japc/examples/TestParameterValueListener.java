package cern.rade.services.japc.examples;

import java.util.concurrent.LinkedBlockingQueue;
import cern.japc.core.AcquiredParameterValue;
import cern.japc.core.ParameterException;
import cern.japc.core.ParameterValueListener;

/**
 * Copyright (c) 2022 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

public class TestParameterValueListener implements ParameterValueListener {
    LinkedBlockingQueue<AcquiredParameterValue> acquiredValsQueue;
    public TestParameterValueListener(LinkedBlockingQueue<AcquiredParameterValue> initialQ) {
        this.acquiredValsQueue=initialQ;
    }

    @Override
    public void valueReceived(String parameterName, AcquiredParameterValue value) {

        acquiredValsQueue.add(value);
    }

    @Override
    public void exceptionOccured(String parameterName, String description, ParameterException exception) {

        
    }
    
}
