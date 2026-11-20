package cern.rade.services.japc.rbac;

import cern.rade.services.japc.MutableClock;
import cern.rbac.client.authentication.AuthenticationException;
import cern.rbac.common.RbaToken;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Signature validation of {@code Rbac: Token} tokens runs once per token, and the result lives until the token ends.
 */
class TokenValidationCacheTest {
    private static final Instant START = Instant.parse("2030-01-01T00:00:00Z");
    private final MutableClock clock = new MutableClock(START);
    private final Instant endTime = START.plus(Duration.ofHours(1));
    private int calls;
    private boolean rejecting;

    private TokenValidationCache cache(int maxEntries) {
        return new TokenValidationCache(encoded -> {
            calls++;
            if (rejecting) {
                throw new AuthenticationException("bad signature");
            }
            return RbaToken.EMPTY_TOKEN;
        }, token -> endTime, maxEntries, clock);
    }

    @Test
    void validatesATokenOnlyOnce() {
        TokenValidationCache cache = cache(10);
        assertSame(RbaToken.EMPTY_TOKEN, cache.validate(new byte[]{1, 2, 3}));
        assertSame(RbaToken.EMPTY_TOKEN, cache.validate(new byte[]{1, 2, 3}));
        assertEquals(1, calls);
        assertEquals(1, cache.size());
    }

    @Test
    void differentTokensAreValidatedSeparately() {
        TokenValidationCache cache = cache(10);
        cache.validate(new byte[]{1});
        cache.validate(new byte[]{2});
        assertEquals(2, calls);
        assertEquals(2, cache.size());
    }

    @Test
    void entryExpiresExactlyAtTheEndTime() {
        TokenValidationCache cache = cache(10);
        cache.validate(new byte[]{1});
        clock.advance(Duration.ofHours(1).minusNanos(1));
        cache.validate(new byte[]{1});
        assertEquals(1, calls);

        clock.advance(Duration.ofNanos(1));
        assertThrows(AuthenticationException.class, () -> cache.validate(new byte[]{1}));
    }

    @Test
    void expiredHitIsRemovedAndRejected() {
        TokenValidationCache cache = cache(10);
        cache.validate(new byte[]{1});
        clock.advance(Duration.ofHours(2));

        AuthenticationException e = assertThrows(AuthenticationException.class, () -> cache.validate(new byte[]{1}));

        assertEquals(TokenValidationCache.EXPIRED, e.getMessage());
        assertEquals(0, cache.size());
        assertEquals(1, calls);
    }

    @Test
    void alreadyExpiredTokenIsRejectedAndNotCached() {
        TokenValidationCache cache = cache(10);
        clock.advance(Duration.ofHours(2));

        assertThrows(AuthenticationException.class, () -> cache.validate(new byte[]{1}));

        assertEquals(0, cache.size());
    }

    @Test
    void invalidTokenPropagatesAndIsNotCached() {
        TokenValidationCache cache = cache(10);
        rejecting = true;

        assertThrows(AuthenticationException.class, () -> cache.validate(new byte[]{1}));
        assertThrows(AuthenticationException.class, () -> cache.validate(new byte[]{1}));

        assertEquals(2, calls);
        assertEquals(0, cache.size());
    }

    @Test
    void fullCacheStillValidatesButDoesNotStore() {
        TokenValidationCache cache = cache(1);
        cache.validate(new byte[]{1});
        assertSame(RbaToken.EMPTY_TOKEN, cache.validate(new byte[]{2}));
        assertSame(RbaToken.EMPTY_TOKEN, cache.validate(new byte[]{2}));

        assertEquals(3, calls);
        assertEquals(1, cache.size());
    }

    @Test
    void sweepRemovesExpiredEntries() {
        TokenValidationCache cache = cache(10);
        cache.validate(new byte[]{1});
        cache.validate(new byte[]{2});

        cache.removeExpired();
        assertEquals(2, cache.size());

        clock.advance(Duration.ofHours(1));
        cache.removeExpired();
        assertEquals(0, cache.size());
    }
}
