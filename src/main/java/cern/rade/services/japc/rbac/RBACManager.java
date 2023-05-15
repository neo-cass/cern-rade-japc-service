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
import java.util.concurrent.ConcurrentHashMap;

public class RBACManager {
    private static Configuration config = ConfigurationBuilder.newInstance().setProperty("rbac.token.lifetime.mins", "1440").build();
    private static ClientConfiguration clientConfig = ClientConfiguration.create(config);
    private  static AuthenticationClient authClient =  AuthenticationClient.create(clientConfig);;
    private static ConcurrentHashMap<Integer, RBACClient>rbacClients = new ConcurrentHashMap<>();

    public  static MasterToken getMasterToken(String username, String password){
        return authClient.loginMasterExplicit(username, password, Duration.ofDays(365L));
    }

    public static RbaToken getRbaToken(MasterToken masterToken){
        return authClient.loginMaster(masterToken);
    }
    public void addClient(RBACClient rbacClient){
        rbacClients.put(rbacClient.hashCode(), rbacClient);
    }
    public RBACClient getClient(int id){
        return rbacClients.get(id);
    }
}
