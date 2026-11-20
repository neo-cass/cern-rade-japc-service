# Graph Report - japc-new  (2026-11-20)

## Corpus Check
- 145 files · ~50,610 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 15 file(s) not represented in the graph (top: (none) 5, .properties 2, .css 2)

## Summary
- 2086 nodes · 4620 edges · 152 communities (82 shown, 70 thin omitted)
- Extraction: 90% EXTRACTED · 10% INFERRED · 0% AMBIGUOUS · INFERRED: 442 edges (avg confidence: 0.86)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `cf85c971`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- org.springframework.context.annotation.Bean
- cern.rade.services.common.containers.labview.LVTypeDescriptor
- org.springframework.http.ResponseEntity
- EndpointTest
- security.md
- Python Subscription Tests
- StatsConfiguration.java
- Kubernetes Deployment
- schemas
- Log4j Config (A)
- SecurityConfiguration.java
- Log4j Config (B)
- RbacSession
- RbacLogin
- OpenAPI Operations
- jakarta.servlet.http.HttpServletRequest
- RbacSession
- Message Payload Schema
- SubscriptionHandshakeInterceptor.java
- AdminService
- RbacProperties
- SubscriptionManager
- .negotiate
- RateLimiter
- org.springframework.http.MediaType
- OpenShift Route (A)
- OpenShift Route (B)
- org.slf4j.Logger
- WebSocket Channel Bindings
- info
- GitLab CI Pipeline
- org.springframework.http.HttpHeaders
- org.junit.jupiter.api.Test
- TokenValidationCache
- Prod Config
- admin.js
- AsyncAPI Components
- cern.japc.value.ParameterValue
- OTLP Export Config
- ParameterAddress
- SubscriptionService
- Stubs
- LabVIEW Descriptor Channels
- Monitoring links
- RbacOidcUserService.java
- Deploy Environments
- Smoke Tests & CI Rules
- CI Test Job
- SubscriptionWebSocketHandler
- MONIT tenant rade
- cern.rade.services.common.containers.labview.LVTypeDescriptor
- Docs & WAR Build
- Production Deploy
- OAuth2 Client Registration
- Publish WAR
- Debug Config
- RbacSession
- RbacHeaderFilterTest
- Server & SSL Config
- SubscriptionProperties
- Override
- Payload Fields
- Frame Layout Schema
- Endpoint Test Reports
- PaaS CI Defaults
- cern.rbac.common.MasterToken
- Spring Profiles
- Tracing & OTLP Logging
- CERN SSO for the admin panel
- jsonData
- RbacSession.java
- targets
- CI Tool Variables
- Remote probe (down alert)
- Health Probes
- CLAUDE.md
- Forms to fill in
- RADE JAPC service
- Springdoc Config
- Deployed Test Job
- Swagger UI Config
- Redocly Lint
- Actuator Exposure
- Metrics Export
- Session Attributes
- Format
- RADE JAPC service home page
- .of
- .login
- Update
- ParameterProvider
- ParameterServiceTest
- README.md
- 5. State in memory, one replica
- cern.rade.services.japc.rbac.RbacSessionFactory
- RbacHeaderFilter.java
- labviewError
- ParameterEndpoint.java
- 1. RBAC login through the `Rbac` header and cached sessions
- 6. API versions in the path, unversioned prefix means latest
- 4. One error model for REST and WebSocket
- RADE JAPC admin page
- x-close-codes
- SubscriptionWebSocketConfigurator.java
- JSONFormat
- Configuration reference
- Security
- labviewData
- RateLimitProperties
- value
- org.springframework.context.annotation.Configuration

## God Nodes (most connected - your core abstractions)
1. `RbacSession` - 62 edges
2. `ApiContractTest` - 56 edges
3. `RbacProperties` - 41 edges
4. `EndpointTest` - 34 edges
5. `LoginType` - 30 edges
6. `ParameterAddress` - 29 edges
7. `RbacHeaderFilterTest` - 28 edges
8. `RbacGateway` - 28 edges
9. `RbacSessionRegistry` - 28 edges
10. `SubscriptionService` - 26 edges

## Surprising Connections (you probably didn't know these)
- `Tests` --references--> `EndpointTest`  [INFERRED]
  docs/configuration.md → src/test/java/cern/rade/services/japc/EndpointTest.java
- `Not required for SSO itself` --references--> `InactiveSessionFilter`  [INFERRED]
  docs/sso_setup.md → src/main/java/cern/rade/services/japc/security/InactiveSessionFilter.java
