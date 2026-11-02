package cern.rade.services.japc;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.WebSocket;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@EnabledIfEnvironmentVariable(named = "APP_URL", matches = ".+")
class EndpointTest {
    private static final String APP_URL = System.getenv("APP_URL");
    private static final String SUB_URL = System.getenv("SUB_URL");
    private static final String OPEN_DEVICE = "RADE.TEST";
    private static final String RBAC_DEVICE = "RADE.TEST.RBAC";
    private static final String EXPLICIT = "Explicit " + Base64.getUrlEncoder()
            .encodeToString(("pma:" + System.getenv("PMA_PASSWORD")).getBytes(StandardCharsets.ISO_8859_1));
    private static final HttpClient CLIENT = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

    @Test
    void set() throws Exception {
        assertStatus(200, set(OPEN_DEVICE, "Set", 1, null));

        assertStatus(403, set(RBAC_DEVICE, "Set", 2, null));
        HttpResponse<String> explicit = set(RBAC_DEVICE, "Set", 3, EXPLICIT);
        assertStatus(200, explicit);
        assertStatus(200, set(RBAC_DEVICE, "Set", 4, cached(explicit)));
    }

    @Test
    void get() throws Exception {
        assertStatus(200, set(OPEN_DEVICE, "Get", 1, null));
        assertEquals(1, number(value(get(OPEN_DEVICE, "Get", null))));

        String cached = cached(set(RBAC_DEVICE, "Get", 2, EXPLICIT));
        assertStatus(403, get(RBAC_DEVICE, "Get", null));
        assertEquals(2, number(value(get(RBAC_DEVICE, "Get", EXPLICIT))));
        assertEquals(2, number(value(get(RBAC_DEVICE, "Get", cached))));
    }

    @Test
    void subscribe() throws Exception {
        assertStatus(200, set(OPEN_DEVICE, "Subscribe", 1, null));
        assertEquals(1, number(firstUpdate(OPEN_DEVICE, "Subscribe", null)));

        String cached = cached(set(RBAC_DEVICE, "Subscribe", 2, EXPLICIT));
        assertThrows(Exception.class, () -> firstUpdate(RBAC_DEVICE, "Subscribe", null));
        assertEquals(2, number(firstUpdate(RBAC_DEVICE, "Subscribe", EXPLICIT)));
        assertEquals(2, number(firstUpdate(RBAC_DEVICE, "Subscribe", cached)));
    }

    private static HttpResponse<String> set(String device, String property, double number, String rbac) throws Exception {
        String body = "{\"@type\":\"map_value\",\"number\":{\"@type\":\"double_value\",\"value\":" + number + "}}";
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(APP_URL + "/" + device + "/" + property))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(body));
        return send(request, rbac);
    }

    private static HttpResponse<String> get(String device, String property, String rbac) throws Exception {
        return send(HttpRequest.newBuilder(URI.create(APP_URL + "/" + device + "/" + property)).GET(), rbac);
    }

    private static HttpResponse<String> send(HttpRequest.Builder request, String rbac) throws Exception {
        if (rbac != null) {
            request.header("Rbac", rbac);
        }
        return CLIENT.send(request.timeout(Duration.ofSeconds(30)).build(), HttpResponse.BodyHandlers.ofString());
    }

    private static void assertStatus(int expected, HttpResponse<String> response) {
        assertEquals(expected, response.statusCode(), response.body());
    }

    private static String value(HttpResponse<String> response) {
        assertStatus(200, response);
        return response.body();
    }

    private static String cached(HttpResponse<String> response) {
        assertStatus(200, response);
        return response.headers().firstValue("Rbac").orElseThrow();
    }

    private static String firstUpdate(String device, String property, String rbac) throws Exception {
        CompletableFuture<String> update = new CompletableFuture<>();
        WebSocket.Builder builder = CLIENT.newWebSocketBuilder().subprotocols("json");
        if (rbac != null) {
            builder.header("Rbac", rbac);
        }
        WebSocket socket = builder.buildAsync(URI.create(SUB_URL + "/" + device + "/" + property),
                new WebSocket.Listener() {
                    @Override
                    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                        update.complete(data.toString());
                        return null;
                    }
                }).get(30, TimeUnit.SECONDS);
        try {
            JsonObject frame = JsonParser.parseString(update.get(30, TimeUnit.SECONDS)).getAsJsonObject();
            assertEquals("data", frame.get("type").getAsString(), frame.toString());
            return frame.get("value").toString();
        } finally {
            socket.abort();
        }
    }

    private static double number(String value) {
        return JsonParser.parseString(value).getAsJsonObject()
                .getAsJsonObject("number").get("value").getAsDouble();
    }
}
