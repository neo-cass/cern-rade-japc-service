# Graph Report - japc-new  (2026-11-16)

## Corpus Check
- Corpus is ~31,384 words - fits in a single context window. You may not need a graph.

## Summary
- 1745 nodes · 3764 edges · 98 communities (64 shown, 34 thin omitted)
- Extraction: 92% EXTRACTED · 8% INFERRED · 0% AMBIGUOUS · INFERRED: 283 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- Subscription Management
- LabVIEW Format & Get Service
- Admin Endpoints
- Endpoint Tests
- Set Service & Parameters
- Python Subscription Tests
- Security Configuration
- Kubernetes Deployment
- AsyncAPI Schemas
- Log4j Config (A)
- Error Handling & Logging
- Log4j Config (B)
- RBAC Session Registry
- JSON Format Converters
- OpenAPI Operations
- RBAC Gateway & Login Types
- RBAC Authentication
- Message Payload Schema
- Session View
- RBAC Credentials & Master Token
- HTTP Message Converters
- RBAC Login Service
- Error Codes
- Metrics & Rate Limiting
- Service Properties
- OpenShift Route (A)
- OpenShift Route (B)
- WebSocket Channel Bindings
- AsyncAPI Info & Close Codes
- GitLab CI Pipeline
- Request Interceptor
- RBAC Header Filter Tests
- Error Code & HTTP Status
- Subscription Handshake
- Prod Config
- MONIT Monitoring Docs
- AsyncAPI Components
- Rate Limiter & Stats
- OTLP Export Config
- Admin Page JS
- Client Context & Headers
- OIDC User Service
- LabVIEW Descriptor Channels
- Format Negotiation
- API Paths & Endpoints
- WebSocket Configurator
- Probes & Alerts
- Home Page & Alert Routing
- Deploy Environments
- Smoke Tests & CI Rules
- CI Test Job
- RBAC Filters
- RBAC Token Authorities
- Core Services & Parameter Names
- Docs & WAR Build
- Production Deploy
- OAuth2 Client Registration
- Publish WAR
- Debug Config
- Fixed Login & Servlet Output
- Server & SSL Config
- LabVIEW Data Messages
- Context Provider
- Payload Fields
- Frame Layout Schema
- Endpoint Test Reports
- PaaS CI Defaults
- RBAC Session Endpoint Tests
- Spring Profiles
- Tracing & OTLP Logging
- Admin Page UI
- Security Schemes
- JSON Examples
- JSON Error Examples
- CI Tool Variables
- Web Configuration
- Health Probes
- Mutable Clock
- LabVIEW Error Schema
- Format Configuration
- Rate Limiter Tests
- Springdoc Config
- Deployed Test Job
- Swagger UI Config
- Redocly Lint
- Actuator Exposure
- Metrics Export
- RBAC Login Scheme
- Session Attributes
- Deploy Success Page

## God Nodes (most connected - your core abstractions)
1. `RbacSession` - 98 edges
2. `RbacSessionRegistry` - 48 edges
3. `RbacProperties` - 42 edges
4. `RbacGateway` - 42 edges
5. `ErrorCode` - 31 edges
6. `LoginType` - 30 edges
7. `RbacSessionFactory` - 30 edges
8. `SubscriptionManager` - 30 edges
9. `EndpointTest` - 29 edges
10. `Format` - 27 edges

## Surprising Connections (you probably didn't know these)
- `Status indicator (/readyz)` --semantically_similar_to--> `Remote probe (down alert)`  [INFERRED] [semantically similar]
  src/main/resources/web/index.html → deploy/monit/README.md
- `REST API (GET and SET)` --semantically_similar_to--> `REST API (/api/v1)`  [INFERRED] [semantically similar]
  docs/index.html → src/main/resources/web/index.html
- `WebSocket API (subscriptions)` --semantically_similar_to--> `WebSocket API (/ws/v1)`  [INFERRED] [semantically similar]
  docs/index.html → src/main/resources/web/index.html
- `rade-admins e-group` --conceptually_related_to--> `MONIT tenant rade`  [INFERRED]
  src/main/resources/web/index.html → deploy/monit/README.md
