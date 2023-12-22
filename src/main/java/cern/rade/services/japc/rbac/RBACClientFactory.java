package cern.rade.services.japc.rbac;

import cern.rade.services.common.communication.AuthenticationType;
import cern.rade.services.common.communication.MetaData;
import cern.rade.services.japc.ContextProvider;
import cern.rbac.client.authentication.AuthenticationException;
import cern.rbac.common.TokenFormatException;

public class RBACClientFactory {
    private final RBACManager rbacManager = ContextProvider.getRbacManager();
    public RBACClientFactory(){
    }


}
