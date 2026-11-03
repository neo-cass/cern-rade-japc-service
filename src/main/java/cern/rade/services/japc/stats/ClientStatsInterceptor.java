package cern.rade.services.japc.stats;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;

/**
 * Records every GET and SET handled by the API.
 */
public class ClientStatsInterceptor implements HandlerInterceptor {
    private static final Map<String, Operation> OPERATIONS = Map.of(
            HttpMethod.GET.name(), Operation.GET,
            HttpMethod.PUT.name(), Operation.SET);
    private final ClientStats stats;

    public ClientStatsInterceptor(ClientStats stats) {
        this.stats = stats;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        Operation operation = OPERATIONS.get(request.getMethod());
        if (operation != null) {
            stats.record(Client.of(request), operation);
        }
    }
}
