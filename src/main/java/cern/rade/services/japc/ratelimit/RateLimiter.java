package cern.rade.services.japc.ratelimit;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * One token bucket per RBAC user (a bounded set, unlike addresses), in memory: limits reset on restart.
 */
@Component
public class RateLimiter {
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final RateLimitProperties properties;

    public RateLimiter(RateLimitProperties properties) {
        this.properties = properties;
    }

    /**
     * @throws RateLimitedException after setting {@code Retry-After} on the response
     */
    public void check(String user, HttpServletResponse response) {
        int rate = properties.requestsPerSecondByUser().getOrDefault(user, properties.requestsPerSecond());
        if (rate <= 0) {
            return;
        }
        Bucket bucket = buckets.computeIfAbsent(user, key -> Bucket.builder()
                .addLimit(limit -> limit.capacity(Math.max(rate, properties.burst())).refillGreedy(rate, Duration.ofSeconds(1)))
                .build());
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        if (!probe.isConsumed()) {
            response.setHeader(HttpHeaders.RETRY_AFTER, String.valueOf(TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill()) + 1));
            throw new RateLimitedException("Rate limit of " + rate + " requests per second exceeded for " + user);
        }
    }

    public void checkSubscriptions(String user, long open) {
        int max = properties.subscriptionsPerUser();
        if (max > 0 && open >= max) {
            throw new RateLimitedException("Limit of " + max + " open subscriptions reached for " + user);
        }
    }
}
