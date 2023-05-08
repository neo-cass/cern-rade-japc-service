package cern.rade.services.japc.rbac;

import cern.cmw.util.config.Configuration;
import cern.cmw.util.config.ConfigurationBuilder;
import cern.rbac.client.ClientConfiguration;
import cern.rbac.client.authentication.AuthenticationClient;
import cern.rbac.common.MasterToken;
import cern.rbac.common.RbaToken;
import cern.rbac.common.RbaTokenExpirationListener;
import cern.rbac.util.holder.MiddleTierRbaTokenHolder;

import java.time.Duration;

public class RBACTestClass {

    public RBACTestClass(){
        Configuration config = ConfigurationBuilder.newInstance().setProperty("rbac.token.lifetime.mins", "1440").build();
        ClientConfiguration clientConfig = ClientConfiguration.create(config);
        AuthenticationClient authClient = AuthenticationClient.create(clientConfig);

        RbaToken token;

        MasterToken masterToken = authClient.loginMasterExplicit("","", Duration.ofDays(365));
        token = authClient.loginMaster(masterToken);
        RbaTokenExpirationListener listener = new RbaTokenExpirationListener(token);
    }
}