- `RBAC: `service-properties.rbac` (`RbacProperties`)` --references--> `RbacProperties`  [INFERRED]
  docs/configuration.md → src/main/java/cern/rade/services/japc/rbac/RbacProperties.java
- `Subscriptions: `service-properties.subscription` (`SubscriptionProperties`)` --references--> `SubscriptionProperties`  [INFERRED]
  docs/configuration.md → src/main/java/cern/rade/services/japc/subscription/SubscriptionProperties.java
- `Rate limits: `service-properties.rate-limit` (`RateLimitProperties`)` --references--> `RateLimitProperties`  [INFERRED]
  docs/configuration.md → src/main/java/cern/rade/services/japc/ratelimit/RateLimitProperties.java

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **MONIT observability pipeline (OTLP export, data sources, alert rules)** — deploy_monit_readme_otlp_export, deploy_monit_readme_monit_tenant_rade, deploy_monit_readme_ds_monit_metrics, deploy_monit_readme_ds_monit_logs, deploy_monit_readme_alert_rules, deploy_monit_readme_snow_alert_routing [EXTRACTED 0.95]
- **Admin panel tabs** — src_main_resources_web_admin_index_service_info_tab, src_main_resources_web_admin_index_rbac_sessions_tab, src_main_resources_web_admin_index_subscriptions_tab, src_main_resources_web_admin_index_clients_tab [EXTRACTED 1.00]

## Communities (152 total, 70 thin omitted)

### Community 3 - "EndpointTest"
Cohesion: 0.13
Nodes (9): Health, Observing, Operations, Recurring maintenance, Release, Runbook, Where it runs, EndpointTest (+1 more)

### Community 4 - "security.md"
Cohesion: 0.36
Nodes (4): 7. `Rbac: Token` is stateless, Consequences, Context, Decision

### Community 5 - "Python Subscription Tests"
Cohesion: 0.08
Nodes (13): describe_rejection(), field_value(), load_rbac_header(), main(), receive_data(), rest(), rest_get(), Result (+5 more)

### Community 7 - "Kubernetes Deployment"
Cohesion: 0.06
Nodes (35): env, image, livenessProbe, name, ports, readinessProbe, resources, startupProbe (+27 more)

### Community 8 - "schemas"
Cohesion: 0.06
Nodes (34): description, format, type, schemas, description, format, type, description (+26 more)

### Community 9 - "Log4j Config (A)"
Cohesion: 0.06
Nodes (33): ref, Console, OpenTelemetry, RollingFile, Configuration, Appenders, Loggers, properties (+25 more)

### Community 11 - "Log4j Config (B)"
Cohesion: 0.07
Nodes (30): ref, Console, RollingFile, Configuration, Appenders, Loggers, properties, status (+22 more)

### Community 12 - "RbacSession"
Cohesion: 0.05
Nodes (16): 1. Route, 2. Service method, 5. Tests, 6. Specification and documentation, Steps, MeView, CachedLogin, RbacHeaders (+8 more)

### Community 14 - "OpenAPI Operations"
Cohesion: 0.08
Nodes (28): $ref, default, description, operations, sendField, sendLabviewDescriptor, sendProperty, $ref (+20 more)

### Community 15 - "jakarta.servlet.http.HttpServletRequest"
Cohesion: 0.11
Nodes (10): RateLimitedException, Client, ClientRequests, Operation, GET, SET, SUBSCRIBE, RequestInterceptor (+2 more)

### Community 17 - "Message Payload Schema"
Cohesion: 0.10
Nodes (21): $ref, $ref, $ref, payload, payload, type, properties, required (+13 more)

### Community 19 - "AdminService"
Cohesion: 0.13
Nodes (5): AdminService, ServiceInfoView, SessionView, SubscriptionInfo, SubscriptionRegistry

### Community 20 - "RbacProperties"
Cohesion: 0.14
Nodes (12): RbacGateway, KerberosLogin, MasterTokenLogin, OauthLogin, LoginType, EXPLICIT, KERBEROS, OAUTH (+4 more)

### Community 25 - "OpenShift Route (A)"
Cohesion: 0.09
Nodes (21): haproxy.router.openshift.io/rate-limit-connections, haproxy.router.openshift.io/rate-limit-connections.concurrent-tcp, haproxy.router.openshift.io/rate-limit-connections.rate-http, router.cern.ch/network-visibility, apiVersion, kind, app, metadata (+13 more)

