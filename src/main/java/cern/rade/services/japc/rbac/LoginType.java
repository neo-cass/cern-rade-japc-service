package cern.rade.services.japc.rbac;

/**
 * How an {@link RbacSession} was obtained. Sessions logged in by this service hold a master token and are cached, so
 * clients can reuse them with {@code Rbac: Cached <id>}; sessions built from a token the client already holds are not.
 */
public enum LoginType {
    EXPLICIT(true),
    KERBEROS(true),
    OAUTH(true),
    TOKEN(false);

    private final boolean cached;

    LoginType(boolean cached) {
        this.cached = cached;
    }

    public boolean cached() {
        return cached;
    }
}