- `Clients tab (requests per RBAC user)` --conceptually_related_to--> `Data source: MONIT metrics`  [INFERRED]
  src/main/resources/web/admin/index.html → deploy/monit/README.md

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **MONIT observability pipeline (OTLP export, data sources, alert rules)** — deploy_monit_readme_otlp_export, deploy_monit_readme_monit_tenant_rade, deploy_monit_readme_ds_monit_metrics, deploy_monit_readme_ds_monit_logs, deploy_monit_readme_alert_rules, deploy_monit_readme_snow_alert_routing [EXTRACTED 0.95]
- **Admin panel tabs** — src_main_resources_web_admin_index_service_info_tab, src_main_resources_web_admin_index_rbac_sessions_tab, src_main_resources_web_admin_index_subscriptions_tab, src_main_resources_web_admin_index_clients_tab [EXTRACTED 1.00]

## Communities (98 total, 34 thin omitted)

### Community 0 - "Subscription Management"
Cohesion: 0.05
Nodes (5): SubscriptionManager, SubscriptionService, Latest, Update, SubscriptionWebSocketHandler

### Community 1 - "LabVIEW Format & Get Service"
Cohesion: 0.06
Nodes (10): LabVIEWBody, LabVIEWFormat, LabVIEWValue, GetEndpoint, GetService, LVDescriptorGetEndpoint, PropertyGetEndpoint, LabVIEWBodyTest (+2 more)

### Community 2 - "Admin Endpoints"
Cohesion: 0.07
Nodes (10): AdminEndpoint, AdminService, MeView, ServiceInfoView, SessionView, LVDescriptorFallbackEndpoint, RbacSessionEndpoint, ClientRequests (+2 more)

### Community 3 - "Endpoint Tests"
Cohesion: 0.09
Nodes (5): EndpointTest, WebSocket.Listener, RbacCredentialsTest, RbacLoginServiceTest, RbacPropertiesTest

### Community 4 - "Set Service & Parameters"
Cohesion: 0.09
Nodes (9): ParameterProvider, LVDescriptorSetEndpoint, PropertySetEndpoint, SetEndpoint, SetService, ValueSource, SetServiceTest, Call (+1 more)

### Community 5 - "Python Subscription Tests"
Cohesion: 0.08
Nodes (13): describe_rejection(), field_value(), load_rbac_header(), main(), receive_data(), rest(), rest_get(), Result (+5 more)

### Community 6 - "Security Configuration"
Cohesion: 0.11
Nodes (5): LocalRbacTokenSupport, RbacHeaderFilter, RbacTokenSupport, SecurityConfiguration, SecurityErrorWriter

### Community 7 - "Kubernetes Deployment"
Cohesion: 0.06
Nodes (35): env, image, livenessProbe, name, ports, readinessProbe, resources, startupProbe (+27 more)

### Community 8 - "AsyncAPI Schemas"
Cohesion: 0.06
Nodes (34): description, format, type, schemas, description, format, type, description (+26 more)

### Community 9 - "Log4j Config (A)"
Cohesion: 0.06
Nodes (33): ref, Console, OpenTelemetry, RollingFile, Configuration, Appenders, Loggers, properties (+25 more)

### Community 10 - "Error Handling & Logging"
Cohesion: 0.10
Nodes (3): ErrorHandler, ErrorLog, InitialiseService

### Community 11 - "Log4j Config (B)"
Cohesion: 0.07
Nodes (30): ref, Console, RollingFile, Configuration, Appenders, Loggers, properties, status (+22 more)

### Community 14 - "OpenAPI Operations"
Cohesion: 0.08
Nodes (28): $ref, default, description, operations, sendField, sendLabviewDescriptor, sendProperty, $ref (+20 more)

### Community 15 - "RBAC Gateway & Login Types"
Cohesion: 0.20
Nodes (11): RbacGateway, KerberosLogin, MasterTokenLogin, OauthLogin, LoginType, EXPLICIT, KERBEROS, OAUTH (+3 more)

### Community 16 - "RBAC Authentication"
Cohesion: 0.13
Nodes (3): RbacAuthentication, RbacOidcUser, RbacSession

### Community 17 - "Message Payload Schema"
Cohesion: 0.08
Nodes (26): $ref, $ref, $ref, payload, payload, type, properties, required (+18 more)

