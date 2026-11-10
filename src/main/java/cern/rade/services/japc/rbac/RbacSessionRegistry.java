package cern.rade.services.japc.rbac;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Clock;
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

    public RbacSession register(RbacSession session) {
        sessions.put(session.id(), session);
        return session;
    }

    public Optional<RbacSession> find(String id) {
        return Optional.ofNullable(sessions.get(id));
    }

    public List<RbacSession> list() {
        return List.copyOf(sessions.values());
    }

    public boolean revoke(String handle) {
        return revokeIf(session -> session.handle().equals(handle)) > 0;
    }

    public void revoke(RbacSession session) {
        sessions.remove(session.id());
        session.revoke();
    }

    /**
     * Drops a session created for a request whose client never received its cached id, e.g. a login without the
     * required role, a failed request or an admin API call. Nothing can reuse it.
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
