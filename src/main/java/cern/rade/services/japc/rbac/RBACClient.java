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

    public RbaToken login() throws AuthenticationException {
        if(masterToken==null || !masterToken.isValid()){return null;}
        return RBACManager.getRbaToken(masterToken);
    }

    public RbaToken getApplicationToken() {
        return applicationToken;
    }
}
