package cern.rade.services.japc;

import cern.japc.core.AcquiredParameterValue;
import cern.japc.core.Parameter;
import cern.japc.core.ParameterException;
import cern.japc.core.ParameterValueListener;
import cern.japc.core.SubscriptionHandle;
import cern.japc.core.ValueHeader;
import cern.japc.core.factory.MapParameterValueFactory;
import cern.japc.core.factory.SimpleParameterValueFactory;
import cern.japc.value.MapParameterValue;
import cern.japc.value.ParameterValue;
import cern.rade.services.codec.labview.LVTypeEncoder;
import cern.rade.services.common.containers.labview.LVClusterDescriptor;
import cern.rade.services.common.containers.labview.LVType;
import cern.rade.services.common.containers.labview.LVTypeDescriptor;
import cern.rade.services.japc.format.LabVIEWFormat;
import cern.rade.services.japc.parameter.ParameterProvider;
import cern.rade.services.japc.rbac.RbacSession;
import cern.rade.services.japc.rbac.RbacSessionRegistry;
import cern.rade.services.japc.rbac.TestSessions;
import cern.rade.services.japc.rbac.login.RbacCredentials;
import cern.rade.services.japc.rbac.login.RbacLogin;
import cern.rade.services.japc.security.RbacTokenSupport;
import cern.rbac.common.RbaToken;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.WebSocket;
import java.net.http.WebSocketHandshakeException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The HTTP and WebSocket contract of the whole service: the real application, in process, with a stub device and a
 * stub login, called through the real security chains like a client would.
 */
class ApiContractTest {
    private static final String DATA = "/api/v1/DEV/Prop";
    private static final String LOGIN_HINT = "PUT /rbac/session";
    private static final String LABVIEW = LabVIEWFormat.MEDIA_TYPE;
    private static final String SET_JSON = "{\"@type\":\"map_value\",\"number\":{\"@type\":\"double_value\",\"value\":2.5}}";
    private static final HttpClient CLIENT = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private static ConfigurableApplicationContext context;
    private static int port;

    // ---------------------------------------------------------------------------------------------------------------
    // The application under test
    // ---------------------------------------------------------------------------------------------------------------

    /**
     * What replaces the outside world: the device, the RBAC token support and a login scheme "Test".
     */
    @Configuration
    public static class Stubs {
        static final List<Set> SETS = new CopyOnWriteArrayList<>();
        static final Map<String, AtomicInteger> STOPS = new ConcurrentHashMap<>();

        record Set(String name, ParameterValue value) {
        }

        private interface Handler {
            Object handle(String method, Object[] args) throws Throwable;
        }

        @Bean
        @Primary
        public ParameterProvider stubParameters() {
            return name -> {
                if (name.startsWith("MISSING/")) {
                    throw new ParameterException("no such device");
                }
                if (name.startsWith("BOOM/")) {
                    throw new IllegalStateException("secret internals");
                }
                return parameter(name);
            };
        }

        @Bean
        @Primary
        public RbacTokenSupport stubTokenSupport() {
            return new RbacTokenSupport() {
                @Override
                public Collection<? extends GrantedAuthority> authorities(RbaToken token) {
                    return List.of();
                }

                @Override
                public Collection<? extends GrantedAuthority> authorities(RbacSession session) {
                    return session.roles().stream().map(RbacTokenSupport::roleAuthority).map(SimpleGrantedAuthority::new).toList();
                }
            };
        }

        /**
         * {@code Rbac: Test user:ROLE1,ROLE2} logs in without an RBAC server.
         */
        @Bean
        public RbacLogin testLogin() {
            return new RbacLogin() {
                @Override
                public String scheme() {
                    return "Test";
                }

                @Override
                public RbacSession login(RbacCredentials credentials) {
                    String[] parts = credentials.value().split(":", 2);
                    return TestSessions.of(parts[0], parts[1].split(","));
                }
            };
        }

        private static Object stub(Class<?> type, Handler handler) {
            return Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, (proxy, method, args) -> {
                Object result = handler.handle(method.getName(), args == null ? new Object[0] : args);
                return result != null ? result : defaultValue(method);
            });
        }

