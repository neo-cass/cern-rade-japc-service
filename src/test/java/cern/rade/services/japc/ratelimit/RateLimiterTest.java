package cern.rade.services.japc.ratelimit;

import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RateLimiterTest {
    private final Map<String, String> headers = new HashMap<>();
    private final HttpServletResponse response = (HttpServletResponse) Proxy.newProxyInstance(getClass().getClassLoader(),
            new Class<?>[]{HttpServletResponse.class}, (proxy, method, args) -> {
                if (method.getName().equals("setHeader")) {
                    headers.put((String) args[0], (String) args[1]);
                }
                return null;
            });
    private final RateLimiter limiter = new RateLimiter(new RateLimitProperties(1, 2, 1, Map.of("shared", 0)));

    @Test
    void rejectsBeyondBurstWithRetryAfter() {
        limiter.check("alice", response);
        limiter.check("alice", response);
        assertThrows(RateLimitedException.class, () -> limiter.check("alice", response));
        assertNotNull(headers.get("Retry-After"));
        assertDoesNotThrow(() -> limiter.check("bob", response));
    }

    @Test
    void overrideZeroIsUnlimited() {
        for (int i = 0; i < 10; i++) {
            limiter.check("shared", response);
        }
    }

    @Test
    void limitsOpenSubscriptions() {
        assertDoesNotThrow(() -> limiter.checkSubscriptions("alice", 0));
        assertThrows(RateLimitedException.class, () -> limiter.checkSubscriptions("alice", 1));
    }
}
