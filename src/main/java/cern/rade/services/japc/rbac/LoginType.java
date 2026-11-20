package cern.rade.services.japc.rbac;

/**
 * How an {@link RbacSession} was obtained. Explicit, Kerberos and Oauth sessions are cached, so clients reuse them with
 * {@code Rbac: Cached <id>}; they hold a master token and are renewed. Token sessions are not cached: the client sends
 * the token on every request, and it is used until it expires.
 */
public enum LoginType {
    EXPLICIT,
    KERBEROS,
    OAUTH,
    TOKEN
}
