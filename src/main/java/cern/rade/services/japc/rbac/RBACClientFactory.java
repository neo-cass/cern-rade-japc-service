package cern.rade.services.japc.rbac;

import cern.rade.services.common.communication.AuthenticationType;
import cern.rade.services.common.communication.MetaData;
import cern.rade.services.common.communication.Request;
import cern.rbac.client.authentication.AuthenticationException;
import cern.rbac.common.TokenFormatException;

public class RBACClientFactory {

    private MetaData metaData;
    private AuthenticationType authenticationType;
    public RBACClientFactory(MetaData metaData){
        this.metaData=metaData;
        this.authenticationType=metaData.getAuth_type();
    }

    public RBACClient createClient() throws AuthenticationException, TokenFormatException {
        switch(authenticationType){
            case RBAC_CACHED -> {
                return initRBACCached();
            }
            case RBAC_EXPLICIT -> {
                return initRBACExplicit();
            }
            case RBAC_LOCATION -> {
                return initRBACLocation();
            }
            default -> {
                throw new AuthenticationException("auth_type is NONE.");//TODO
            }
        }
    }

    private RBACClient initRBACExplicit() throws AuthenticationException{
        RBACClient rbacClient = new RBACClient(metaData.getUsername(), metaData.getPassword());
        RBACManager.addClient(rbacClient);
        return rbacClient;
    }

    private RBACClient initRBACCached() throws AuthenticationException{
        int rbac_id = metaData.getRbac_id();
        RBACClient rbacClient = RBACManager.getClient(rbac_id);
        if (rbacClient==null){
            throw new AuthenticationException("No RBAC token associated with this ID");
        }
        return rbacClient;
    }

    private RBACClient initRBACLocation() throws AuthenticationException, TokenFormatException {
        RBACClient rbacClient = new RBACClient(metaData.getRbac_token());
        RBACManager.addClient(rbacClient);
        return rbacClient;
    }
}
