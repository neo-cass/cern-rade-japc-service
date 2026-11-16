package cern.rade.services.japc.ratelimit;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.EstimationProbe;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Rate limits, in memory, so they reset on restart: one token bucket per RBAC user for requests, and one per client
 * address for failed logins.
 */
@Component
public class RateLimiter {
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> failedLogins = new ConcurrentHashMap<>();
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
            setRetryAfter(response, probe.getNanosToWaitForRefill());
            throw new RateLimitedException("Rate limit of " + rate + " requests per second exceeded for " + user);
        }
    }

    /**
     * Refuses a login from an address that has used up its failed logins. Does not use up the allowance itself.
     *
     * @throws RateLimitedException after setting {@code Retry-After} on the response
     */
    public void checkLogin(String address, HttpServletResponse response) {
        Bucket bucket = failedLogins.get(address);
        if (properties.loginFailuresPerMinute() <= 0 || bucket == null) {
            return;
        }
        EstimationProbe probe = bucket.estimateAbilityToConsume(1);
        if (!probe.canBeConsumed()) {
            setRetryAfter(response, probe.getNanosToWaitForRefill());
            throw new RateLimitedException("Too many failed logins from this address");
        }
    }

    public void loginFailed(String address) {
        int limit = properties.loginFailuresPerMinute();
        if (limit <= 0) {
            return;
        }
        failedLogins.computeIfAbsent(address, key -> Bucket.builder()
                .addLimit(bandwidth -> bandwidth.capacity(limit).refillGreedy(limit, Duration.ofMinutes(1)))
                .build()).tryConsume(1);
    }

    /**
     * Addresses are unbounded, unlike users, so an address that failed once must not stay in memory forever.
     */
    @Scheduled(fixedDelayString = "PT10M")
    void forgetRecoveredAddresses() {
        failedLogins.values().removeIf(bucket -> bucket.getAvailableTokens() >= properties.loginFailuresPerMinute());
    }

    private static void setRetryAfter(HttpServletResponse response, long nanosToWaitForRefill) {
        response.setHeader(HttpHeaders.RETRY_AFTER, String.valueOf(TimeUnit.NANOSECONDS.toSeconds(nanosToWaitForRefill) + 1));
    }

    public void checkSubscriptions(String user, long open) {
        int max = properties.subscriptionsPerUser();
        if (max > 0 && open >= max) {
            throw new RateLimitedException("Limit of " + max + " open subscriptions reached for " + user);
        }
    }
}
