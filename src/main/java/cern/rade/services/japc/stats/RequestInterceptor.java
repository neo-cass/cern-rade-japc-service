package cern.rade.services.japc.stats;

import cern.rade.services.japc.ratelimit.RateLimitedException;
import cern.rade.services.japc.ratelimit.RateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;

/**
 * Rate limits and records every GET and SET handled by the API.
 */
public class RequestInterceptor implements HandlerInterceptor {
    private static final String START = RequestInterceptor.class.getName() + ".start";
    private static final Map<String, Operation> OPERATIONS = Map.of(
            HttpMethod.GET.name(), Operation.GET,
            HttpMethod.PUT.name(), Operation.SET);
    private final RequestMetrics metrics;
    private final RateLimiter rateLimiter;

    public RequestInterceptor(RequestMetrics metrics, RateLimiter rateLimiter) {
        this.metrics = metrics;
        this.rateLimiter = rateLimiter;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Operation operation = OPERATIONS.get(request.getMethod());
        if (operation == null) {
            return true;
        }
        long start = System.nanoTime();
        request.setAttribute(START, start);
        Client client = Client.of(request);
        try {
            rateLimiter.check(client.user(), response);
        } catch (RateLimitedException e) {
            // afterCompletion is skipped when preHandle throws
            metrics.record(client, operation, request.getRequestURI(), HttpStatus.TOO_MANY_REQUESTS.value(), start);
            throw e;
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        Operation operation = OPERATIONS.get(request.getMethod());
        if (operation != null && request.getAttribute(START) instanceof Long start) {
            metrics.record(Client.of(request), operation, request.getRequestURI(), response.getStatus(), start);
        }
    }
}
