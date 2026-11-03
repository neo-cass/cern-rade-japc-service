package cern.rade.services.japc.admin;

import cern.rade.services.japc.admin.view.ServiceInfoView;
import cern.rade.services.japc.admin.view.SessionView;
import cern.rade.services.japc.rbac.RbacProperties;
import cern.rade.services.japc.rbac.RbacSessionRegistry;
import cern.rade.services.japc.rbac.gateway.RbacGateway;
import cern.rade.services.japc.stats.ClientRate;
import cern.rade.services.japc.stats.ClientStats;
import cern.rade.services.japc.subscription.SubscriptionInfo;
import cern.rade.services.japc.subscription.SubscriptionRegistry;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;

/**
 * What the admin panel shows and can change.
 */
@Service
public class AdminService {
    private static final String UNKNOWN_VERSION = "unknown";
    private final RbacSessionRegistry sessions;
    private final SubscriptionRegistry subscriptions;
    private final ClientStats clientStats;
    private final RbacProperties properties;
    private final RbacGateway gateway;
    private final ObjectProvider<BuildProperties> buildProperties;
    private final Clock clock;

    public AdminService(RbacSessionRegistry sessions, SubscriptionRegistry subscriptions, ClientStats clientStats,
                        RbacProperties properties, RbacGateway gateway, ObjectProvider<BuildProperties> buildProperties,
                        Clock clock) {
        this.sessions = sessions;
        this.subscriptions = subscriptions;
        this.clientStats = clientStats;
        this.properties = properties;
        this.gateway = gateway;
        this.buildProperties = buildProperties;
        this.clock = clock;
    }

    public ServiceInfoView info() {
        Instant startedAt = Instant.ofEpochMilli(ManagementFactory.getRuntimeMXBean().getStartTime());
        String version = buildProperties.stream().map(BuildProperties::getVersion).findFirst().orElse(UNKNOWN_VERSION);
        return new ServiceInfoView(version, startedAt, Duration.between(startedAt, clock.instant()).toString(),
                gateway.environment(), properties.requiredRole(), properties.adminRole(),
                properties.tokenLifetime().toString(), properties.sessionLifetime().toString(),
                properties.sessionIdleTimeout().toString(), sessions.list().size(), subscriptions.subscriptions().size());
    }

    public List<SessionView> sessions() {
        return sessions.list().stream()
                .map(SessionView::of)
                .sorted(Comparator.comparing(SessionView::lastUsed).reversed())
                .toList();
    }

    public boolean revokeSession(String handle) {
        return sessions.revoke(handle);
    }

    public int revokeAllSessions() {
        return sessions.revokeAll();
    }

    public List<SubscriptionInfo> subscriptions() {
        return subscriptions.subscriptions().stream()
                .sorted(Comparator.comparing(SubscriptionInfo::openedAt).reversed())
                .toList();
    }

    public boolean closeSubscription(String id) {
        return subscriptions.closeSubscription(id);
    }

    public List<ClientRate> clients() {
        return clientStats.rates();
    }
}
