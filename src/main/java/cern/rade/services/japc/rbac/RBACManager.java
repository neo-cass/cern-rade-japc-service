package cern.rade.services.japc.rbac;

import cern.cmw.util.config.Configuration;
import cern.cmw.util.config.ConfigurationBuilder;
import cern.rbac.client.ClientConfiguration;
import cern.rbac.client.authentication.AuthenticationClient;
import cern.rbac.common.MasterToken;
import cern.rbac.common.RbaToken;
import cern.rbac.common.RbaTokenExpirationListener;
import cern.rbac.common.TokenExpirationListener;

import java.time.Duration;
import java.util.ArrayList;

public class RBACManager {
    private  AuthenticationClient authClient;
    public RBACManager(){
        Configuration config = ConfigurationBuilder.newInstance().setProperty("rbac.token.lifetime.mins", "1440").build();
        ClientConfiguration clientConfig = ClientConfiguration.create(config);
        this.authClient = AuthenticationClient.create(clientConfig);
    }
    public  MasterToken getMasterToken(String username, String password){
        return authClient.loginMasterExplicit(username, password, Duration.ofDays(365L));
    }

    public RbaToken getRbaToken(MasterToken masterToken){
        return authClient.loginMaster(masterToken);
    }
}
