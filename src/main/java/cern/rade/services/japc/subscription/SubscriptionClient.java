package cern.rade.services.japc.subscription;

import cern.japc.core.*;
import cern.japc.core.factory.ParameterFactory;
import cern.japc.core.factory.SelectorFactory;
import cern.rade.services.common.types.JavaContainer;
import cern.rade.services.japc.messages.MetaData;
import cern.rade.services.japc.messages.Request;
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
            this.vc = new ValueCallback();
            this.sh = parameter.createSubscription(selectorObj, vc);
        }

    }

    private LoginService rbacAuth() throws AuthenticationException{
            return LoginServiceBuilder.newInstance().autoRefresh(true).loginPolicy(LoginPolicy.EXPLICIT)
                    .userName(username).userPassword(metaData.password).build();
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

    public int getSessionID() {
        return sessionID;
    }
}
