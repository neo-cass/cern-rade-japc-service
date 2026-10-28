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
import java.util.concurrent.TimeoutException;

public class SubscriptionService implements ParameterValueListener {
    private String parameterName;
    private String selectorName;
    private Parameter parameter;
    private Selector selector;
    private RBACClient rbacClient;
    private volatile Latest latest;
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
        enqueue(new Update(value, null));
    }

    @Override
    public void exceptionOccured(String parameterName, String description, ParameterException exception) {
        logger.debug("JAPC subscription to {} reported {}: {}", parameterName, description, exception.getMessage());
        enqueue(new Update(null, exception));
    }

    private void enqueue(Update update) {
        while (!valueQueue.offer(update)) {
            if (valueQueue.poll() != null) {
                logger.debug("JAPC subscription to {}: queue full, dropped oldest update", parameterName);
            }
        }
    }

    /**
     * Waits for the next update from the subscription and makes it the latest value.
     * @param timeout the amount of time to wait in milliseconds for an update before timing out.
     * @return true if a new value is available, false if the timeout was reached
     * @throws ParameterException if the subscription reported an exception instead of a value
     */
    public boolean awaitUpdate(long timeout) throws ParameterException {
        if(!isMonitoring()){throw new RuntimeException("SubscriptionService not initialised");}
        Update update;
        try {
            update = valueQueue.poll(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            return false;
        }
        if (update == null){return false;}
        if (update.exception() != null){throw update.exception();}
        latest = new Latest(update.value().getValue(), update.value().getHeader());
        return true;
    }

    /**
     * Initialises the subscription, starts monitoring and waits for the first update.
     * @throws ParameterException if the subscription could not be started, reported an exception, or sent no first update in time.
     */
    public void execute() throws ParameterException {
        init();
        subscribe();
        long timeout = ContextProvider.getServiceProperties().getFirstUpdateTimeout();
        if (!awaitUpdate(timeout)) {
            throw new ParameterException("No first update from " + parameterName + " within " + timeout + " ms", new TimeoutException());
        }
        this.executed = true;
    }

    public Latest latest() {
        if(!isExecuted()){throw new RuntimeException("SubscriptionService not initialised");}
        return latest;
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

    public record Latest(ParameterValue value, ValueHeader header) {
    }

    private record Update(AcquiredParameterValue value, ParameterException exception) {
    }
}
