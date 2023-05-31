package cern.rade.services.japc.rbac;

import cern.rbac.client.authentication.AuthenticationException;
import cern.rbac.common.MasterToken;
import cern.rbac.common.RbaToken;


public class RBACClient {
    private RbaToken applicationToken;
    private MasterToken masterToken;
    private int rbac_id;
    public RBACClient(String username, String password) throws AuthenticationException {
        masterToken = RBACManager.getMasterToken(username, password);
        rbac_id = this.hashCode();
    }

    private RbaToken login() throws AuthenticationException {
        this.applicationToken= RBACManager.getRbaToken(masterToken);
        return applicationToken;
    }

    public RbaToken getApplicationToken() {
        if (applicationToken==null || !applicationToken.isValid()){
            return login();
        }
        return applicationToken;
    }

    public int getRbac_id() {
        return rbac_id;
    }
}
