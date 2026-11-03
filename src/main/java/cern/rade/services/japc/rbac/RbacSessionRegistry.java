package cern.rade.services.japc.rbac;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

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
        return sessions.values().removeIf(session -> session.handle().equals(handle));
    }

    public int revokeAll() {
        int count = sessions.size();
        sessions.clear();
        return count;
    }

    @Scheduled(fixedDelayString = "PT5M", initialDelayString = "PT5M")
    public void removeStale() {
        int before = sessions.size();
        sessions.values().removeIf(this::isStale);
        int removed = before - sessions.size();
        if (removed > 0) {
            logger.info("Removed {} expired or idle RBAC sessions", removed);
        }
    }

    boolean isStale(RbacSession session) {
        return session.isExpired() || isIdle(session);
    }

    private boolean isIdle(RbacSession session) {
        return properties.idleTimeoutEnabled()
                && session.lastUsed().plus(properties.sessionIdleTimeout()).isBefore(clock.instant());
    }
}
