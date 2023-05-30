package cern.rade.services.japc.subscription;

import cern.japc.core.*;
import cern.japc.core.factory.ParameterFactory;
import cern.japc.core.factory.SelectorFactory;

import cern.rade.services.common.communication.MetaData;
import cern.rade.services.common.communication.Request;
import cern.rade.services.common.containers.java.DataContainer;
import cern.rade.services.japc.rbac.RBACClient;
import cern.rade.services.japc.rbac.RBACManager;
import cern.rbac.util.holder.MiddleTierRbaTokenHolder;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class SubscriptionClient implements ParameterValueListener{
    private int sessionID;
    private String selector;
    private String device;
    private String propertyName;
    private Request request;
    private MetaData metaData;
    private DataContainer<?> property;
    private RBACClient rbacClient;
    private SubscriptionHandle sh;
    private LinkedBlockingQueue<AcquiredParameterValue> valueQueue;
    public SubscriptionClient(Request request) {
        this.valueQueue = new LinkedBlockingQueue<>();
        this.sessionID = request.getId();
        this.request = request;
        this.metaData=request.getMetaData();
        this.rbacClient = RBACManager.getClient(metaData.getRbac_id());
        this.device = metaData.getDevice();
        this.selector = metaData.getSelector();
        this.property=request.getData();
        this.propertyName=property.getName();
        SubscriptionManager.addSubClient(sessionID, this);
    }

    private void init() throws ParameterException {
        String name = Parameters.buildParameterName(device, propertyName);
        Parameter parameter = ParameterFactory.newInstance().newParameter(name);
        Selector selectorObj = SelectorFactory.newSelector(selector);
        switch (request.getAuthenticationType()){
            case SECURE -> MiddleTierRbaTokenHolder.set(rbacClient.getApplicationToken());
            case SECURE_BY_LOCATION -> MiddleTierRbaTokenHolder.set(rbacClient.getApplicationToken()); //TODO: implement by location
        }
        this.sh = parameter.createSubscription(selectorObj, this);
        MiddleTierRbaTokenHolder.clear();
    }

    public void startSubscription() throws ParameterException {
        sh.startMonitoring();
    }

    public void stopSubscription() {
        sh.stopMonitoring();
    }

    public int getSessionID() {
        return sessionID;
    }

    @Override
    public void valueReceived(String parameterName, AcquiredParameterValue value) {
        valueQueue.add(value);
    }
    public AcquiredParameterValue getLatest(int timeout) throws InterruptedException {
        AcquiredParameterValue apv;
        apv = valueQueue.poll(timeout, TimeUnit.SECONDS);
        return apv;
    }
    @Override
    public void exceptionOccured(String parameterName, String description, ParameterException exception) {
        System.out.println("Exception occurred in subscription to parameter: " + parameterName + "\n Exception details: " + description);
    }

}