### Community 26 - "OpenShift Route (B)"
Cohesion: 0.10
Nodes (20): haproxy.router.openshift.io/rate-limit-connections, haproxy.router.openshift.io/rate-limit-connections.concurrent-tcp, haproxy.router.openshift.io/rate-limit-connections.rate-http, router.cern.ch/network-visibility, apiVersion, kind, app, metadata (+12 more)

### Community 28 - "WebSocket Channel Bindings"
Cohesion: 0.14
Nodes (20): labviewDescriptor, withFields, withoutFields, channelBindings, properties, required, type, ws (+12 more)

### Community 29 - "info"
Cohesion: 0.20
Nodes (10): info, description, title, version, x-first-update-timeout-ms, x-idle-timeout-ms, x-ping-interval-ms, x-queue (+2 more)

### Community 30 - "GitLab CI Pipeline"
Cohesion: 0.11
Nodes (19): image, build_image, image, script, stage, tags, .gradle_job, before_script (+11 more)

### Community 32 - "org.junit.jupiter.api.Test"
Cohesion: 0.06
Nodes (9): When you change code, Fields, ApiContractTest, Call, WebSocket.Listener, ApiPathsTest, FormatConfigurationTest, FieldsTest (+1 more)

### Community 34 - "TokenValidationCache"
Cohesion: 0.22
Nodes (3): TokenValidationCache, MutableClock, TokenValidationCacheTest

### Community 35 - "Prod Config"
Cohesion: 0.14
Nodes (13): cern.cmw.directory.http.client.impl.feign.FeignDecoder, cern.cmw.rda3.impl.client.subscription.SubscriptionControllerImpl, cern.cmw.rda3.impl.transport.client.ClientDispatcher, cern.cmw.rda3.impl.transport.zeromq.ZMQAdapter, com.netflix.config.sources.URLConfigurationSource, org.springframework.boot.autoconfigure, logging, config (+5 more)

### Community 36 - "admin.js"
Cohesion: 0.25
Nodes (19): act(), button(), collapsedUsers, csrfToken(), duration(), element(), loadClients(), loadInfo() (+11 more)

### Community 37 - "AsyncAPI Components"
Cohesion: 0.16
Nodes (15): parameters, description, examples, $ref, description, examples, parameters, $ref (+7 more)

### Community 38 - "cern.japc.value.ParameterValue"
Cohesion: 0.12
Nodes (3): 3. Controller method, NamedValue, ValueSource

### Community 39 - "OTLP Export Config"
Cohesion: 0.12
Nodes (17): enabled, headers, otlp, step, url, Authorization, export, management (+9 more)

### Community 40 - "ParameterAddress"
Cohesion: 0.19
Nodes (3): What protects what, ParameterAddress, ParameterAddressTest

### Community 43 - "Stubs"
Cohesion: 0.09
Nodes (7): WebSocket.Listener, Stubs, Handler, Set, RbacTokenSupport, RbacLogin, RbacSession

### Community 44 - "LabVIEW Descriptor Channels"
Cohesion: 0.13
Nodes (15): asyncapi, $ref, channels, field, property, address, bindings, messages (+7 more)

### Community 45 - "Monitoring links"
Cohesion: 0.25
Nodes (8): Alerts, Dashboards, Help, Logs, Metrics, Monitoring links, Probe, Service and cluster

### Community 49 - "RbacOidcUserService.java"
Cohesion: 0.15
Nodes (3): ClientContext, RbacTokenSupport, RbacOidcUserService

### Community 50 - "Deploy Environments"
Cohesion: 0.14
Nodes (15): environment, deploy_review, environment, extends, rules, deploy_staging, environment, extends (+7 more)

### Community 51 - "Smoke Tests & CI Rules"
Cohesion: 0.15
Nodes (15): rules, if, when, smoke_production, extends, rules, script, stage (+7 more)

### Community 52 - "CI Test Job"
Cohesion: 0.13
Nodes (15): alias, command, entrypoint, name, test_war, after_script, extends, needs (+7 more)

### Community 54 - "MONIT tenant rade"
Cohesion: 0.19
Nodes (15): Grafana alert rules (app not reporting, CPU, heap, rate limited, dropped updates, errors, pod restart), Data source: MONIT logs (OpenSearch), Data source: MONIT metrics, Data source: PaaS metrics, Grafana organization ticket, JAPC Grafana dashboard (deploy/grafana/japc-dashboard.json), JAPC service metrics (japc_*), MONIT Grafana (monit-grafana.cern.ch) (+7 more)