### Community 21 - "RBAC Login Service"
Cohesion: 0.15
Nodes (5): CachedLogin, RbacLogin, RbacLoginService, TokenLogin, RbacConfiguration

### Community 22 - "Error Codes"
Cohesion: 0.16
Nodes (7): ErrorCode, BAD_REQUEST, CODEC_ERROR, INTERNAL_ERROR, JAPC_ERROR, RATE_LIMITED, RBAC_ERROR

### Community 25 - "OpenShift Route (A)"
Cohesion: 0.09
Nodes (21): haproxy.router.openshift.io/rate-limit-connections, haproxy.router.openshift.io/rate-limit-connections.concurrent-tcp, haproxy.router.openshift.io/rate-limit-connections.rate-http, router.cern.ch/network-visibility, apiVersion, kind, app, metadata (+13 more)

### Community 26 - "OpenShift Route (B)"
Cohesion: 0.10
Nodes (20): haproxy.router.openshift.io/rate-limit-connections, haproxy.router.openshift.io/rate-limit-connections.concurrent-tcp, haproxy.router.openshift.io/rate-limit-connections.rate-http, router.cern.ch/network-visibility, apiVersion, kind, app, metadata (+12 more)

### Community 28 - "WebSocket Channel Bindings"
Cohesion: 0.14
Nodes (20): labviewDescriptor, withFields, withoutFields, channelBindings, properties, required, type, ws (+12 more)

### Community 29 - "AsyncAPI Info & Close Codes"
Cohesion: 0.10
Nodes (20): info, description, title, version, x-close-codes, x-first-update-timeout-ms, x-idle-timeout-ms, x-ping-interval-ms (+12 more)

### Community 30 - "GitLab CI Pipeline"
Cohesion: 0.11
Nodes (19): image, build_image, image, script, stage, tags, .gradle_job, before_script (+11 more)

### Community 31 - "Request Interceptor"
Cohesion: 0.14
Nodes (7): RateLimitedException, Client, Operation, GET, SET, SUBSCRIBE, RequestInterceptor

### Community 32 - "RBAC Header Filter Tests"
Cohesion: 0.20
Nodes (3): RbacHeaderFilterTest, CapturingChain, StubResponse

### Community 35 - "Prod Config"
Cohesion: 0.11
Nodes (18): cern.cmw.directory.http.client.impl.feign.FeignDecoder, cern.cmw.rda3.impl.client.subscription.SubscriptionControllerImpl, cern.cmw.rda3.impl.transport.client.ClientDispatcher, cern.cmw.rda3.impl.transport.zeromq.ZMQAdapter, com.netflix.config.sources.URLConfigurationSource, org.springframework.boot.autoconfigure, logging, config (+10 more)

### Community 36 - "MONIT Monitoring Docs"
Cohesion: 0.15
Nodes (18): Grafana alert rules (app not reporting, CPU, heap, rate limited, dropped updates, errors, pod restart), Data source: MONIT logs (OpenSearch), Data source: MONIT metrics, Data source: PaaS metrics, Grafana organization ticket, JAPC Grafana dashboard (deploy/grafana/japc-dashboard.json), JAPC service metrics (japc_*), MONIT Grafana (monit-grafana.cern.ch) (+10 more)

### Community 37 - "AsyncAPI Components"
Cohesion: 0.13
Nodes (18): components, messages, parameters, description, examples, $ref, description, examples (+10 more)

### Community 38 - "Rate Limiter & Stats"
Cohesion: 0.18
Nodes (3): RateLimiter, RateLimitProperties, StatsConfiguration

### Community 39 - "OTLP Export Config"
Cohesion: 0.12
Nodes (17): enabled, headers, otlp, step, url, Authorization, export, management (+9 more)

### Community 40 - "Admin Page JS"
Cohesion: 0.27
Nodes (17): act(), button(), csrfToken(), duration(), element(), loadClients(), loadInfo(), loadMe() (+9 more)

### Community 44 - "LabVIEW Descriptor Channels"
Cohesion: 0.12
Nodes (16): asyncapi, $ref, channels, labviewDescriptor, property, bindings, address, bindings (+8 more)

### Community 48 - "Probes & Alerts"
Cohesion: 0.13
Nodes (14): certificate_expiration, days, http_ipv4, targets, snow_auto_closing, snow_functional_element, alert_options, enabled_alerts (+6 more)

