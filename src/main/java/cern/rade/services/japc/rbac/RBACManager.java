package cern.rade.services.japc.rbac;

import cern.cmw.util.config.Configuration;
import cern.cmw.util.config.ConfigurationBuilder;
import cern.rbac.client.ClientConfiguration;
import cern.rbac.client.authentication.AuthenticationClient;
import cern.rbac.client.authentication.AuthenticationException;
import cern.rbac.common.MasterToken;
import cern.rbac.common.RbaToken;
import cern.rbac.common.TokenFormatException;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;
@Component
public class RBACManager {
    private Configuration config = ConfigurationBuilder.newInstance().setProperty("rbac.token.lifetime.mins", "1440").build();
    private ClientConfiguration clientConfig = ClientConfiguration.create(config);
    private AuthenticationClient authClient =  AuthenticationClient.create(clientConfig);;
    private ConcurrentHashMap<Integer, RBACClient>rbacClients = new ConcurrentHashMap<>();

    public MasterToken getMasterToken(String username, char[] password) throws AuthenticationException {
        return authClient.loginMasterExplicit(username, password, Duration.ofDays(365L));
    }

    public RbaToken getRbaToken(MasterToken masterToken) throws AuthenticationException  {
        return authClient.loginMaster(masterToken);
    }
    public MasterToken getMasterToken(byte[] token) throws TokenFormatException {
        RbaToken locationToken = RbaToken.parseAndValidate(ByteBuffer.wrap(token));
        return new MasterToken(locationToken);
    }

    public void addClient(RBACClient rbacClient){
        rbacClients.put(rbacClient.getRbac_id(), rbacClient);
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


    public RBACClient createClient(byte[] rbac_token) throws TokenFormatException {
        return initRBACLocation(rbac_token);
    }

    public RBACClient createClient(String username, char[] password) throws AuthenticationException {
        return initRBACExplicit(username, password);
    }

    public RBACClient createClient(int rbac_id) throws AuthenticationException {
        return initRBACCached(rbac_id);
    }

    public RBACClient createClient(String authStr) throws TokenFormatException, AuthenticationException {
        String[] auth = authStr.split(" ");
        if (auth.length != 2) {
            throw new AuthenticationException("Invalid authentication format.");
        }
        switch (auth[0]) {
            case "Explicit" -> {
                byte[] credentials = Base64.getUrlDecoder().decode(auth[1]);
                String credentialsStr = new String(credentials, StandardCharsets.ISO_8859_1);
                String[] credentialsArr = credentialsStr.split(":");
                if (credentialsArr.length != 2) {
                    throw new AuthenticationException("Invalid credentials format for Rbac-Explicit.");
                }
                return createClient(credentialsArr[0], credentialsArr[1].toCharArray());
            }
            case "Location" -> {
                return createClient(Base64.getUrlDecoder().decode(auth[1]));
            }
            case "Cached" -> {
                return createClient(Integer.parseInt(auth[1]));
            }
            default -> throw new AuthenticationException("Invalid authentication type.");
        }
    }

    private RBACClient initRBACExplicit(String username, char[] password) throws AuthenticationException{
        MasterToken masterToken = getMasterToken(username, password);
        RbaToken applicationToken = getRbaToken(masterToken);
        RBACClient rbacClient = new RBACClient(masterToken, applicationToken);
        addClient(rbacClient);
        return rbacClient;
    }

    private RBACClient initRBACCached(int rbac_id) throws AuthenticationException{
        RBACClient rbacClient = getClient(rbac_id);
        if (rbacClient==null){
            throw new AuthenticationException("No RBAC token associated with this ID");
        }
        return rbacClient;
    }

    private RBACClient initRBACLocation(byte[] rbac_token) throws TokenFormatException {
        MasterToken masterToken = getMasterToken(rbac_token);
        RbaToken applicationToken = getRbaToken(masterToken);
        RBACClient rbacClient = new RBACClient(masterToken, applicationToken);
        addClient(rbacClient);
        return rbacClient;
    }
}
