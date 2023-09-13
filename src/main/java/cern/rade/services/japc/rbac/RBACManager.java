package cern.rade.services.japc.rbac;

import cern.cmw.util.config.Configuration;
import cern.cmw.util.config.ConfigurationBuilder;
import cern.rbac.client.ClientConfiguration;
import cern.rbac.client.authentication.AuthenticationClient;
import cern.rbac.client.authentication.AuthenticationException;
import cern.rbac.common.MasterToken;
import cern.rbac.common.RbaToken;
import cern.rbac.common.TokenFormatException;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

public class RBACManager {
    private Configuration config = ConfigurationBuilder.newInstance().setProperty("rbac.token.lifetime.mins", "1440").build();
    private ClientConfiguration clientConfig = ClientConfiguration.create(config);
    private AuthenticationClient authClient =  AuthenticationClient.create(clientConfig);;
    private ConcurrentHashMap<Integer, RBACClient>rbacClients = new ConcurrentHashMap<>();

    public MasterToken getMasterToken(String username, String password) throws AuthenticationException {
        return authClient.loginMasterExplicit(username, password, Duration.ofDays(365L));
    }

    public RbaToken getRbaToken(MasterToken masterToken) throws AuthenticationException  {
        return authClient.loginMaster(masterToken);
    }
    public MasterToken getMasterToken(byte[] token) throws AuthenticationException, TokenFormatException {
        RbaToken locationToken = RbaToken.parseAndValidate(ByteBuffer.wrap(token));
        return new MasterToken(locationToken);
    }

    public void addClient(RBACClient rbacClient){
        rbacClients.put(rbacClient.hashCode(), rbacClient);
    }
    public RBACClient getClient(int id){
        return rbacClients.get(id);
    }

    public void clearRBACClients(){
        rbacClients.clear();
    }

    public void removeRBACClient(int id){
        rbacClients.remove(id);
    }
}