### Community 49 - "Home Page & Alert Routing"
Cohesion: 0.14
Nodes (15): Alert contact point monit-alarms, Alert routing to SNOW LabVIEW Support, RADE JAPC API docs landing page, asyncapi.yaml (AsyncAPI 3.1), openapi.yaml (OpenAPI 3.1), REST API (GET and SET), WebSocket API (subscriptions), RADE JAPC service home page (+7 more)

### Community 50 - "Deploy Environments"
Cohesion: 0.14
Nodes (15): environment, deploy_review, environment, extends, rules, deploy_staging, environment, extends (+7 more)

### Community 51 - "Smoke Tests & CI Rules"
Cohesion: 0.15
Nodes (15): rules, if, when, smoke_production, extends, rules, script, stage (+7 more)

### Community 52 - "CI Test Job"
Cohesion: 0.13
Nodes (15): alias, command, entrypoint, name, test_war, after_script, extends, needs (+7 more)

### Community 54 - "RBAC Token Authorities"
Cohesion: 0.22
Nodes (3): Action, TokenRenewer, VoidAction

### Community 56 - "Docs & WAR Build"
Cohesion: 0.14
Nodes (14): name, paths, build_docs, artifacts, script, stage, tags, variables (+6 more)

### Community 57 - "Production Deploy"
Cohesion: 0.15
Nodes (13): .deploy, environment, extends, deploy_production, extends, resource_group, script, variables (+5 more)

### Community 58 - "OAuth2 Client Registration"
Cohesion: 0.15
Nodes (12): client-id, client-secret, issuer-uri, scope, provider, registration, client, cern (+4 more)

### Community 59 - "Publish WAR"
Cohesion: 0.21
Nodes (12): needs, artifacts, job, optional, publish_war, extends, needs, rules (+4 more)

### Community 60 - "Debug Config"
Cohesion: 0.17
Nodes (11): logging, config, admin-role, required-role, service-properties, firstUpdateTimeout, pingToClientFrequency, rbac (+3 more)

### Community 63 - "Server & SSL Config"
Cohesion: 0.18
Nodes (11): server, yaml, server, forward-headers-strategy, mime-mappings, port, servlet, ssl (+3 more)

### Community 65 - "LabVIEW Data Messages"
Cohesion: 0.20
Nodes (10): field, address, messages, title, contentType, description, name, $ref (+2 more)

### Community 68 - "Payload Fields"
Cohesion: 0.25
Nodes (8): payload, acqStamp, code, cycleStamp, message, source, status, value

### Community 69 - "Frame Layout Schema"
Cohesion: 0.32
Nodes (8): payload, payload, format, type, x-frame-layout, offset, type, value

### Community 70 - "Endpoint Test Reports"
Cohesion: 0.25
Nodes (8): reports, when, artifacts, .endpoint_tests, artifacts, extends, dotenv, junit

### Community 71 - "PaaS CI Defaults"
Cohesion: 0.25
Nodes (8): .paas, before_script, image, stage, tags, variables, PROJECT, SERVER

### Community 73 - "Spring Profiles"
Cohesion: 0.25
Nodes (8): name, active, add-mappings, spring, application, profiles, web, resources

### Community 74 - "Tracing & OTLP Logging"
Cohesion: 0.29
Nodes (8): otlp, export, management, logging, otlp, tracing, enabled, export

### Community 75 - "Admin Page UI"
Cohesion: 0.25
Nodes (8): RADE JAPC admin page, admin.css, common.css, RBAC sessions tab (cached logins, revoke), Service info tab, Subscriptions tab, rade-services-japc-users e-group, RBAC access flow (PUT /rbac/session, Rbac headers)

### Community 76 - "Security Schemes"
Cohesion: 0.29
Nodes (7): securitySchemes, description, in, name, type, x-rbac-schemes, Rbac

### Community 77 - "JSON Examples"
Cohesion: 0.29
Nodes (7): contentType, description, examples, name, $ref, title, jsonData

### Community 78 - "JSON Error Examples"
Cohesion: 0.29
Nodes (7): contentType, description, examples, name, $ref, title, jsonError

