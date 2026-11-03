package cern.rade.services.japc.rbac;

import cern.rbac.client.authentication.AuthenticationException;
import cern.rbac.common.MasterToken;
import cern.rbac.common.RbaToken;
import cern.rbac.common.Role;
import cern.rbac.util.holder.MiddleTierRbaTokenHolder;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * An authenticated RBAC client of this service. It holds the application token used for JAPC calls and, when this
 * service performed the login, the master token used to renew it. Location and pass-through tokens were issued to the
 * client's machine, so RBAC refuses to renew them here: they are used until they expire.
 */
public class RbacSession {
    private static final SecureRandom RANDOM = new SecureRandom();

    private final String id = UUID.randomUUID().toString();
    private final String handle = randomHandle();
    private final LoginType loginType;
    private final MasterToken master;
    private final TokenRenewer renewer;
    private final Clock clock;
    private final Instant created;
    private volatile RbaToken token;
    private volatile Instant lastUsed;

    /**
     * Renews the application token from the master token.
     */
    @FunctionalInterface
    public interface TokenRenewer {
        RbaToken renew(MasterToken master);
    }

    @FunctionalInterface
    public interface Action<T, E extends Exception> {
        T run() throws E;
    }

    @FunctionalInterface
    public interface VoidAction<E extends Exception> {
        void run() throws E;
    }

    RbacSession(LoginType loginType, RbaToken token, MasterToken master, TokenRenewer renewer, Clock clock) {
        this.loginType = loginType;
        this.token = token;
        this.master = master;
        this.renewer = renewer;
        this.clock = clock;
        this.created = clock.instant();
        this.lastUsed = created;
    }

    /**
     * Runs a JAPC call with the session's token, or without RBAC when there is no session.
     */
    public static <T, E extends Exception> T callAs(RbacSession session, Action<T, E> action) throws E {
        return session == null ? action.run() : session.callAs(action);
    }

    public static <E extends Exception> void runAs(RbacSession session, VoidAction<E> action) throws E {
        callAs(session, () -> {
            action.run();
            return null;
        });
    }

    public <T, E extends Exception> T callAs(Action<T, E> action) throws E {
        MiddleTierRbaTokenHolder.set(applicationToken());
        try {
            return action.run();
        } finally {
            MiddleTierRbaTokenHolder.clear();
        }
    }

    /**
     * Returns a valid application token, renewing it from the master token when it has expired.
     *
     * @throws AuthenticationException if the session can no longer be renewed
     */
    public synchronized RbaToken applicationToken() {
        lastUsed = clock.instant();
        if (token.isValid()) {
            return token;
        }
        if (!renewable()) {
            throw new AuthenticationException("RBAC session has expired. Please log in again");
        }
        token = renewer.renew(master);
        return token;
    }

    public boolean isExpired() {
        return !token.isValid() && !renewable();
    }

    /**
     * Secret identifier, sent back to the client as {@code Rbac: Cached <id>}. Never show it to anyone else.
     */
    public String id() {
        return id;
    }

    /**
     * Public identifier for the admin API.
     */
    public String handle() {
        return handle;
    }

    public LoginType loginType() {
        return loginType;
    }

    public String user() {
        return token.getUser().getName();
    }

    public Set<String> roles() {
        return Arrays.stream(token.getUser().getRoles()).map(Role::getName).collect(Collectors.toUnmodifiableSet());
    }

    public String location() {
        return token.getLocation() == null ? null : token.getLocation().getName();
    }

    public Instant created() {
        return created;
    }

    public Instant lastUsed() {
        return lastUsed;
    }

    public Instant tokenExpiry() {
        return token.getEndTime().toInstant();
    }

    /**
     * When the session can no longer be used: the end of the master token, or of the token itself if it cannot be renewed.
     */
    public Instant sessionExpiry() {
        return master == null ? tokenExpiry() : master.getEndTime();
    }

    private boolean renewable() {
        return master != null && master.isValid();
    }

    private static String randomHandle() {
        byte[] bytes = new byte[6];
        RANDOM.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }
}
