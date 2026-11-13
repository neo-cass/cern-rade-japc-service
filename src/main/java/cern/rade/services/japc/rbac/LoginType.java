package cern.rade.services.japc.rbac;

/**
 * How an {@link RbacSession} was obtained. Every login is cached, so clients reuse it with {@code Rbac: Cached <id>}.
 * Explicit, Kerberos and Oauth sessions hold a master token and are renewed; Token sessions are used until the token
 * expires.
 */
public enum LoginType {
    EXPLICIT,
    KERBEROS,
    OAUTH,
    TOKEN
}
