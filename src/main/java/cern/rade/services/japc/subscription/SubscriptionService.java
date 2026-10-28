package cern.rade.services.japc.subscription;

import cern.japc.core.*;
import cern.japc.core.factory.ParameterFactory;
import cern.japc.core.factory.SelectorFactory;
import cern.japc.value.ParameterValue;
import cern.rade.services.japc.ContextProvider;
import cern.rade.services.japc.ParameterNames;
import cern.rade.services.japc.rbac.RBACClient;
import cern.rbac.client.authentication.AuthenticationException;
import cern.rbac.util.holder.MiddleTierRbaTokenHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class SubscriptionService implements ParameterValueListener {
    private String parameterName;
    private String selectorName;
    private Parameter parameter;
    private Selector selector;
    private RBACClient rbacClient;
    private ParameterValue data;
    private ValueHeader header;
    private SubscriptionHandle sh;
    private LinkedBlockingQueue<Update> valueQueue;
    private boolean executed = false;
    private final Logger logger = LoggerFactory.getLogger(SubscriptionService.class);
    public SubscriptionService(String selectorName, String device, String property, String field, RBACClient rbacClient){
        this.parameterName = ParameterNames.parameter(device, property, field);
        this.selectorName = ParameterNames.selector(selectorName);
        this.rbacClient = rbacClient;
    }

    public void init() throws ParameterRuntimeException, ParameterException, AuthenticationException {
        this.parameter = ParameterFactory.newInstance().newParameter(parameterName);
        this.selector = SelectorFactory.newSelector(selectorName);
        this.sh = parameter.createSubscription(selector, this);
        this.valueQueue = new LinkedBlockingQueue<>(ContextProvider.getServiceProperties().getSubQueueSize());
    }

    private void subscribe() throws ParameterException, AuthenticationException {
        if(rbacClient == null){
            startNormalSubscription();
        }
        else{
            startSecureSubscription();
        }
    }

    private void startNormalSubscription() throws ParameterException{
        sh.startMonitoring();
    }

    private void startSecureSubscription() throws ParameterException, AuthenticationException{
        MiddleTierRbaTokenHolder.set(rbacClient.getApplicationToken());
        startNormalSubscription();
        MiddleTierRbaTokenHolder.clear();
    }

    public void stopSubscription() {
        sh.stopMonitoring();
    }

    @Override
    public void valueReceived(String parameterName, AcquiredParameterValue value) {
        //TODO: lossy queue
        try {
            valueQueue.put(new Update(value, null));
        } catch (InterruptedException e) {
            logger.error("Handler thread interrupted, missed value.", e);
        }
    }

    @Override
    public void exceptionOccured(String parameterName, String description, ParameterException exception) {
        logger.error("ParameterException occurred in the JAPC subscription: " + description, exception);
        try {
            valueQueue.put(new Update(null, exception));
        } catch (InterruptedException e) {
            logger.error("Handler thread interrupted, missed exception.", e);
        }
    }

    /**
     * Pushes the latest value from the subscription into the DataContainer
     * @param timeout the amount of time to wait in milliseconds for a value to be available before timing out.
     * @return true if a value was pushed into the DataContainer, false if the timeout was reached
     * @throws ParameterException if the subscription reported an exception instead of a value
     */
    public boolean pushLatestIntoContainer(long timeout) throws ParameterException {
        //TODO: consider implementing a copy of the DataContainer to avoid race conditions, is this necessary?
        if(!isMonitoring()){throw new RuntimeException("SubscriptionService not initialised");}
        Update update;
        try {
            update = valueQueue.poll(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            return false;
        }
        if (update == null){return false;}
        if (update.exception() != null){throw update.exception();}
        decode(update.value());
        return true;
    }

    /**
     * Initialises the subscription, starts monitoring and pushes the latest value into the DataContainer.
     * @throws ParameterException if the parameter could not be created or if the subscription could not be started.
     */
    public void execute() throws ParameterException {
        init();
        subscribe();
        pushLatestIntoContainer(1000);
        this.executed = true;
    }

    public ParameterValue getLatest() {
        if(!isExecuted()){throw new RuntimeException("SubscriptionService not initialised");}
        return data;
    }

    public ValueHeader getLatestHeader() {
        if(!isExecuted()){throw new RuntimeException("SubscriptionService not initialised");}
        return header;
    }

    private void decode(AcquiredParameterValue apv) {
        data = apv.getValue();
        header = apv.getHeader();
    }

    public boolean isExecuted() {
        return executed;
    }

    public boolean isMonitoring() {
        return sh.isMonitoring();
    }

    public RBACClient getRbacClient() {
        return rbacClient;
    }

    private record Update(AcquiredParameterValue value, ParameterException exception) {
    }
}
