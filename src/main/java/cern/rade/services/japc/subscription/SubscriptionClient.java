package cern.rade.services.japc.subscription;

import cern.japc.core.*;
import cern.japc.core.factory.ParameterFactory;
import cern.japc.core.factory.SelectorFactory;
import cern.rade.services.common.communication.client.ClientMetaData;
import cern.rade.services.common.communication.client.MetaData;
import cern.rade.services.common.communication.client.Request;
import cern.rade.services.common.containers.java.DataContainer;
public class SubscriptionClient {
    private int sessionID;
    private String username;
    private String selector;
    private String device;
    private String propertyName;
    private Request request;
    private MetaData metaData;
    private DataContainer<?> property;
    private SubscriptionHandle sh;
    private SubscriptionCallback vc;
    public SubscriptionClient(Request request) throws ParameterException {
        this.sessionID = request.getId();
        this.request = request;
        this.metaData=request.getMetaData();
        this.username=metaData.getUsername();
        this.device = metaData.getDevice();
        this.selector = metaData.getSelector();
        this.property=request.getData();
        this.propertyName=property.getName();
        init();
    }

    private void init() throws ParameterException{
            SubscriptionManager.addSubClient(sessionID, this);
            String name = Parameters.buildParameterName(device, propertyName);
            Parameter parameter;
            parameter = ParameterFactory.newInstance().newParameter(name);
            Selector selectorObj;
            selectorObj = SelectorFactory.newSelector(selector);
            this.vc = new SubscriptionCallback();
            this.sh = parameter.createSubscription(selectorObj, vc);
    }

    public void startSubscription() throws ParameterException {
        sh.startMonitoring();
    }

    public void stopSubscription() {
        sh.stopMonitoring();
    }

    public String getLatest() throws InterruptedException {
        return vc.getLatest(50);
    }

    public int getSessionID() {
        return sessionID;
    }
}
