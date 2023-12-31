package cern.rade.services.japc.endpoints;

import cern.rade.services.japc.ContextProvider;
import cern.rade.services.japc.rbac.RBACClient;
import cern.rade.services.japc.rbac.RBACManager;
import cern.rbac.client.authentication.AuthenticationException;
import cern.rbac.common.TokenFormatException;
import org.springframework.http.HttpHeaders;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class Endpoint {
    RBACManager rbacManager = ContextProvider.getRbacManager();
    protected RBACClient createRbacClient(String authStr) throws TokenFormatException, AuthenticationException {
        String[] auth = authStr.split(" ");
        if (auth.length != 2) {
            throw new AuthenticationException("Invalid authentication format.");
        }
        switch (auth[0]) {
            case "Explicit" -> {
                byte[] credentials = Base64.getDecoder().decode(auth[1]);
                String credentialsStr = new String(credentials, StandardCharsets.ISO_8859_1);
                String[] credentialsArr = credentialsStr.split(":");
                if (credentialsArr.length != 2) {
                    throw new AuthenticationException("Invalid credentials format for Rbac-Explicit.");
                }
                return rbacManager.createClient(credentialsArr[0], credentialsArr[1].toCharArray());
            }
            case "Location" -> {
                return rbacManager.createClient(Base64.getDecoder().decode(auth[1]));
            }
            case "Cached" -> {
                return rbacManager.createClient(Integer.parseInt(auth[1]));
            }
            default -> throw new AuthenticationException("Invalid authentication type.");
        }
    }



}
