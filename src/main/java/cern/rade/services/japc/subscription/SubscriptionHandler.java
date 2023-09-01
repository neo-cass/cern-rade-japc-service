package cern.rade.services.japc.subscription;

import cern.japc.core.*;

import cern.rade.services.codec.CodecException;
import cern.rade.services.codec.japcjava.JAPCDecoder;
import cern.rade.services.codec.javatolv.LVDataEncoder;
import cern.rade.services.codec.javatolv.LVTypeEncoder;
import cern.rade.services.common.communication.AuthenticationType;
import cern.rade.services.common.containers.java.DataContainer;
import cern.rade.services.common.containers.java.PropertyContainer;
import cern.rade.services.japc.Client;
import cern.rade.services.japc.rbac.RBACClient;
import cern.rbac.client.authentication.AuthenticationException;
import cern.rbac.util.holder.MiddleTierRbaTokenHolder;
import jakarta.websocket.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class SubscriptionHandler implements ParameterValueListener{
    private final Parameter parameter;
    private final Selector selector;
    private final int sub_id;
    private final Client client;
    private final RBACClient rbacClient;
    private SubscriptionHandle sh;
    private DataContainer data;
    private short[] type_descriptor;
    //TODO set max value, check we wont have memopry issues
    private LinkedBlockingQueue<AcquiredParameterValue> valueQueue;
    private static final Logger logger = LoggerFactory.getLogger(SubscriptionHandler.class);
    public SubscriptionHandler(Client client) {
        this.valueQueue = new LinkedBlockingQueue<>();
        this.sub_id = this.hashCode();
        this.client = client;
        this.type_descriptor=client.getType_descriptor();
        this.rbacClient = client.getRbacClient();
        this.parameter = client.getParameter();
        this.data=client.getDataContainer();
        this.selector = client.getSelector();
    }

    public void init() throws ParameterRuntimeException, ParameterException, AuthenticationException {
        this.sh = parameter.createSubscription(selector, this);
        startSubscription();
    }

    public void startSubscription() throws ParameterException, AuthenticationException {
        if(client.getAuthenticationType()==AuthenticationType.NONE){
            startNormalSubscription();
        }
        else{
            startSecureSubscription();
        }
    }

    public void startNormalSubscription() throws ParameterException{
        sh.startMonitoring();
    }

    public void startSecureSubscription() throws ParameterException, AuthenticationException{
        MiddleTierRbaTokenHolder.set(rbacClient.getApplicationToken());
        startNormalSubscription();
    }

    public void stopSubscription() {
        if(client.getAuthenticationType()==AuthenticationType.NONE){
            stopNormalSubscription();
        }
        else{
            stopSecureSubscription();
        }
    }
    public void stopSecureSubscription() {
        MiddleTierRbaTokenHolder.clear();
        stopNormalSubscription();
    }
    public void stopNormalSubscription() {
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
        return valueQueue.poll(timeout, TimeUnit.SECONDS);
    }

    public byte[] getLVData(int timeout) throws InterruptedException, CodecException {
        AcquiredParameterValue apv = getLatest(timeout);
        if (apv==null){return null;}
        if(data==null){
            data = JAPCDecoder.decode(apv);
            type_descriptor = LVTypeEncoder.encodeTypeDescriptor(data);
        }
        JAPCDecoder.decode((PropertyContainer) data, apv);
        return LVDataEncoder.encode(data);
    }

    public String getPYData(int timeout) throws InterruptedException {
        AcquiredParameterValue apv = getLatest(timeout);
        if (apv==null){return null;}
        return apv.getValue().castAsMap().getString();
    }

    @Override
    public void exceptionOccured(String parameterName, String description, ParameterException exception) {
        logger.error("Exception occurred in subscription with description {}", description, exception);
        sh.stopMonitoring();
    }

    public Client getClient() {
        return client;
    }
    public boolean isMonitoring(){
        return sh.isMonitoring();
    }
    public short[] getType_descriptor() {
        return type_descriptor;
    }
}
