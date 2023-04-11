package cern.rade.services.japc;

import cern.japc.core.*;
import cern.japc.core.factory.ParameterFactory;
import cern.japc.core.factory.SelectorFactory;
import cern.rade.services.common.types.JavaContainer;
import cern.rade.services.japc.messages.MetaData;
import cern.rade.services.japc.messages.Request;
import jakarta.websocket.RemoteEndpoint;
import jakarta.websocket.Session;

public class SubscriptionClient {
    private int sessionID;
    private String username;
    private String selector;
    private String device;
    private String propertyName;
    private Request request;
    private MetaData metaData;
    private JavaContainer<?> property;
    private SubscriptionHandle sh;
    private ValueCallback vc;
    public SubscriptionClient(Request request) throws ParameterException {
        this.sessionID = request.getId();
        this.request = request;
        this.metaData=request.getMetaData();
        this.username=metaData.username;
        this.device = metaData.device;
        this.selector = metaData.selector;
        this.property=request.getData();
        this.propertyName=property.getFieldName();
        init();
    }

    private void init() throws ParameterException{
        SubscriptionManager.addSubClient(sessionID, this);
        SubscriptionManager.removeRequest(sessionID);
        String name = Parameters.buildParameterName(device, propertyName);
        System.out.println(name);
        Parameter parameter;
        parameter = ParameterFactory.newInstance().newParameter(name);
        Selector selectorObj;
        selectorObj = SelectorFactory.newSelector(selector);
        this.sh = parameter.createSubscription(selectorObj, vc);
        this.vc = new ValueCallback();
    }

    public void startSubscription() throws ParameterException {
        sh.startMonitoring();
    }

    public void stopSubscription() {
        sh.stopMonitoring();
    }

    public String getLatest() throws InterruptedException {
        return vc.getLatest(59);
    }
}
