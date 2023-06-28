package cern.rade.services.japc.subscription;

import cern.accsoft.commons.util.value.Pair;
import cern.japc.core.*;
import cern.japc.core.factory.ParameterFactory;
import cern.japc.core.factory.SelectorFactory;

import cern.rade.services.codec.japcjava.JAPCDecoder;
import cern.rade.services.codec.javatolv.LVDataEncoder;
import cern.rade.services.codec.javatolv.LVTypeEncoder;
import cern.rade.services.common.communication.AuthenticationType;
import cern.rade.services.common.communication.MetaData;
import cern.rade.services.common.communication.Request;
import cern.rade.services.common.containers.java.DataContainer;
import cern.rade.services.common.containers.java.PropertyContainer;
import cern.rade.services.japc.rbac.RBACClient;
import cern.rade.services.japc.rbac.RBACClientFactory;
import cern.rbac.client.authentication.AuthenticationException;
import cern.rbac.util.holder.MiddleTierRbaTokenHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class SubscriptionHandler implements ParameterValueListener{
    private int sub_id;
    private String selector;
    private String device;
    private String propertyName;
    private Request request;
    private MetaData metaData;
    private RBACClient rbacClient;
    private SubscriptionHandle sh;
    private DataContainer data;
    private short[] type_descriptor;
    private boolean exceptionOccurred = false;
    private ParameterException exception;
    private LinkedBlockingQueue<AcquiredParameterValue> valueQueue;
    private Logger logger = LoggerFactory.getLogger(SubscriptionHandler.class);
    public SubscriptionHandler(Request request) {
        this.metaData=request.getMetaData();
        this.valueQueue = new LinkedBlockingQueue<>();
        this.sub_id = this.hashCode();
        this.request = request;
        this.device = metaData.getDevice();
        this.selector = metaData.getSelector();
        this.data=request.getData();
        this.propertyName=metaData.getProperty();
    }

    public void init() throws ParameterException {
        if(request.getAuthenticationType()!= AuthenticationType.NONE){initRBAC();}
        String name = Parameters.buildParameterName(device, propertyName);
        Parameter parameter = ParameterFactory.newInstance().newParameter(name);
        Selector selectorObj = SelectorFactory.newSelector(selector);
        this.sh = parameter.createSubscription(selectorObj, this);
    }

    private void initRBAC() {
        try{
            this.rbacClient = (new RBACClientFactory(request)).createClient();
        }
        catch (AuthenticationException e){
            logger.error("Error initialising RBAC", e);
        }
    }

    public void startSubscription() throws ParameterException {
        switch (request.getAuthenticationType()){
            case RBAC_EXPLICIT, RBAC_CACHED -> MiddleTierRbaTokenHolder.set(rbacClient.getApplicationToken());
            case RBAC_LOCATION -> {
                //TODO: implement by location
            }
        }
        sh.startMonitoring();
    }

    public void stopSubscription() {
        sh.stopMonitoring();
    }

    public int getSub_id() {
        return sub_id;
    }

    @Override
    public void valueReceived(String parameterName, AcquiredParameterValue value) {
        valueQueue.add(value);
    }
    public AcquiredParameterValue getLatest(int timeout) throws InterruptedException {
        AcquiredParameterValue apv = valueQueue.poll(timeout, TimeUnit.SECONDS);
        return apv;
    }

    public Pair<short[], byte[]> getLVData(int timeout) throws IOException, InterruptedException {
        AcquiredParameterValue apv = getLatest(timeout);
        data = request.getData();

        if (apv==null){return null;}

        if(data==null){
            data = JAPCDecoder.decode(apv);
            type_descriptor = LVTypeEncoder.encodeTypeDescriptor(data);
        }

        else{JAPCDecoder.decode((PropertyContainer) data, apv);}

        byte[] dataBytes = LVDataEncoder.encode(data);

        return new Pair<>(type_descriptor, dataBytes);
    }

    public String getPYData(int timeout) throws InterruptedException {
        AcquiredParameterValue apv = getLatest(timeout);
        if (apv==null){return null;}
        return apv.getValue().castAsMap().getString();
    }

    @Override
    public void exceptionOccured(String parameterName, String description, ParameterException exception) {
        logger.error("Exception occurred in subscription with description {}", description, exception);
        exceptionOccurred = true;
        this.exception = exception;
        sh.stopMonitoring();
    }
    public int getRbac_id(){
        return rbacClient.getRbac_id();
    }
    public Request getRequest() {
        return request;
    }
    public boolean isMonitoring(){
        return sh.isMonitoring();
    }

    public boolean hasExceptionOccurred() {
        return exceptionOccurred;
    }

    public ParameterException getException(){
        return exception;
    }
}
