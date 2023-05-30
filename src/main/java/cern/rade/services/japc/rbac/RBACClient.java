package cern.rade.services.japc.rbac;

import cern.rbac.client.authentication.AuthenticationException;
import cern.rbac.common.MasterToken;
import cern.rbac.common.RbaToken;


public class RBACClient {
    private RbaToken applicationToken;
    private MasterToken masterToken;

    public RBACClient(String username, String password) throws AuthenticationException {
        masterToken = RBACManager.getMasterToken(username, password);
    }

    private RbaToken login() throws AuthenticationException {
        this.applicationToken= RBACManager.getRbaToken(masterToken);
        return applicationToken;
    }

    public RbaToken getApplicationToken() {
        if (applicationToken!=null && !applicationToken.isValid()){
            return login();
        }
        return applicationToken;
    }
}
