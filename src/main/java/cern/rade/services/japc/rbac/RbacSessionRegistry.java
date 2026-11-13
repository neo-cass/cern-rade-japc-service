package cern.rade.services.japc.rbac;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Clock;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/**
 * The cached sessions clients reuse with {@code Rbac: Cached <id>}.
 */
public class RbacSessionRegistry {
    private final Map<String, RbacSession> sessions = new ConcurrentHashMap<>();
    private final RbacProperties properties;
    private final Clock clock;
    private final Logger logger = LoggerFactory.getLogger(RbacSessionRegistry.class);

    public RbacSessionRegistry(RbacProperties properties, Clock clock) {
        this.properties = properties;
        this.clock = clock;
    }

    /**
     * Caches a session. A user may hold several (one per browser, LabVIEW client, script...); above
     * {@code sessions-per-user} their least recently used ones are revoked, so a client that logs in repeatedly
     * instead of reusing its cached id cannot pile up sessions.
     */
    public synchronized RbacSession register(RbacSession session) {
        sessions.put(session.id(), session);
        enforceSessionLimit(session);
        return session;
    }

    public Optional<RbacSession> find(String id) {
        return Optional.ofNullable(sessions.get(id));
    }

    public List<RbacSession> list() {
        return List.copyOf(sessions.values());
    }

    public List<RbacSession> sessionsOf(String user) {
        return sessions.values().stream().filter(session -> session.user().equals(user)).toList();
    }

    public boolean revoke(String handle) {
        return revokeIf(session -> session.handle().equals(handle)) > 0;
    }

    public int revokeUser(String user) {
        return revokeIf(session -> session.user().equals(user));
    }

    public void revoke(RbacSession session) {
        sessions.remove(session.id());
        session.revoke();
    }

    /**
     * Drops a session created by a login request whose client never received its cached id, e.g. a login without the
     * required role. Nothing can reuse it.
     */
    public void releaseIfUnissued(RbacSession session) {
        if (!session.issued()) {
            revoke(session);
        }
    }

    public int revokeAll() {
        return revokeIf(session -> true);
    }

    @Scheduled(fixedDelayString = "PT5M", initialDelayString = "PT5M")
    public void removeStale() {
        int removed = revokeIf(this::isStale);
        if (removed > 0) {
            logger.info("Removed {} expired or idle RBAC sessions", removed);
        }
    }

    private void enforceSessionLimit(RbacSession newest) {
        if (!properties.sessionLimitEnabled()) {
            return;
        }
        List<RbacSession> others = sessionsOf(newest.user()).stream()
                .filter(session -> session != newest)
                .sorted(Comparator.comparing(RbacSession::lastUsed))
                .toList();
        int excess = others.size() + 1 - properties.sessionsPerUser();
        if (excess > 0) {
            others.subList(0, excess).forEach(this::revoke);
            logger.warn("User {} exceeded {} RBAC sessions, revoked the {} least recently used. A client is probably calling"
                    + " PUT /rbac/session repeatedly instead of reusing its Rbac: Cached id", newest.user(), properties.sessionsPerUser(), excess);
        }
    }

    /**
     * Removes and revokes the matching sessions, so browser logins holding them end too.
     */
    private int revokeIf(Predicate<RbacSession> condition) {
        List<RbacSession> matching = sessions.values().stream().filter(condition).toList();
        matching.forEach(this::revoke);
        return matching.size();
    }

    boolean isStale(RbacSession session) {
        return session.isExpired() || isIdle(session);
    }

    private boolean isIdle(RbacSession session) {
        return properties.idleTimeoutEnabled()
                && session.lastUsed().plus(properties.sessionIdleTimeout()).isBefore(clock.instant());
    }
}
