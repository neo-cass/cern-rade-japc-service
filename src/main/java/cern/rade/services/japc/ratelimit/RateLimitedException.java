package cern.rade.services.japc.ratelimit;

/**
 * HTTP 429, error code 5005.
 */
public class RateLimitedException extends RuntimeException {
    public RateLimitedException(String message) {
        super(message);
    }
}
