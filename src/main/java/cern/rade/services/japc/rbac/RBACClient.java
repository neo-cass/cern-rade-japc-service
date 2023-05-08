package cern.rade.services.japc.rbac;

import cern.cmw.util.config.Configuration;
import cern.cmw.util.config.ConfigurationBuilder;
import cern.rbac.client.ClientConfiguration;
import cern.rbac.client.authentication.AuthenticationClient;
import cern.rbac.common.MasterToken;
import cern.rbac.common.RbaToken;
import cern.rbac.common.RbaTokenExpirationListener;
import cern.rbac.common.TokenExpirationListener;

public class RBACClient implements TokenExpirationListener{
    private RbaTokenExpirationListener rbaTokenExpirationListener;
    private RbaToken applicationToken;
    private AuthenticationClient authClient;
    private MasterToken masterToken;

    public RBACClient(String username, String password){
        Configuration config = ConfigurationBuilder.newInstance().setProperty("rbac.token.lifetime.mins", "1440").build();
        ClientConfiguration clientConfig = ClientConfiguration.create(config);
        this.authClient = AuthenticationClient.create(clientConfig);
        this.masterToken = authClient.loginMasterExplicit(username, password);
        this.rbaTokenExpirationListener = new RbaTokenExpirationListener(applicationToken);
        this.rbaTokenExpirationListener.addExpirationListener(this);
    }
    public  RbaToken login(){
        if(masterToken==null || !masterToken.isValid()){return null;}
        RbaToken token = authClient.loginMaster(masterToken);
        this.rbaTokenExpirationListener.removeExpirationListeners();
        this.rbaTokenExpirationListener = new RbaTokenExpirationListener(token);
        return token;
    }
    @Override
    public void tokenExpired() {
        applicationToken = login();
    }

    public RbaToken getApplicationToken() {
        return applicationToken;
    }
}
