package cern.rade.services.japc.subscription;

import cern.japc.core.*;
import cern.japc.core.factory.SelectorFactory;
import cern.japc.value.ParameterValue;
import cern.rade.services.japc.parameter.ParameterAddress;
import cern.rade.services.japc.parameter.ParameterProvider;
import cern.rade.services.japc.rbac.RbacSession;
import io.micrometer.core.instrument.Metrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Runs one JAPC subscription and queues its updates for a single client.
 */
public class SubscriptionService implements ParameterValueListener {
    private final ParameterProvider parameters;
    private final String parameterName;
    private final String selectorName;
    private Parameter parameter;
    private Selector selector;
    private final RbacSession rbacSession;
    private volatile Latest latest;
    private SubscriptionHandle sh;
    private LinkedBlockingQueue<Update> valueQueue;
    private final int queueSize;
    private final long firstUpdateTimeout;
    private boolean executed = false;
    private final Logger logger = LoggerFactory.getLogger(SubscriptionService.class);
    public SubscriptionService(ParameterProvider parameters, ParameterAddress address, RbacSession rbacSession,
                               SubscriptionProperties properties){
        this.parameters = parameters;
        this.parameterName = address.parameterName();
        this.selectorName = address.selector();
        this.rbacSession = rbacSession;
        this.queueSize = properties.queueSize();
        this.firstUpdateTimeout = properties.firstUpdateTimeout().toMillis();
    }

    public void init() throws ParameterRuntimeException, ParameterException {
        this.parameter = parameters.newParameter(parameterName);
        this.selector = SelectorFactory.newSelector(selectorName);
        this.sh = parameter.createSubscription(selector, this);
        this.valueQueue = new LinkedBlockingQueue<>(queueSize);
    }

    private void subscribe() throws ParameterException {
        RbacSession.runAs(rbacSession, sh::startMonitoring);
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
                Metrics.counter("japc.subscription.updates.dropped").increment();
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
        try {
            if (!awaitUpdate(firstUpdateTimeout)) {
                throw new ParameterException("No first update from " + parameterName + " within " + firstUpdateTimeout + " ms", new TimeoutException());
            }
        } catch (ParameterException | RuntimeException e) {
            stopSubscription();
            throw e;
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

    public String getParameterName() {
        return parameterName;
    }

    public String getSelectorName() {
        return selectorName;
    }

    public record Latest(ParameterValue value, ValueHeader header) {
    }

    private record Update(AcquiredParameterValue value, ParameterException exception) {
    }
}
