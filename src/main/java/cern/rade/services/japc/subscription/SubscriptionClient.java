package cern.rade.services.japc.subscription;

import cern.japc.core.*;
import cern.japc.core.factory.ParameterFactory;
import cern.japc.core.factory.SelectorFactory;
import cern.rade.services.common.communication.client.ClientMetaData;
import cern.rade.services.common.communication.client.Request;
import cern.rade.services.common.containers.java.DataContainer;
import cern.rbac.util.authentication.LoginService;
import cern.rbac.client.authentication.AuthenticationException;
import cern.rbac.common.authentication.LoginPolicy;
import cern.rbac.util.authentication.LoginServiceBuilder;

public class SubscriptionClient {
    private int sessionID;
    private String username;
    private String selector;
    private String device;
    private String propertyName;
    private Request request;
    private ClientMetaData metaData;
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
        //System.setProperty(RbacConfiguration.SYSTEM_PROPERTY_RBAC_ENV, RbacConfiguration.Environment.TEST.name());
        //ClientTierTokenHolder.addRbaTokenChangeListener(System.out::println);
        try (LoginService login = rbacAuth()) {
            login.loginNewUser();
            SubscriptionManager.addSubClient(sessionID, this);
            String name = Parameters.buildParameterName(device, propertyName);
            Parameter parameter;
            parameter = ParameterFactory.newInstance().newParameter(name);
            Selector selectorObj;
            selectorObj = SelectorFactory.newSelector(selector);
            this.vc = new SubscriptionCallback();
            this.sh = parameter.createSubscription(selectorObj, vc);
        }

    }

    private LoginService rbacAuth() throws AuthenticationException{
            return LoginServiceBuilder.newInstance().autoRefresh(true).loginPolicy(LoginPolicy.EXPLICIT)
                    .userName(username).userPassword(metaData.getPassword()).build();
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
