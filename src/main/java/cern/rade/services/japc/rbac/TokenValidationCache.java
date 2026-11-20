package cern.rade.services.japc.rbac;

import cern.rade.services.japc.rbac.gateway.RbacGateway;
import cern.rbac.client.authentication.AuthenticationException;
import cern.rbac.common.RbaToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * The {@code Rbac: Token} tokens that passed signature validation, so the check runs once per token rather than on
 * every request. An entry lives until the token's own end time; invalid tokens are never cached. When the cache is
 * full, tokens are still validated but not stored.
 */
public class TokenValidationCache {
    static final String EXPIRED = "RBAC token has expired. Please obtain a new one";

    private final Map<String, RbaToken> tokens = new ConcurrentHashMap<>();
    private final Function<byte[], RbaToken> validator;
    private final Function<RbaToken, Instant> endTime;
    private final int maxEntries;
    private final Clock clock;
    private volatile boolean fullWarned;
    private final Logger logger = LoggerFactory.getLogger(TokenValidationCache.class);

    public TokenValidationCache(RbacGateway gateway, RbacProperties properties, Clock clock) {
        this(gateway::validate, token -> token.getEndTime().toInstant(), properties.tokenCacheMaxEntries(), clock);
    }

    TokenValidationCache(Function<byte[], RbaToken> validator, Function<RbaToken, Instant> endTime, int maxEntries,
                         Clock clock) {
        this.validator = validator;
        this.endTime = endTime;
        this.maxEntries = maxEntries;
        this.clock = clock;
    }

    /**
     * @throws AuthenticationException if the token is invalid or has expired
     */
    public RbaToken validate(byte[] encoded) {
        String key = key(encoded);
        RbaToken cached = tokens.get(key);
        if (cached != null) {
            if (clock.instant().isBefore(endTime.apply(cached))) {
                return cached;
            }
            tokens.remove(key);
            throw new AuthenticationException(EXPIRED);
        }
        RbaToken token = validator.apply(encoded);
        if (!clock.instant().isBefore(endTime.apply(token))) {
            throw new AuthenticationException(EXPIRED);
        }
        if (tokens.size() < maxEntries) {
            tokens.put(key, token);
        } else if (!fullWarned) {
            fullWarned = true;
            logger.warn("Token validation cache is full ({} entries), validating tokens without caching them", maxEntries);
        }
        return token;
    }

    @Scheduled(fixedDelayString = "PT5M", initialDelayString = "PT5M")
    public void removeExpired() {
        Instant now = clock.instant();
        tokens.values().removeIf(token -> !now.isBefore(endTime.apply(token)));
        fullWarned = false;
    }

    int size() {
        return tokens.size();
    }

    private static String key(byte[] encoded) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(encoded));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
