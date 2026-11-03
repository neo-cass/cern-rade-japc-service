package cern.rade.services.japc.rbac.gateway;

import cern.rbac.client.ClientConfiguration;
import cern.rbac.client.authentication.AuthenticationClient;
import cern.rbac.client.authentication.AuthenticationException;
import cern.rbac.client.impl.request.ServerRequestInvokerImpl;
import cern.rbac.client.request.ServerRequestException;
import cern.rbac.common.MasterToken;
import cern.rbac.common.RbaToken;
import cern.rbac.common.TokenFormatException;
import cern.rbac.common.TokenType;
import cern.rbac.common.impl.request.AuthenticationRequestBuilder;
import cern.rbac.common.impl.request.RequestType;
import cern.rbac.util.oauth2.OAuth2ExchangeClient;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.Base64;

/**
 * The only class that talks to the RBAC client libraries. Everything else in the service works with the tokens it
 * returns, so an RBAC library upgrade only touches this class.
 */
public class RbacGateway {
    private final ClientConfiguration configuration;
    private final AuthenticationClient authenticationClient;

    public RbacGateway(ClientConfiguration configuration) {
        this.configuration = configuration;
        this.authenticationClient = AuthenticationClient.create(configuration);
    }

    public MasterToken explicitMaster(String user, char[] password, Duration lifetime) {
        return authenticationClient.loginMasterExplicit(user, password, lifetime);
    }

    public MasterToken oauthMaster(String rbacAudienceAccessToken, Duration lifetime) {
        return authenticationClient.loginMasterOauth(rbacAudienceAccessToken, lifetime);
    }

    /**
     * Logs in with a Kerberos service ticket that the client obtained for the RBAC server
     * ({@code RBAC/rbac-pro-lb.cern.ch@CERN.CH} in PRO). {@link AuthenticationClient#loginMasterKerberos()} cannot be used
     * because it reads the ticket cache of this process, so the request is built the same way as rbac-client's
     * {@code KerberosMasterLoginModule}.
     */
    public MasterToken kerberosMaster(byte[] serviceTicket, Duration lifetime) {
        AuthenticationRequestBuilder builder = AuthenticationRequestBuilder.newInstance()
                .setTokenType(TokenType.MASTER)
                .setLifetime(minutes(lifetime))
                .setKerberosTicket(Base64.getEncoder().encode(serviceTicket));
        builder.setRequestType(RequestType.AUTHENTICATE_KERBEROS);
        try {
            byte[] response = new ServerRequestInvokerImpl(configuration).invokeRequest(builder.buildRequest());
            return new MasterToken(validate(Base64.getDecoder().decode(response)));
        } catch (ServerRequestException e) {
            throw new AuthenticationException("RBAC Kerberos login failed: " + e.getMessage(), e);
        }
    }

    public RbaToken renew(MasterToken master, Duration lifetime) {
        return authenticationClient.loginMaster(master, lifetime);
    }

    /**
     * Parses a token and checks its signature against the keys of this RBAC environment.
     */
    public RbaToken validate(byte[] encodedToken) {
        try {
            return RbaToken.parseAndValidate(ByteBuffer.wrap(encodedToken), configuration.getRuntimeConfiguration());
        } catch (TokenFormatException e) {
            throw new AuthenticationException("Invalid RBAC token: " + e.getMessage(), e);
        }
    }

    /**
     * Exchanges a CERN SSO access token for one whose audience is the RBAC server of this environment.
     */
    public String exchangeForRbac(String clientId, String clientSecret, String userAccessToken) {
        return OAuth2ExchangeClient.getInstance().exchange(clientId, clientSecret, userAccessToken).getAccessToken();
    }

    public String environment() {
        return configuration.getRuntimeConfiguration().getEnvironment().name();
    }

    private static int minutes(Duration lifetime) {
        return Math.toIntExact(lifetime.toMinutes());
    }
}
