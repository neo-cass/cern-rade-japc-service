package cern.rade.services.japc.subscription;

import cern.japc.core.*;
import cern.japc.core.factory.ParameterFactory;
import cern.japc.core.factory.SelectorFactory;
import cern.rade.services.codec.CodecException;
import cern.rade.services.codec.japcjava.JAPCDecoder;
import cern.rade.services.common.containers.java.ContainerException;
import cern.rade.services.common.containers.java.DataContainer;
import cern.rade.services.common.containers.java.PropertyContainer;
import cern.rade.services.japc.ContextProvider;
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
    private DataContainer data;
    private SubscriptionHandle sh;
    private LinkedBlockingQueue<AcquiredParameterValue> valueQueue;
    private boolean executed = false;
    private final Logger logger = LoggerFactory.getLogger(SubscriptionService.class);
    public SubscriptionService(String selectorName, String device, String property, String field){
        this.parameterName = Parameters.buildParameterName(device, property, field);
        this.selectorName = selectorName;
    }

    public SubscriptionService(String selectorName, String device, String property){
        this.parameterName = Parameters.buildParameterName(device, property);
        this.selectorName =selectorName;
    }

    public SubscriptionService(String selectorName, String device, DataContainer data){
        this.selectorName = selectorName;
        this.parameterName = Parameters.buildParameterName(device, data.getName());
        this.data = data;
    }

    public SubscriptionService(String selectorName, String device, String property, String field, RBACClient rbacClient){
        this(selectorName, device, property, field);
        this.rbacClient = rbacClient;
    }

    public SubscriptionService(String selectorName, String device, String property, RBACClient rbacClient){
        this(selectorName, device, property);
        this.rbacClient = rbacClient;
    }

    public SubscriptionService(String selectorName, String device, DataContainer data, RBACClient rbacClient){
        this(selectorName, device, data);
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
//        if(this.rbacClient == null){
//            stopNormalSubscription();
//        }
//        else{
//            stopSecureSubscription();
//        }
    }
//    private void stopSecureSubscription() {
//        MiddleTierRbaTokenHolder.clear();
//        stopNormalSubscription();
//    }
//    private void stopNormalSubscription() {
//        sh.stopMonitoring();
//    }

    @Override
    public void valueReceived(String parameterName, AcquiredParameterValue value) {
        //TODO: lossy queue
        try {
            valueQueue.put(value);
        } catch (InterruptedException e) {
            logger.error("Handler thread interrupted, missed value", e);
        }
    }

    @Override
    public void exceptionOccured(String parameterName, String description, ParameterException exception) {
        logger.error("Exception occurred in subscription service: " + description, exception);
    }

    /**
     * Pushes the latest value from the subscription into the DataContainer
     * @param timeout the amount of time to wait in milliseconds for a value to be available before timing out.
     * @return true if a value was pushed into the DataContainer, false if the timeout was reached
     * @throws CodecException if the value could not be decoded into the DataContainer
     * @throws ContainerException  if the DataContainer is not compatible with the value received from the subscription
     */
    public boolean pushLatestIntoContainer(long timeout) throws CodecException, ContainerException {
        //TODO: consider implementing a copy of the DataContainer to avoid race conditions, is this necessary?
        //Its a bit strange to return the same DataContainer every time with different values
        if(!isExecuted()){throw new RuntimeException("SubscriptionService not initialised");}
        AcquiredParameterValue apv;
        try {
            apv = valueQueue.poll(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            return false;
        }
        if (apv == null){return false;}
        decode(apv);
        return true;
    }

    /**
     * Initialises the subscription, starts monitoring and pushes the latest value into the DataContainer.
     * @throws ParameterException if the parameter could not be created or if the subscription could not be started.
     * @throws ContainerException if the DataContainer is not compatible with the value received from the subscription.
     * @throws CodecException if the value could not be decoded into the DataContainer.
     */
    public void execute() throws ParameterException, ContainerException, CodecException {
        init();
        subscribe();
        pushLatestIntoContainer(1000);
        this.executed = true;
    }

    public DataContainer getLatest() {
        if(!isExecuted()){throw new RuntimeException("SubscriptionService not initialised");}
        return data;
    }

    private void decode(AcquiredParameterValue apv) throws CodecException, ContainerException {
        if(data==null){
            data = JAPCDecoder.decode(apv);
        }
        else{
            JAPCDecoder.decode((PropertyContainer) data, apv);
        }
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
}