        private static Object defaultValue(Method method) {
            Class<?> type = method.getReturnType();
            if (type == boolean.class) {
                return false;
            }
            if (type == long.class) {
                return 0L;
            }
            if (type == int.class) {
                return 0;
            }
            return null;
        }

        private static ParameterValue value(String name) {
            if (name.contains("#")) {
                return SimpleParameterValueFactory.newSimpleParameterValue(1.5);
            }
            MapParameterValue map = MapParameterValueFactory.newMapParameterValue();
            map.setDouble("number", 1.5);
            return map;
        }

        private static AcquiredParameterValue acquired(String name) {
            ValueHeader header = (ValueHeader) stub(ValueHeader.class, (method, args) -> switch (method) {
                case "getAcqStamp" -> 111L;
                case "getCycleStamp" -> 222L;
                default -> null;
            });
            return (AcquiredParameterValue) stub(AcquiredParameterValue.class, (method, args) -> switch (method) {
                case "getValue" -> value(name);
                case "getHeader" -> header;
                case "getParameterName" -> name;
                default -> null;
            });
        }

        private static Parameter parameter(String name) {
            return (Parameter) stub(Parameter.class, (method, args) -> switch (method) {
                case "getName" -> name;
                case "getValue" -> acquired(name);
                case "setValue" -> {
                    SETS.add(new Set(name, (ParameterValue) args[1]));
                    yield null;
                }
                case "createSubscription" -> subscription(name, (ParameterValueListener) args[1]);
                default -> null;
            });
        }