### Community 55 - "cern.rade.services.common.containers.labview.LVTypeDescriptor"
Cohesion: 0.25
Nodes (3): LabVIEWBody, LabVIEWValue, LabVIEWBodyTest

### Community 56 - "Docs & WAR Build"
Cohesion: 0.14
Nodes (14): name, paths, build_docs, artifacts, script, stage, tags, variables (+6 more)

### Community 57 - "Production Deploy"
Cohesion: 0.15
Nodes (13): .deploy, environment, extends, deploy_production, extends, resource_group, script, variables (+5 more)

### Community 58 - "OAuth2 Client Registration"
Cohesion: 0.11
Nodes (18): client-id, client-secret, issuer-uri, scope, provider, registration, same-site, secure (+10 more)

### Community 59 - "Publish WAR"
Cohesion: 0.21
Nodes (12): needs, artifacts, job, optional, publish_war, extends, needs, rules (+4 more)

### Community 60 - "Debug Config"
Cohesion: 0.29
Nodes (6): logging, config, admin-role, required-role, service-properties, rbac

### Community 61 - "RbacSession"
Cohesion: 0.28
Nodes (3): RbacLogin, RbacSession, CapturingChain

### Community 63 - "Server & SSL Config"
Cohesion: 0.18
Nodes (11): server, yaml, server, forward-headers-strategy, mime-mappings, port, servlet, ssl (+3 more)

### Community 64 - "SubscriptionProperties"
Cohesion: 0.23
Nodes (6): 3. Stateless singleton services, constructor injection, typed configuration, Consequences, Context, Decision, SubscriptionProperties, SubscriptionPropertiesTest

### Community 65 - "Override"
Cohesion: 0.17
Nodes (3): FailingLogin, FixedLogin, ServletOutputStream

### Community 68 - "Payload Fields"
Cohesion: 0.13
Nodes (15): payload, contentType, description, examples, name, $ref, title, jsonError (+7 more)

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

### Community 75 - "CERN SSO for the admin panel"
Cohesion: 0.15
Nodes (12): CERN SSO for the admin panel, Checking it, Configuration, Dependencies (`build.gradle.kts`), How it works, Minimum code, Not required for SSO itself, References (+4 more)

### Community 76 - "jsonData"
Cohesion: 0.12
Nodes (16): components, messages, securitySchemes, contentType, description, examples, name, $ref (+8 more)

### Community 78 - "targets"
Cohesion: 0.17
Nodes (12): certificate_expiration, days, targets, monit_shared, snow_assignment_level, snow_auto_closing, snow_functional_element, snow_watchlist (+4 more)

### Community 79 - "CI Tool Variables"
Cohesion: 0.29
Nodes (7): variables, ASYNCAPI_CLI, ASYNCAPI_HTML_TEMPLATE, CMW_TOOLS_IMAGE, PROD_APP_NAME, REDOCLY_CLI, WAR_NAME

### Community 80 - "Remote probe (down alert)"
Cohesion: 0.40
Nodes (4): http_ipv4, monit-remote-probes repository, Remote probe (down alert), Status indicator (/readyz)

### Community 81 - "Health Probes"
Cohesion: 0.29
Nodes (7): health, readiness, group, probes, endpoint, enabled, additional-path

### Community 84 - "Forms to fill in"
Cohesion: 0.29
Nodes (7): 1. Application Portal: add the application, 2. Application Portal: CERN SSO Registration (OIDC), 3. Application Portal: token exchange permission for RBAC, 4. Application Portal: roles (optional), 5. OpenShift: secret with the client id and secret, 6. Access for a new admin (Groups Portal), Forms to fill in

### Community 85 - "RADE JAPC service"
Cohesion: 0.33
Nodes (6): Documentation, Quick start, RADE JAPC service, Rules for changes, Support, Where things are

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

### Community 95 - "Format"
Cohesion: 0.27
Nodes (5): 2. JSON and LabVIEW as interchangeable formats, Consequences, Context, Decision, Format

### Community 98 - "RADE JAPC service home page"
Cohesion: 0.14
Nodes (15): Alert contact point monit-alarms, Alert routing to SNOW LabVIEW Support, RADE JAPC API docs landing page, asyncapi.yaml (AsyncAPI 3.1), openapi.yaml (OpenAPI 3.1), REST API (GET and SET), WebSocket API (subscriptions), RADE JAPC service home page (+7 more)

