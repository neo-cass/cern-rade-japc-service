package cern.rade.services.japc.subscription;

import cern.japc.core.*;
import cern.japc.core.factory.ParameterFactory;
import cern.japc.core.factory.SelectorFactory;

import cern.rade.services.codec.japcjava.JAPCDecoder;
import cern.rade.services.codec.javatolv.LVDataEncoder;
import cern.rade.services.codec.javatolv.LVTypeEncoder;
import cern.rade.services.common.communication.MetaData;
import cern.rade.services.common.communication.Request;
import cern.rade.services.common.containers.java.DataContainer;
import cern.rade.services.common.containers.java.PropertyContainer;
import cern.rade.services.japc.rbac.RBACClient;
import cern.rade.services.japc.rbac.RBACManager;
import cern.rbac.util.holder.MiddleTierRbaTokenHolder;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class SubscriptionHandler implements ParameterValueListener{
    private int sessionID;
    private String selector;
    private String device;
    private String propertyName;
    private Request request;
    private MetaData metaData;
    private DataContainer<?> property;
    private RBACClient rbacClient;
    private SubscriptionHandle sh;
    private DataContainer data;
    private byte[] type_descriptor;
    private int rbac_id;
    private LinkedBlockingQueue<AcquiredParameterValue> valueQueue;
    public SubscriptionHandler(Request request) {
        this.valueQueue = new LinkedBlockingQueue<>();
        this.sessionID = this.hashCode();
        this.rbac_id = metaData.getRbac_id();
        this.request = request;
        this.metaData=request.getMetaData();
        this.rbacClient = RBACManager.getClient(rbac_id);
        this.device = metaData.getDevice();
        this.selector = metaData.getSelector();
        this.property=request.getData();
        this.propertyName=property.getName();
    }

    public void init() throws ParameterException {
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
    public byte[] getLatest(int timeout) throws InterruptedException, IOException {
        AcquiredParameterValue apv = valueQueue.poll(timeout, TimeUnit.SECONDS);
        data = request.getData();
        if(data==null){
            data = JAPCDecoder.decode(apv);
            type_descriptor = LVTypeEncoder.encodeTypeDescriptor(data);
        }
        else{JAPCDecoder.decode((PropertyContainer) data, apv);}
        return LVDataEncoder.encode(data);
    }
    @Override
    public void exceptionOccured(String parameterName, String description, ParameterException exception) {
        System.out.println("Exception occurred in subscription to parameter: " + parameterName + "\n Exception details: " + description);
    }

    public int getRbac_id(){
        return rbac_id;
    }

}
