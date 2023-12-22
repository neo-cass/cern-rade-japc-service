package cern.rade.services.japc.rbac;

import cern.rade.services.japc.ContextProvider;
import cern.rbac.client.authentication.AuthenticationException;
import cern.rbac.common.MasterToken;
import cern.rbac.common.RbaToken;
import cern.rbac.common.TokenFormatException;


public class RBACClient {
    private RbaToken applicationToken;
    private MasterToken masterToken;
    private int rbac_id;
    private final RBACManager rbacManager = ContextProvider.getRbacManager();
    public RBACClient(String username, char[] password) throws AuthenticationException {
        this.masterToken = rbacManager.getMasterToken(username, password);
        this.applicationToken = rbacManager.getRbaToken(masterToken);
        this.rbac_id = this.hashCode();
    }

    public RBACClient(byte[] token) throws TokenFormatException {
        masterToken = rbacManager.getMasterToken(token);
        applicationToken = rbacManager.getRbaToken(masterToken);
        rbac_id = this.hashCode();
    }

    private RbaToken relogin() throws AuthenticationException {
        if(masterToken.isValid()){
            applicationToken = rbacManager.getRbaToken(masterToken);
            return applicationToken;
        }
        else{
            rbacManager.removeRBACClient(this.rbac_id);
            throw new AuthenticationException("Token has expired. Please reauthenticate");
        }
    }

    public RbaToken getApplicationToken() throws AuthenticationException{
        if (!applicationToken.isValid()){
            return relogin();
        }
        return applicationToken;
    }

    public int getRbac_id() {
        return rbac_id;
    }
}
