package cern.rade.services.japc.rbac;

import cern.rbac.common.RbaToken;

import java.time.Clock;
import java.time.Instant;
import java.util.Set;

/**
 * A session with a chosen user and roles, since tests cannot create a signed RBAC token.
 */
public final class TestSessions {
    private TestSessions() {
    }

    public static RbacSession of(String user, String... roles) {
        return new RbacSession(LoginType.EXPLICIT, RbaToken.EMPTY_TOKEN, null, null, ClientContext.UNKNOWN, Clock.systemUTC()) {
            @Override
            public String user() {
                return user;
            }

            @Override
            public Set<String> roles() {
                return Set.of(roles);
            }

            @Override
            public RbaToken applicationToken() {
                return RbaToken.EMPTY_TOKEN;
            }

            @Override
            public boolean isExpired() {
                return false;
            }

            @Override
            public Instant tokenExpiry() {
                return Instant.now().plusSeconds(3600);
            }

            @Override
            public Instant sessionExpiry() {
                return Instant.now().plusSeconds(3600);
            }

            @Override
            public String location() {
                return null;
            }
        };
    }
}