        private static SubscriptionHandle subscription(String name, ParameterValueListener listener) {
            boolean[] monitoring = {false};
            return (SubscriptionHandle) stub(SubscriptionHandle.class, (method, args) -> switch (method) {
                case "startMonitoring" -> {
                    monitoring[0] = true;
                    listener.valueReceived(name, acquired(name));
                    yield null;
                }
                case "stopMonitoring" -> {
                    monitoring[0] = false;
                    STOPS.computeIfAbsent(name, key -> new AtomicInteger()).incrementAndGet();
                    yield null;
                }
                case "isMonitoring" -> monitoring[0];
                default -> null;
            });
        }
    }

    @BeforeAll
    static void startService() {
        // command-line arguments, because the settings of the active profile (application-prod.yml) beat default properties
        context = new SpringApplicationBuilder(InitialiseService.class, Stubs.class).run(
                "--server.port=0",
                "--management.server.port=0",
                "--service-properties.rbac.required-role=USER",
                "--service-properties.rbac.admin-role=ADMIN",
                "--service-properties.rate-limit.burst=3",
                "--service-properties.rate-limit.requests-per-second-by-user.limited=1",
                "--service-properties.subscription.first-update-timeout=2s",
                "--service-properties.max-request-body=1KB");
        port = Integer.parseInt(context.getEnvironment().getProperty("local.server.port"));
    }

    @AfterAll
    static void stopService() {
        if (context != null) {
            context.close();
        }
    }

    // ---------------------------------------------------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------------------------------------------------

    /**
     * Registers a session of the user, as a login would, and returns the {@code Rbac} header value to use it.
     */
    private static String cached(String user, String... roles) {
        RbacSession session = context.getBean(RbacSessionRegistry.class).register(TestSessions.of(user, roles));
        return "Cached " + session.id();
    }

    private static String user(String user) {
        return cached(user, "USER");
    }

    /**
     * One HTTP request of a client.
     */
    private static final class Call {
        private final String method;
        private final String path;
        private String rbac;
        private String accept;
        private String contentType;
        private String descriptor;
        private byte[] body;

        Call(String method, String path) {
            this.method = method;
            this.path = path;
        }

        Call rbac(String rbac) {
            this.rbac = rbac;
            return this;
        }

        Call accept(String accept) {
            this.accept = accept;
            return this;
        }

        Call body(String contentType, byte[] body) {
            this.contentType = contentType;
            this.body = body;
            return this;
        }

        Call json(String body) {
            return body("application/json", body.getBytes(StandardCharsets.UTF_8));
        }

        Call descriptor(String descriptor) {
            this.descriptor = descriptor;
            return this;
        }

        HttpResponse<byte[]> send() throws Exception {
            HttpRequest.Builder request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                    .timeout(Duration.ofSeconds(30))
                    .method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofByteArray(body));
            if (rbac != null) {
                request.header("Rbac", rbac);
            }
            if (accept != null) {
                request.header("Accept", accept);
            }
            if (contentType != null) {
                request.header("Content-Type", contentType);
            }
            if (descriptor != null) {
                request.header(LabVIEWFormat.TYPE_DESCRIPTOR_HEADER, descriptor);
            }
            return CLIENT.send(request.build(), HttpResponse.BodyHandlers.ofByteArray());
        }

        String origin() {
            return "rade-japc " + method + " " + path.replaceFirst("\\?.*", "");
        }
    }

    private static Call get(String path) {
        return new Call("GET", path);
    }

    private static Call put(String path) {
        return new Call("PUT", path);
    }

    private static Call delete(String path) {
        return new Call("DELETE", path);
    }

    private static String text(HttpResponse<byte[]> response) {
        return new String(response.body(), StandardCharsets.UTF_8);
    }

    private static JsonObject json(HttpResponse<byte[]> response) {
        return JsonParser.parseString(text(response)).getAsJsonObject();
    }

    private static String header(HttpResponse<?> response, String name) {
        return response.headers().firstValue(name).orElse(null);
    }

    private static void assertStatus(int expected, HttpResponse<byte[]> response) {
        assertEquals(expected, response.statusCode(), text(response));
    }

    /**
     * Asserts the JSON error of the service and returns it.
     */
    private static JsonObject assertError(int status, int code, Call call, HttpResponse<byte[]> response) {
        assertStatus(status, response);
        assertTrue(header(response, "Content-Type").startsWith("application/json"), header(response, "Content-Type"));
        JsonObject error = json(response);
        assertTrue(error.get("status").getAsBoolean(), error.toString());
        assertEquals(code, error.get("code").getAsInt(), error.toString());
        assertEquals(call.origin(), error.get("source").getAsString());
        assertNotNull(error.get("message"), error.toString());
        return error;
    }

    private static JsonObject assertError(int status, int code, Call call) throws Exception {
        return assertError(status, code, call, call.send());
    }

    private static void assertLoginRequired(Call call) throws Exception {
        JsonObject error = assertError(401, 5002, call);
        assertTrue(error.get("message").getAsString().contains(LOGIN_HINT), error.toString());
    }

    private static String descriptorOf(String cluster, String... doubleFields) throws Exception {
        LVTypeDescriptor[] fields = Arrays.stream(doubleFields).map(name -> new LVTypeDescriptor(LVType.DBL, name))
                .toArray(LVTypeDescriptor[]::new);
        return LabVIEWFormat.typeDescriptorToHeader(LVTypeEncoder.encodeTypeDescriptor(new LVClusterDescriptor(cluster, fields)));
    }

    private static byte[] doubleBytes(double value) {
        return ByteBuffer.allocate(8).putDouble(value).array();
    }

    private static Stubs.Set setOn(String name) {
        return Stubs.SETS.stream().filter(set -> set.name().equals(name)).findFirst().orElse(null);
    }

    private static void assertSetNumber(String name, double expected) {
        Stubs.Set set = setOn(name);
        assertNotNull(set, "nothing was set on " + name);
        assertEquals(expected, set.value().castAsMap().getDouble("number"));
    }

    private static void await(String what, BooleanSupplier condition) throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (!condition.getAsBoolean()) {
            assertTrue(System.nanoTime() < deadline, "timed out waiting for " + what);
            Thread.sleep(50);
        }
    }

    private static WebSocket.Builder socket(String rbac, String subprotocol) {
        WebSocket.Builder builder = CLIENT.newWebSocketBuilder();
        if (rbac != null) {
            builder.header("Rbac", rbac);
        }
        if (subprotocol != null) {
            builder.subprotocols(subprotocol);
        }
        return builder;
    }

    private static WebSocket open(String path, String rbac, WebSocket.Listener listener) throws Exception {
        return socket(rbac, "json").buildAsync(URI.create("ws://localhost:" + port + path), listener).get(30, TimeUnit.SECONDS);
    }

    /**
     * The HTTP status of a WebSocket handshake that the server refused.
     */
    private static int handshakeStatus(String path, String rbac, String subprotocol) throws Exception {
        CompletableFuture<WebSocket> opening = socket(rbac, subprotocol)
                .buildAsync(URI.create("ws://localhost:" + port + path), new WebSocket.Listener() {
                });
        ExecutionException failure = assertThrows(ExecutionException.class, () -> opening.get(30, TimeUnit.SECONDS));
        return assertInstanceOf(WebSocketHandshakeException.class, failure.getCause()).getResponse().statusCode();
    }

    // ---------------------------------------------------------------------------------------------------------------
    // Authentication
    // ---------------------------------------------------------------------------------------------------------------

    @Test
    void requestsWithoutLoginAreToldHowToLogIn() throws Exception {
        assertLoginRequired(get(DATA));
        assertLoginRequired(put(DATA).json(SET_JSON));
        assertLoginRequired(get("/rbac/session"));
        assertLoginRequired(get("/admin/api/info"));
        assertEquals(401, handshakeStatus("/ws/v1/DEV/Prop", null, "json"));
    }

    @Test
    void loginErrorsFollowTheAcceptedFormat() throws Exception {
        HttpResponse<byte[]> response = get(DATA).accept(LABVIEW).send();
        assertStatus(401, response);
        assertTrue(header(response, "Content-Type").startsWith(LABVIEW), header(response, "Content-Type"));
        assertNotNull(header(response, LabVIEWFormat.TYPE_DESCRIPTOR_HEADER));
    }

    @Test
    void invalidRbacHeadersAreRefused() throws Exception {
        String noColon = "Explicit " + Base64.getUrlEncoder().encodeToString("nocolon".getBytes(StandardCharsets.ISO_8859_1));
        for (String rbac : new String[]{"Cached " + UUID.randomUUID(), "Unknown abc", "Cached", "Explicit !!!", noColon}) {
            assertStatus(401, put("/rbac/session").rbac(rbac).send());
        }
    }

    @Test
    void loginSchemesAreOnlyAcceptedWhereClientsLogIn() throws Exception {
        String login = "Test a:USER";
        assertLoginRequired(get(DATA).rbac(login));
        assertLoginRequired(put(DATA).rbac(login).json(SET_JSON));
        assertLoginRequired(get("/rbac/session").rbac(login));
        assertLoginRequired(delete("/rbac/session").rbac(login));
        assertLoginRequired(get("/admin/api/info").rbac(login));
        assertEquals(401, handshakeStatus("/ws/v1/DEV/Prop", login, "json"));
    }

    @Test
    void usersWithoutTheRequiredRoleAreForbidden() throws Exception {
        Call call = get(DATA).rbac(cached("other1", "OTHER"));
        JsonObject error = assertError(403, 5002, call);
        assertEquals("RBAC role USER required", error.get("message").getAsString());
        assertEquals(403, handshakeStatus("/ws/v1/DEV/Prop", cached("other2", "OTHER"), "json"));
    }

    @Test
    void theApiNeedsTheUserRoleAndTheAdminApiTheAdminRole() throws Exception {
        assertStatus(403, get("/admin/api/info").rbac(user("plain1")).send());
        assertStatus(403, get(DATA).rbac(cached("admin2", "ADMIN")).send());
        assertStatus(200, get("/rbac/session").rbac(cached("admin3", "ADMIN")).send());
    }

    @Test
    void everythingElseIsDeniedByDefault() throws Exception {
        assertStatus(403, get("/nope").rbac(user("plain2")).send());
        assertStatus(401, get("/nope").send());
    }

    @Test
    void homePageAndReadinessNeedNoLogin() throws Exception {
        assertStatus(200, get("/").send());
        assertStatus(200, get("/readyz").send());
    }

    // ---------------------------------------------------------------------------------------------------------------
    // Login flow
    // ---------------------------------------------------------------------------------------------------------------

    @Test
    void loginHandsOutACachedIdThatCanBeUsedAndRevoked() throws Exception {
        HttpResponse<byte[]> login = put("/rbac/session").rbac("Test alice:USER").send();
        assertStatus(204, login);
        assertEquals(0, login.body().length);
        String id = header(login, "Rbac");
        assertTrue(id.matches("^Cached [0-9a-f-]{36}$"), id);
        assertEquals("no-store", header(login, "Cache-Control"));
        assertStatus(200, get(DATA).rbac(id).send());

        HttpResponse<byte[]> again = put("/rbac/session").rbac(id).send();
        assertStatus(204, again);
        assertEquals(id, header(again, "Rbac"));

        HttpResponse<byte[]> info = get("/rbac/session").rbac(id).send();
        assertStatus(200, info);
        JsonObject session = json(info);
        assertEquals("alice", session.get("user").getAsString());
        for (String field : new String[]{"roles", "loginType", "tokenExpiry", "sessionExpiry"}) {
            assertTrue(session.has(field), field + " missing in " + session);
        }
        assertFalse(text(info).contains(id.substring("Cached ".length())), "the secret id must not be returned");

        assertStatus(204, delete("/rbac/session").rbac(id).send());
        assertStatus(401, get(DATA).rbac(id).send());
    }

    @Test
    void loginWithoutTheRequiredRoleIsForbiddenAndLeavesNoSession() throws Exception {
        assertStatus(403, put("/rbac/session").rbac("Test bob:OTHER").send());
        RbacSessionRegistry registry = context.getBean(RbacSessionRegistry.class);
        await("the unissued session to be dropped", () -> registry.sessionsOf("bob").isEmpty());
    }

    // ---------------------------------------------------------------------------------------------------------------
    // Data
    // ---------------------------------------------------------------------------------------------------------------

    @Test
    void getReturnsTheValueWithItsStamps() throws Exception {
        String rbac = user("reader1");
        for (String path : new String[]{DATA, "/api/DEV/Prop"}) {
            HttpResponse<byte[]> response = get(path).rbac(rbac).send();
            assertStatus(200, response);
            assertTrue(header(response, "Content-Type").startsWith("application/json"), header(response, "Content-Type"));
            assertEquals("111", header(response, "Acq-Stamp"));
            assertEquals("222", header(response, "Cycle-Stamp"));
            assertNull(header(response, "Rbac"), "data calls must not hand out the cached id");
            assertEquals(1.5, json(response).getAsJsonObject("number").get("value").getAsDouble());
        }
    }

    @Test
    void getCanSelectAFieldOrFields() throws Exception {
        String rbac = user("reader2");
        HttpResponse<byte[]> field = get(DATA + "/number").rbac(rbac).send();
        assertStatus(200, field);
        assertEquals(1.5, json(field).get("value").getAsDouble(), text(field));

        HttpResponse<byte[]> fields = get(DATA + "?fields=number").rbac(rbac).send();
        assertStatus(200, fields);
        assertTrue(json(fields).has("number"), text(fields));

        assertError(400, 5003, get(DATA + "?fields=nope").rbac(rbac));
    }

    @Test
    void getInLabViewFormatReturnsBinaryDataAndItsDescriptor() throws Exception {
        HttpResponse<byte[]> response = get(DATA).rbac(user("reader3")).accept(LABVIEW).send();
        assertStatus(200, response);
        assertTrue(header(response, "Content-Type").startsWith(LABVIEW), header(response, "Content-Type"));
        assertNotNull(header(response, LabVIEWFormat.TYPE_DESCRIPTOR_HEADER));
        assertEquals(ByteBuffer.wrap(doubleBytes(1.5)), ByteBuffer.wrap(response.body()));
    }

    @Test
    void setWritesTheValueToTheDevice() throws Exception {
        HttpResponse<byte[]> response = put("/api/v1/DEV2/Prop").rbac(user("writer1")).json(SET_JSON).send();
        assertStatus(200, response);
        assertEquals(0, response.body().length);
        assertSetNumber("DEV2/Prop", 2.5);
    }

    @Test
    void setRefusesMalformedOrUnsupportedBodies() throws Exception {
        String rbac = user("writer2");
        assertError(400, 5001, put("/api/v1/DEV2/Prop").rbac(rbac).json("{not json"));
        assertError(415, 5003, put("/api/v1/DEV2/Prop").rbac(rbac).body("text/plain", "hello".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void deviceOnlyRoutesNeedALabViewDescriptor() throws Exception {
        String rbac = user("device1");
        String descriptor = descriptorOf("Prop", "number");
        assertError(404, 5003, get("/api/v1/DEV").rbac(rbac));
        assertStatus(406, get("/api/v1/DEV").rbac(rbac).descriptor(descriptor).accept("application/json").send());
        assertStatus(415, put("/api/v1/DEV").rbac(rbac).descriptor(descriptor).json(SET_JSON).send());
    }

    @Test
    void setInLabViewFormatTakesThePropertyFromThePathOrTheDescriptor() throws Exception {
        String rbac = user("writer3");
        String descriptor = descriptorOf("Prop", "number");
        assertStatus(200, put("/api/v1/LV1/Prop").rbac(rbac).descriptor(descriptor).body(LABVIEW, doubleBytes(3.5)).send());
        assertSetNumber("LV1/Prop", 3.5);
        assertStatus(200, put("/api/v1/LV2").rbac(rbac).descriptor(descriptor).body(LABVIEW, doubleBytes(4.5)).send());
        assertSetNumber("LV2/Prop", 4.5);
    }

    @Test
    void setInLabViewFormatRefusesBadDescriptors() throws Exception {
        String rbac = user("writer4");
        String invalid = Base64.getUrlEncoder().encodeToString(new byte[]{0, 6, 0, 0x50, (byte) 0xFF, (byte) 0xFF});
        assertStatus(400, put("/api/v1/LV3/Prop").rbac(rbac).descriptor(invalid).body(LABVIEW, doubleBytes(1)).send());
        assertStatus(400, put("/api/v1/LV3/Prop").rbac(rbac).descriptor(descriptorOf("Other", "number")).body(LABVIEW, doubleBytes(1)).send());
        assertNull(setOn("LV3/Prop"));
    }

    @Test
    void invalidNamesAreABadRequest() throws Exception {
        assertError(400, 5003, get("/api/v1/DEV/Prop%23x").rbac(user("names1")));
    }

    @Test
    void deviceErrorsAreBadGatewayAndInternalErrorsAreHidden() throws Exception {
        String rbac = user("errors1");
        JsonObject error = assertError(502, 5000, get("/api/v1/MISSING/Prop").rbac(rbac));
        assertTrue(error.get("message").getAsString().contains("no such device"), error.toString());

        Call boom = get("/api/v1/BOOM/Prop").rbac(rbac);
        HttpResponse<byte[]> response = boom.send();
        error = assertError(500, 5004, boom, response);
        assertEquals("Internal error", error.get("message").getAsString());
        assertFalse(text(response).contains("secret"), text(response));
    }

    @Test
    void tooLargeBodiesAreRefused() throws Exception {
        byte[] body = new byte[1025];
        Arrays.fill(body, (byte) ' ');
        assertError(413, 5003, put("/api/v1/DEV2/Big").rbac(user("big1")).body("application/json", body));

        byte[] exact = new byte[1024];
        Arrays.fill(exact, (byte) ' ');
        byte[] json = SET_JSON.getBytes(StandardCharsets.UTF_8);
        System.arraycopy(json, 0, exact, 0, json.length);
        assertStatus(200, put("/api/v1/DEV2/Prop").rbac(user("big1")).body("application/json", exact).send());
    }

    @Test
    void requestsAboveTheRateLimitAreTurnedAway() throws Exception {
        String rbac = user("limited");
        List<HttpResponse<byte[]>> responses = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            responses.add(get(DATA).rbac(rbac).send());
        }
        HttpResponse<byte[]> limited = responses.stream().filter(r -> r.statusCode() == 429).findFirst()
                .orElseThrow(() -> new AssertionError("no 429 in " + responses.stream().map(HttpResponse::statusCode).toList()));
        assertEquals(5005, json(limited).get("code").getAsInt());
        assertNotNull(header(limited, "Retry-After"));
    }

    // ---------------------------------------------------------------------------------------------------------------
    // Admin
    // ---------------------------------------------------------------------------------------------------------------

    @Test
    void adminApiShowsTheServiceInfo() throws Exception {
        HttpResponse<byte[]> response = get("/admin/api/info").rbac(cached("admin4", "ADMIN")).send();
        assertStatus(200, response);
        assertEquals("USER", json(response).get("requiredRole").getAsString());
        assertEquals("ADMIN", json(response).get("adminRole").getAsString());
        assertNotNull(header(response, "Content-Security-Policy"));
    }

    @Test
    void adminCanListAndRevokeSessionsWithoutSeeingTheirSecrets() throws Exception {
        String admin = cached("admin5", "ADMIN");
        RbacSession victim = context.getBean(RbacSessionRegistry.class).register(TestSessions.of("victim", "USER"));
        String victimLogin = "Cached " + victim.id();
        assertStatus(200, get(DATA).rbac(victimLogin).send());

        HttpResponse<byte[]> list = get("/admin/api/sessions").rbac(admin).send();
        assertStatus(200, list);
        assertTrue(text(list).contains(victim.handle()), text(list));
        assertFalse(text(list).contains(victim.id()), "the secret id must not be listed");

        assertStatus(204, delete("/admin/api/sessions/" + victim.handle()).rbac(admin).send());
        assertStatus(401, get(DATA).rbac(victimLogin).send());
        assertError(404, 5003, delete("/admin/api/sessions/unknown").rbac(admin));
    }

    // ---------------------------------------------------------------------------------------------------------------
    // WebSocket
    // ---------------------------------------------------------------------------------------------------------------

    @Test
    void subscriptionSendsTheValueAndStopsWhenTheClientCloses() throws Exception {
        int stopsBefore = Stubs.STOPS.computeIfAbsent("DEV/Prop", key -> new AtomicInteger()).get();
        assertFirstFrameIsData("/ws/v1/DEV/Prop", user("subscriber1"));
        await("the subscription to stop", () -> Stubs.STOPS.get("DEV/Prop").get() > stopsBefore);
    }

    /**
     * Subscribes, checks that the first frame is the stub's data, and closes.
     */
    private static void assertFirstFrameIsData(String path, String rbac) throws Exception {
        CompletableFuture<String> first = new CompletableFuture<>();
        WebSocket socket = open(path, rbac, new WebSocket.Listener() {
            @Override
            public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                first.complete(data.toString());
                webSocket.request(1);
                return null;
            }
        });
        try {
            JsonObject frame = JsonParser.parseString(first.get(30, TimeUnit.SECONDS)).getAsJsonObject();
            assertEquals("data", frame.get("type").getAsString());
            assertEquals(111, frame.get("acqStamp").getAsLong());
            assertEquals(1.5, frame.getAsJsonObject("value").getAsJsonObject("number").get("value").getAsDouble());
        } finally {
            socket.sendClose(WebSocket.NORMAL_CLOSURE, "done");
            socket.abort();
        }
    }

    @Test
    void unversionedUrlsServeTheLatestVersionAndUnknownVersionsAreNotFound() throws Exception {
        String rbac = user("versions1");
        HttpResponse<byte[]> latest = get("/api/DEV/Prop").rbac(rbac).send();
        HttpResponse<byte[]> v1 = get(DATA).rbac(rbac).send();
        assertStatus(200, latest);
        assertStatus(200, v1);
        assertEquals(text(v1), text(latest));
        assertFirstFrameIsData("/ws/DEV/Prop", rbac);

        assertError(404, 5003, get("/api/v2/DEV/Prop").rbac(rbac));
        assertEquals(404, handshakeStatus("/ws/v2/DEV/Prop", rbac, "json"));
    }

    @Test
    void refusedHandshakesReportWhy() throws Exception {
        String rbac = user("subscriber2");
        assertEquals(400, handshakeStatus("/ws/v1/DEV/Prop", rbac, "xml"));
        assertEquals(404, handshakeStatus("/ws/v1/DEV", rbac, "json"));
        assertEquals(502, handshakeStatus("/ws/v1/MISSING/Prop", rbac, "json"));
    }
}
