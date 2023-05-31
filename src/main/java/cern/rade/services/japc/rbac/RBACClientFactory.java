package cern.rade.services.japc.rbac;

import cern.rade.services.common.communication.AuthenticationType;
import cern.rade.services.common.communication.MetaData;
import cern.rade.services.common.communication.Request;
import cern.rbac.client.authentication.AuthenticationException;

public class RBACClientFactory {
    private Request request;
    private MetaData metaData;
    private AuthenticationType authenticationType;
    private RBACClient rbacClient;
    private int rbac_id;
    public RBACClientFactory(Request request){
        this.request=request;
        this.metaData=request.getMetaData();
        this.authenticationType=request.getAuthenticationType();
    }

    public RBACClient createClient() throws AuthenticationException{
        switch(request.getAuthenticationType()){
            case RBAC_CACHED -> {
                initRBACCached();
            }
            case RBAC_EXPLICIT -> {
                initRBACExplicit();
            }
            case RBAC_LOCATION -> {
                //TODO
            }
        }

        return rbacClient;
    }

    private void initRBACExplicit() throws AuthenticationException{
        try{
            this.rbacClient = new RBACClient(metaData.getUsername(), metaData.getPassword());
            RBACManager.addClient(rbacClient);
        }
        catch (AuthenticationException e){
            this.rbacClient = null;
        }
    }

    private void initRBACCached() throws AuthenticationException{
        this.rbac_id = metaData.getRbac_id();
        this.rbacClient = RBACManager.getClient(rbac_id);
        if (rbacClient==null){
            throw new AuthenticationException("No RBAC token associated with this ID");
        }
    }
}