### Community 99 - ".of"
Cohesion: 0.16
Nodes (10): 4. A new error, ErrorCode, BAD_REQUEST, CODEC_ERROR, INTERNAL_ERROR, JAPC_ERROR, RATE_LIMITED, RBAC_ERROR (+2 more)

### Community 102 - "ParameterProvider"
Cohesion: 0.16
Nodes (10): A GET or SET request, A subscription, Architecture, External dependencies, Login, Packages, State, Two filter chains (+2 more)

### Community 103 - "ParameterServiceTest"
Cohesion: 0.32
Nodes (3): ParameterServiceTest, Call, StubParameter

### Community 105 - "5. State in memory, one replica"
Cohesion: 0.40
Nodes (4): 5. State in memory, one replica, Consequences, Context, Decision

### Community 107 - "RbacHeaderFilter.java"
Cohesion: 0.22
Nodes (3): RbacLoginService, InactiveSessionFilter, RbacHeaderFilter

### Community 108 - "labviewError"
Cohesion: 0.17
Nodes (12): labviewDescriptor, address, description, messages, parameters, title, contentType, description (+4 more)

### Community 109 - "ParameterEndpoint.java"
Cohesion: 0.09
Nodes (6): Adding an API version, Before you start, Checklist, How to add an endpoint, ParameterEndpoint, ParameterService

### Community 110 - "1. RBAC login through the `Rbac` header and cached sessions"
Cohesion: 0.50
Nodes (4): 1. RBAC login through the `Rbac` header and cached sessions, Consequences, Context, Decision

### Community 111 - "6. API versions in the path, unversioned prefix means latest"
Cohesion: 0.33
Nodes (4): 6. API versions in the path, unversioned prefix means latest, Consequences, Context, Decision

### Community 114 - "4. One error model for REST and WebSocket"
Cohesion: 0.40
Nodes (4): 4. One error model for REST and WebSocket, Consequences, Context, Decision

### Community 115 - "RADE JAPC admin page"
Cohesion: 0.18
Nodes (11): RADE JAPC admin page, admin.css, Clients tab (requests per RBAC user), common.css, RBAC sessions tab (cached logins, revoke), Service info tab, Subscriptions tab, Admin panel (/admin) (+3 more)

### Community 117 - "x-close-codes"
Cohesion: 0.20
Nodes (10): x-close-codes, 1000, 1001, 1011, 4000, 4001, 4002, 4003 (+2 more)

### Community 143 - "Configuration reference"
Cohesion: 0.20
Nodes (10): Configuration reference, Environment variables, Fixed limits, Profiles, Rate limits: `service-properties.rate-limit` (`RateLimitProperties`), RBAC: `service-properties.rbac` (`RbacProperties`), Requests: `service-properties`, Server (+2 more)

### Community 145 - "Security"
Cohesion: 0.40
Nodes (5): Known limitations, Model, Reporting a problem, Secrets, Security

### Community 146 - "labviewData"
Cohesion: 0.33
Nodes (6): contentType, description, name, $ref, title, labviewData

### Community 148 - "value"
Cohesion: 0.40
Nodes (5): value, description, properties, required, type

## Knowledge Gaps
- **26 isolated node(s):** `GET`, `SET`, `SUBSCRIBE`, `EXPLICIT`, `KERBEROS` (+21 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 608 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **70 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Remote probe (down alert)` connect `Remote probe (down alert)` to `README.md`, `MONIT tenant rade`?**
  _High betweenness centrality (0.063) - this node is a cross-community bridge._
- **Are the 2 inferred relationships involving `ApiContractTest` (e.g. with `5. Tests` and `Where things are`) actually correct?**
  _`ApiContractTest` has 2 INFERRED edges - model-reasoned connections that need verification._
- **What connects `GET`, `SET`, `SUBSCRIBE` to the rest of the system?**
  _26 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `EndpointTest` be split into smaller, more focused modules?**
  _Cohesion score 0.1294871794871795 - nodes in this community are weakly interconnected._
- **Why does `MONIT setup checklist` connect `README.md` to `Remote probe (down alert)`?**
  _High betweenness centrality (0.063) - this node is a cross-community bridge._
- **Are the 6 inferred relationships involving `RbacProperties` (e.g. with `Decision` and `RBAC: `service-properties.rbac` (`RbacProperties`)`) actually correct?**
  _`RbacProperties` has 6 INFERRED edges - model-reasoned connections that need verification._
- **Should `Python Subscription Tests` be split into smaller, more focused modules?**
  _Cohesion score 0.08048780487804878 - nodes in this community are weakly interconnected._