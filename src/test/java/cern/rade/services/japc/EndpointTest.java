package cern.rade.services.japc;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.WebSocket;
import java.net.http.WebSocketHandshakeException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnabledIfEnvironmentVariable(named = "APP_URL", matches = ".+")
class EndpointTest {
    private static final String APP_URL = System.getenv("APP_URL");
    private static final String SUB_URL = System.getenv("SUB_URL");
    private static final String OPEN_DEVICE = "RADE.TEST";
    private static final String RBAC_DEVICE = "RADE.TEST.RBAC";
    private static final String EXPLICIT = "Explicit " + Base64.getUrlEncoder()
            .encodeToString(("pma:" + System.getenv("PMA_PASSWORD")).getBytes(StandardCharsets.ISO_8859_1));
    private static final String UNKNOWN_CACHED = "Cached " + UUID.randomUUID();
    private static final String INVALID_KERBEROS = "Kerberos " + Base64.getUrlEncoder().encodeToString("not a ticket".getBytes(StandardCharsets.US_ASCII));
    private static final String MALFORMED_EXPLICIT = "Explicit " + Base64.getUrlEncoder().encodeToString("no separator".getBytes(StandardCharsets.US_ASCII));
    private static final HttpClient CLIENT = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private static String cached;

    @BeforeAll
    static void logIn() throws Exception {
        cached = login(EXPLICIT);
    }

    @Test
    void set() throws Exception {
        HttpResponse<String> open = set(OPEN_DEVICE, "Set", 1, cached);
        assertStatus(200, open);
        assertTrue(open.headers().firstValue("Rbac").isEmpty(), "data calls must not hand out the cached id");
        assertStatus(200, set(RBAC_DEVICE, "Set", 3, cached));
        assertStatus(200, set(RBAC_DEVICE, "Set", 4, cached));
    }

    @Test
    void get() throws Exception {
        set(OPEN_DEVICE, "Get", 1, cached);
        HttpResponse<String> open = get(OPEN_DEVICE, "Get", cached);
        assertEquals(1, number(value(open)));
        assertTrue(open.headers().firstValue("Rbac").isEmpty(), "data calls must not hand out the cached id");

        set(RBAC_DEVICE, "Get", 2, cached);
        assertEquals(2, number(value(get(RBAC_DEVICE, "Get", cached))));
    }

    @Test
    void subscribe() throws Exception {
        set(OPEN_DEVICE, "Subscribe", 1, cached);
        assertEquals(1, number(firstUpdate(OPEN_DEVICE, "Subscribe", cached)));

        set(RBAC_DEVICE, "Subscribe", 2, cached);
        assertEquals(2, number(firstUpdate(RBAC_DEVICE, "Subscribe", cached)));
    }

    @Test
    void loginReturnsAnIdThatWorksOnDataCalls() throws Exception {
        HttpResponse<String> login = putSession(EXPLICIT);
        assertStatus(204, login);
        assertEquals("", login.body());
        assertEquals("no-store", login.headers().firstValue("Cache-Control").orElseThrow());
        String id = cachedId(login);
        assertStatus(200, get(OPEN_DEVICE, "Get", id));
    }

    @Test
    void cachedOnPutReturnsTheSameSession() throws Exception {
        String id = login(EXPLICIT);
        HttpResponse<String> again = putSession(id);
        assertStatus(204, again);
        assertEquals(id, cachedId(again));
    }

    @Test
    void getReturnsTheSessionWithoutItsId() throws Exception {
        String id = login(EXPLICIT);
        HttpResponse<String> session = getSession(id);
        assertStatus(200, session);
        JsonObject json = JsonParser.parseString(session.body()).getAsJsonObject();
        assertEquals("pma", json.get("user").getAsString());
        assertEquals("EXPLICIT", json.get("loginType").getAsString());
        assertTrue(json.has("roles") && json.has("tokenExpiry") && json.has("sessionExpiry"), session.body());
        assertFalse(session.body().contains(id.substring("Cached ".length())), "the secret id must not be returned");
    }

    @Test
    void deleteRevokesTheSession() throws Exception {
        String id = login(EXPLICIT);
        assertStatus(200, get(OPEN_DEVICE, "Get", id));
        assertStatus(204, deleteSession(id));
        assertStatus(401, get(OPEN_DEVICE, "Get", id));
        assertStatus(401, putSession(id));
    }

    @Test
    void deleteDoesNotAcceptALogin() throws Exception {
        assertStatus(401, deleteSession(EXPLICIT));
        assertStatus(401, getSession(EXPLICIT));
    }

    @Test
    void loginRejectsBadOrMissingCredentials() throws Exception {
        for (String rbac : new String[]{null, UNKNOWN_CACHED, INVALID_KERBEROS, MALFORMED_EXPLICIT, "Unknown abc"}) {
            assertStatus(401, putSession(rbac));
        }
    }

    @Test
    void dataCallsRejectMissingOrInvalidLogin() throws Exception {
        for (String rbac : new String[]{null, UNKNOWN_CACHED, "Unknown abc"}) {
            assertStatus(401, get(OPEN_DEVICE, "Get", rbac));
            assertStatus(401, set(OPEN_DEVICE, "Set", 1, rbac));
        }
        ExecutionException handshake = assertThrows(ExecutionException.class, () -> firstUpdate(OPEN_DEVICE, "Subscribe", null));
        assertEquals(401, assertInstanceOf(WebSocketHandshakeException.class, handshake.getCause()).getResponse().statusCode());
    }

    @Test
    void dataCallsRejectLoginSchemes() throws Exception {
        for (String rbac : new String[]{EXPLICIT, INVALID_KERBEROS}) {
            HttpResponse<String> get = get(OPEN_DEVICE, "Get", rbac);
            assertStatus(401, get);
            assertTrue(get.body().contains("PUT /rbac/session"), get.body());
            assertStatus(401, set(OPEN_DEVICE, "Set", 1, rbac));
            ExecutionException handshake = assertThrows(ExecutionException.class, () -> firstUpdate(OPEN_DEVICE, "Subscribe", rbac));
            assertEquals(401, assertInstanceOf(WebSocketHandshakeException.class, handshake.getCause()).getResponse().statusCode());
        }
    }

    @Test
    void adminApiNeedsLogin() throws Exception {
        assertStatus(401, admin("/info", null));
        assertStatus(401, admin("/info", EXPLICIT));
        assertStatus(200, admin("/info", cached));
        assertStatus(200, admin("/sessions", cached));
    }

    private static String baseUrl() {
        return APP_URL.replaceFirst("/api(/v1)?$", "");
    }

    private static HttpResponse<String> admin(String path, String rbac) throws Exception {
        return send(HttpRequest.newBuilder(URI.create(baseUrl() + "/admin/api" + path)).GET(), rbac);
    }

    private static HttpResponse<String> putSession(String rbac) throws Exception {
        return send(session().PUT(HttpRequest.BodyPublishers.noBody()), rbac);
    }

    private static HttpResponse<String> getSession(String rbac) throws Exception {
        return send(session().GET(), rbac);
    }

    private static HttpResponse<String> deleteSession(String rbac) throws Exception {
        return send(session().DELETE(), rbac);
    }

    private static HttpRequest.Builder session() {
        return HttpRequest.newBuilder(URI.create(baseUrl() + "/rbac/session"));
    }

    /**
     * Logs in with {@code PUT /rbac/session} and returns the {@code Rbac: Cached <id>} header value to reuse.
     */
    private static String login(String rbac) throws Exception {
        return cachedId(putSession(rbac));
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

    private static String cachedId(HttpResponse<String> response) {
        assertStatus(204, response);
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
