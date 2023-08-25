package cern.rade.services.japc.rbac;

import cern.rade.services.common.communication.AuthenticationType;
import cern.rbac.client.authentication.AuthenticationException;
import cern.rbac.common.MasterToken;
import cern.rbac.common.RbaToken;
import cern.rbac.common.TokenFormatException;

import java.nio.ByteBuffer;


public class RBACClient {
    private RbaToken applicationToken;
    private MasterToken masterToken;
    private int rbac_id;
    public RBACClient(String username, String password) throws AuthenticationException {
        masterToken = RBACManager.getMasterToken(username, password);
        applicationToken = RBACManager.getRbaToken(masterToken);
        rbac_id = this.hashCode();
    }

    public RBACClient(byte[] token) throws AuthenticationException, TokenFormatException {
        masterToken = RBACManager.getMasterToken(token);
        applicationToken = RBACManager.getRbaToken(masterToken);
        rbac_id = this.hashCode();
    }

    private RbaToken relogin() throws AuthenticationException {
        if(masterToken.isValid()){
            applicationToken = RBACManager.getRbaToken(masterToken);
            return applicationToken;
        }
        else{
            RBACManager.removeRBACClient(this.rbac_id);
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
