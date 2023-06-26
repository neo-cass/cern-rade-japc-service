package cern.rade.services.japc.rbac;

import cern.cmw.util.config.Configuration;
import cern.cmw.util.config.ConfigurationBuilder;
import cern.rbac.client.ClientConfiguration;
import cern.rbac.client.authentication.AuthenticationClient;
import cern.rbac.client.authentication.AuthenticationException;
import cern.rbac.common.MasterToken;
import cern.rbac.common.RbaToken;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

public class RBACManager {
    private static Configuration config = ConfigurationBuilder.newInstance().setProperty("rbac.token.lifetime.mins", "1440").build();
    private static ClientConfiguration clientConfig = ClientConfiguration.create(config);
    private  static AuthenticationClient authClient =  AuthenticationClient.create(clientConfig);;
    private static ConcurrentHashMap<Integer, RBACClient>rbacClients = new ConcurrentHashMap<>();

    public static MasterToken getMasterToken(String username, String password) throws AuthenticationException {
        return authClient.loginMasterExplicit(username, password, Duration.ofDays(365L));
    }

    public static RbaToken getRbaToken(MasterToken masterToken) throws AuthenticationException  {
        return authClient.loginMaster(masterToken);
    }
    public static void addClient(RBACClient rbacClient){
        rbacClients.put(rbacClient.hashCode(), rbacClient);
    }
    public static RBACClient getClient(int id){
        return rbacClients.get(id);
    }

    public static void clearRBACClients(){
        rbacClients.clear();
    }

    public static void removeRBACClient(int id){
        rbacClients.remove(id);
    }
}