### Community 79 - "CI Tool Variables"
Cohesion: 0.29
Nodes (7): variables, ASYNCAPI_CLI, ASYNCAPI_HTML_TEMPLATE, CMW_TOOLS_IMAGE, PROD_APP_NAME, REDOCLY_CLI, WAR_NAME

### Community 81 - "Health Probes"
Cohesion: 0.29
Nodes (7): health, readiness, group, probes, endpoint, enabled, additional-path

### Community 83 - "LabVIEW Error Schema"
Cohesion: 0.33
Nodes (6): contentType, description, name, $ref, title, labviewError

### Community 86 - "Springdoc Config"
Cohesion: 0.33
Nodes (5): org.springframework.web.servlet.PageNotFound, logging, level, springdoc, paths-to-exclude

### Community 87 - "Deployed Test Job"
Cohesion: 0.40
Nodes (5): test_deployed, extends, rules, script, stage

### Community 88 - "Swagger UI Config"
Cohesion: 0.40
Nodes (5): swagger-ui, path, show-common-extensions, show-extensions, url

### Community 90 - "Redocly Lint"
Cohesion: 0.50
Nodes (3): extends, rules, info-license

### Community 91 - "Actuator Exposure"
Cohesion: 0.50
Nodes (4): web, include, endpoints, exposure

### Community 92 - "Metrics Export"
Cohesion: 0.50
Nodes (4): base-time-unit, enabled, export, metrics

## Knowledge Gaps
- **29 isolated node(s):** `JAPC_ERROR`, `CODEC_ERROR`, `RBAC_ERROR`, `BAD_REQUEST`, `INTERNAL_ERROR` (+24 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 536 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **34 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `RbacSession` connect `Session View` to `Subscription Management`, `LabVIEW Format & Get Service`, `Admin Endpoints`, `Endpoint Tests`, `Set Service & Parameters`, `RBAC Session Registry`, `RBAC Gateway & Login Types`, `RBAC Authentication`, `RBAC Credentials & Master Token`, `RBAC Login Service`, `Metrics & Rate Limiting`, `RBAC Header Filter Tests`, `RBAC Session Tests`, `Client Context & Headers`, `OIDC User Service`, `RBAC Filters`, `RBAC Token Authorities`, `Core Services & Parameter Names`, `Fixed Login & Servlet Output`, `Local RBAC Token Support`, `RBAC Session Endpoint Tests`, `RBAC Login Scheme`?**
  _High betweenness centrality (0.042) - this node is a cross-community bridge._
- **Are the 4 inferred relationships involving `RbacProperties` (e.g. with `.capsLifetimesAtRbacMaxima()` and `.keepsLifetimesWithinMaxima()`) actually correct?**
  _`RbacProperties` has 4 INFERRED edges - model-reasoned connections that need verification._
- **What connects `JAPC_ERROR`, `CODEC_ERROR`, `RBAC_ERROR` to the rest of the system?**
  _29 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Subscription Management` be split into smaller, more focused modules?**
  _Cohesion score 0.05009009009009009 - nodes in this community are weakly interconnected._
- **Why does `RbacProperties` connect `RBAC Gateway & Login Types` to `RBAC Header Filter Tests`, `Admin Endpoints`, `Endpoint Tests`, `Security Configuration`, `RBAC Session Endpoint Tests`, `RBAC Session Tests`, `Error Handling & Logging`, `Client Context & Headers`, `RBAC Session Registry`, `RBAC Login Service`, `Service Properties`?**
  _High betweenness centrality (0.026) - this node is a cross-community bridge._
- **Should `LabVIEW Format & Get Service` be split into smaller, more focused modules?**
  _Cohesion score 0.05829420970266041 - nodes in this community are weakly interconnected._
- **Why does `RbacSessionRegistry` connect `RBAC Session Registry` to `RBAC Header Filter Tests`, `Admin Endpoints`, `Endpoint Tests`, `Security Configuration`, `Rate Limiter & Stats`, `RBAC Session Endpoint Tests`, `RBAC Session Tests`, `Error Handling & Logging`, `Client Context & Headers`, `RBAC Gateway & Login Types`, `Session View`, `RBAC Login Service`, `RBAC Filters`, `Metrics & Rate Limiting`?**
  _High betweenness centrality (0.021) - this node is a cross-community bridge._