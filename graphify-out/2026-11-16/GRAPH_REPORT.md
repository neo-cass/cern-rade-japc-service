# Graph Report - japc-new  (2026-11-16)

## Corpus Check
- 139 files · ~42,308 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 15 file(s) not represented in the graph (top: (none) 5, .properties 2, .css 2)

## Summary
- 2019 nodes · 4462 edges · 154 communities (87 shown, 67 thin omitted)
- Extraction: 91% EXTRACTED · 9% INFERRED · 0% AMBIGUOUS · INFERRED: 420 edges (avg confidence: 0.86)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `576189b7`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- SubscriptionManager
- .matching
- org.springframework.http.ResponseEntity
- EndpointTest
- README.md
- Python Subscription Tests
- SecurityConfiguration.java
- Kubernetes Deployment
- schemas
- Log4j Config (A)
- ParameterEndpoint
- Log4j Config (B)
- .id
- OpenAPI Operations
- RbacProperties
- RbacSession
- Message Payload Schema
- org.springframework.http.MediaType
- StatsConfiguration.java
- org.springframework.context.annotation.Bean
- CachedLogin.java
- Format
- RateLimiter
- OpenShift Route (A)
- OpenShift Route (B)
- ParameterProvider
- WebSocket Channel Bindings
- x-close-codes
- GitLab CI Pipeline
- RequestMetrics
- org.junit.jupiter.api.Test
- Subscription Handshake
- Prod Config
- admin.js
- AsyncAPI Components
- cern.rbac.common.MasterToken
- OTLP Export Config
- ParameterAddress
- SubscriptionService
- Stubs
- LabVIEW Descriptor Channels
- SubscriptionWebSocketHandler
- org.springframework.http.HttpHeaders
- RbacOidcUserService.java
- Deploy Environments
- Smoke Tests & CI Rules
- CI Test Job
- RBAC Filters
- MONIT tenant rade
- SecurityErrorWriter.java
- Docs & WAR Build
- Production Deploy
- OAuth2 Client Registration
- Publish WAR
- Debug Config
- Override
- Server & SSL Config
- Local RBAC Token Support
- LabVIEW Data Messages
- Payload Fields
- Frame Layout Schema
- Endpoint Test Reports
- PaaS CI Defaults
- LoginType
- Spring Profiles
- Tracing & OTLP Logging
- RbacHeaderFilterTest
- Rbac
- instant
- targets
- CI Tool Variables
- org.springframework.context.annotation.Configuration
- Health Probes
- CLAUDE.md
- labviewError
- cern.japc.value.ParameterValue
- ValueSource
- Springdoc Config
- Deployed Test Job
- Swagger UI Config
- Redocly Lint
- Actuator Exposure
- Metrics Export
- .callAs
- Session Attributes
- .login
- RADE JAPC service home page
- .of
- ParameterServiceTest
- jsonData
- jsonError
- org.slf4j.Logger
- AdminService
- cern.rade.services.japc.error.ErrorCode
- .select
- MutableClock
- jakarta.servlet.http.HttpServletRequest
- Format
- 4. One error model for REST and WebSocket
- RADE JAPC admin page
- Security
- Operations
- headers
- RADE JAPC service
- subprotocol
- errorCode
- fields
- LvTypeDescriptor
- LvTypeDescriptorOptional
- selector
- properties
- .onText

## God Nodes (most connected - your core abstractions)
1. `RbacSession` - 68 edges
2. `ApiContractTest` - 56 edges
3. `EndpointTest` - 34 edges
4. `RbacProperties` - 33 edges
5. `RbacGateway` - 31 edges
6. `RbacSessionRegistry` - 31 edges
7. `ParameterAddress` - 29 edges
8. `RbacHeaderFilterTest` - 27 edges
9. `SubscriptionService` - 26 edges
10. `SubscriptionWebSocketHandler` - 26 edges

## Surprising Connections (you probably didn't know these)
- `Tests` --references--> `EndpointTest`  [INFERRED]
  docs/configuration.md → src/test/java/cern/rade/services/japc/EndpointTest.java
- `Recurring maintenance` --references--> `EndpointTest`  [INFERRED]
  docs/operations.md → src/test/java/cern/rade/services/japc/EndpointTest.java
- `Release` --references--> `EndpointTest`  [INFERRED]
  docs/operations.md → src/test/java/cern/rade/services/japc/EndpointTest.java
- `RBAC: `service-properties.rbac` (`RbacProperties`)` --references--> `RbacProperties`  [INFERRED]
  docs/configuration.md → src/main/java/cern/rade/services/japc/rbac/RbacProperties.java
- `5. Tests` --references--> `ApiContractTest`  [INFERRED]
  docs/adding-an-endpoint.md → src/test/java/cern/rade/services/japc/ApiContractTest.java

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **MONIT observability pipeline (OTLP export, data sources, alert rules)** — deploy_monit_readme_otlp_export, deploy_monit_readme_monit_tenant_rade, deploy_monit_readme_ds_monit_metrics, deploy_monit_readme_ds_monit_logs, deploy_monit_readme_alert_rules, deploy_monit_readme_snow_alert_routing [EXTRACTED 0.95]
- **Admin panel tabs** — src_main_resources_web_admin_index_service_info_tab, src_main_resources_web_admin_index_rbac_sessions_tab, src_main_resources_web_admin_index_subscriptions_tab, src_main_resources_web_admin_index_clients_tab [EXTRACTED 1.00]

## Communities (154 total, 67 thin omitted)

### Community 1 - ".matching"
Cohesion: 0.16
Nodes (4): LabVIEWBody, LabVIEWBodyTest, LabVIEWConverterTest, HttpInputMessage

### Community 2 - "org.springframework.http.ResponseEntity"
Cohesion: 0.13
Nodes (3): AdminEndpoint, RbacSessionEndpoint, SessionInfoView

### Community 4 - "README.md"
Cohesion: 0.12
Nodes (13): MONIT setup checklist, 1. RBAC login through the `Rbac` header and cached sessions, Consequences, Context, Decision, 5. State in memory, one replica, Consequences, Context (+5 more)

### Community 5 - "Python Subscription Tests"
Cohesion: 0.08
Nodes (13): describe_rejection(), field_value(), load_rbac_header(), main(), receive_data(), rest(), rest_get(), Result (+5 more)

### Community 6 - "SecurityConfiguration.java"
Cohesion: 0.11
Nodes (3): LocalRbacTokenSupport, SecurityConfiguration, SecurityErrorWriter

### Community 7 - "Kubernetes Deployment"
Cohesion: 0.06
Nodes (35): env, image, livenessProbe, name, ports, readinessProbe, resources, startupProbe (+27 more)

### Community 8 - "schemas"
Cohesion: 0.22
Nodes (9): description, format, type, schemas, description, format, type, acqStamp (+1 more)

### Community 9 - "Log4j Config (A)"
Cohesion: 0.06
Nodes (33): ref, Console, OpenTelemetry, RollingFile, Configuration, Appenders, Loggers, properties (+25 more)

### Community 11 - "Log4j Config (B)"
Cohesion: 0.07
Nodes (30): ref, Console, RollingFile, Configuration, Appenders, Loggers, properties, status (+22 more)

### Community 12 - ".id"
Cohesion: 0.13
Nodes (3): RbacSessionRegistry, RbacSessionRegistryTest, RbacSessionEndpointTest

### Community 14 - "OpenAPI Operations"
Cohesion: 0.08
Nodes (28): $ref, default, description, operations, sendField, sendLabviewDescriptor, sendProperty, $ref (+20 more)

### Community 15 - "RbacProperties"
Cohesion: 0.15
Nodes (13): RbacGateway, KerberosLogin, MasterTokenLogin, OauthLogin, TokenLogin, LoginType, EXPLICIT, KERBEROS (+5 more)

### Community 17 - "Message Payload Schema"
Cohesion: 0.08
Nodes (26): $ref, $ref, $ref, payload, payload, type, properties, required (+18 more)

### Community 20 - "org.springframework.context.annotation.Bean"
Cohesion: 0.14
Nodes (3): ExplicitLogin, RbacConfiguration, RbacLogin

### Community 24 - "RateLimiter"
Cohesion: 0.21
Nodes (3): RateLimiter, RequestInterceptor, RateLimiterTest

### Community 25 - "OpenShift Route (A)"
Cohesion: 0.09
Nodes (21): haproxy.router.openshift.io/rate-limit-connections, haproxy.router.openshift.io/rate-limit-connections.concurrent-tcp, haproxy.router.openshift.io/rate-limit-connections.rate-http, router.cern.ch/network-visibility, apiVersion, kind, app, metadata (+13 more)

### Community 26 - "OpenShift Route (B)"
Cohesion: 0.10
Nodes (20): haproxy.router.openshift.io/rate-limit-connections, haproxy.router.openshift.io/rate-limit-connections.concurrent-tcp, haproxy.router.openshift.io/rate-limit-connections.rate-http, router.cern.ch/network-visibility, apiVersion, kind, app, metadata (+12 more)

### Community 27 - "ParameterProvider"
Cohesion: 0.05
Nodes (27): 3. Stateless singleton services, constructor injection, typed configuration, Consequences, Context, Decision, A GET or SET request, A subscription, Architecture, External dependencies (+19 more)

### Community 28 - "WebSocket Channel Bindings"
Cohesion: 0.29
Nodes (11): labviewDescriptor, withFields, withoutFields, channelBindings, ws, type, ws, ws (+3 more)

### Community 29 - "x-close-codes"
Cohesion: 0.10
Nodes (20): info, description, title, version, x-close-codes, x-first-update-timeout-ms, x-idle-timeout-ms, x-ping-interval-ms (+12 more)

### Community 30 - "GitLab CI Pipeline"
Cohesion: 0.11
Nodes (19): image, build_image, image, script, stage, tags, .gradle_job, before_script (+11 more)

### Community 31 - "RequestMetrics"
Cohesion: 0.33
Nodes (3): ClientRequests, RequestMetrics, RequestMetricsTest

### Community 32 - "org.junit.jupiter.api.Test"
Cohesion: 0.08
Nodes (6): ApiContractTest, Call, WebSocket.Listener, ApiPathsTest, FormatConfigurationTest, RbacCredentialsTest

### Community 35 - "Prod Config"
Cohesion: 0.14
Nodes (13): cern.cmw.directory.http.client.impl.feign.FeignDecoder, cern.cmw.rda3.impl.client.subscription.SubscriptionControllerImpl, cern.cmw.rda3.impl.transport.client.ClientDispatcher, cern.cmw.rda3.impl.transport.zeromq.ZMQAdapter, com.netflix.config.sources.URLConfigurationSource, org.springframework.boot.autoconfigure, logging, config (+5 more)

### Community 36 - "admin.js"
Cohesion: 0.27
Nodes (17): act(), button(), csrfToken(), duration(), element(), loadClients(), loadInfo(), loadMe() (+9 more)

### Community 37 - "AsyncAPI Components"
Cohesion: 0.13
Nodes (18): components, messages, parameters, description, examples, $ref, description, examples (+10 more)

### Community 39 - "OTLP Export Config"
Cohesion: 0.12
Nodes (17): enabled, headers, otlp, step, url, Authorization, export, management (+9 more)

### Community 42 - "SubscriptionService"
Cohesion: 0.14
Nodes (3): SubscriptionService, Latest, Update

### Community 43 - "Stubs"
Cohesion: 0.09
Nodes (6): WebSocket.Listener, Stubs, Handler, RbacTokenSupport, RbacLogin, RbacSession

### Community 44 - "LabVIEW Descriptor Channels"
Cohesion: 0.12
Nodes (16): asyncapi, $ref, channels, labviewDescriptor, property, bindings, address, bindings (+8 more)

### Community 49 - "RbacOidcUserService.java"
Cohesion: 0.16
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
Cohesion: 0.16
Nodes (17): Grafana alert rules (app not reporting, CPU, heap, rate limited, dropped updates, errors, pod restart), Alert contact point monit-alarms, Data source: MONIT logs (OpenSearch), Data source: MONIT metrics, Data source: PaaS metrics, Grafana organization ticket, JAPC Grafana dashboard (deploy/grafana/japc-dashboard.json), JAPC service metrics (japc_*) (+9 more)

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

### Community 62 - "Override"
Cohesion: 0.14
Nodes (3): FailingLogin, FixedLogin, ServletOutputStream

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

### Community 72 - "LoginType"
Cohesion: 0.17
Nodes (3): MeView, RbacSession, RbacOidcUser

### Community 73 - "Spring Profiles"
Cohesion: 0.25
Nodes (8): name, active, add-mappings, spring, application, profiles, web, resources

### Community 74 - "Tracing & OTLP Logging"
Cohesion: 0.29
Nodes (8): otlp, export, management, logging, otlp, tracing, enabled, export

### Community 75 - "RbacHeaderFilterTest"
Cohesion: 0.14
Nodes (4): RbacSession, RbacHeaderFilterTest, CapturingChain, StubResponse

### Community 76 - "Rbac"
Cohesion: 0.29
Nodes (7): securitySchemes, description, in, name, type, x-rbac-schemes, Rbac

### Community 78 - "targets"
Cohesion: 0.14
Nodes (13): certificate_expiration, days, http_ipv4, targets, snow_auto_closing, snow_functional_element, alert_options, enabled_alerts (+5 more)

### Community 79 - "CI Tool Variables"
Cohesion: 0.29
Nodes (7): variables, ASYNCAPI_CLI, ASYNCAPI_HTML_TEMPLATE, CMW_TOOLS_IMAGE, PROD_APP_NAME, REDOCLY_CLI, WAR_NAME

### Community 81 - "Health Probes"
Cohesion: 0.29
Nodes (7): health, readiness, group, probes, endpoint, enabled, additional-path

### Community 83 - "labviewError"
Cohesion: 0.33
Nodes (6): contentType, description, name, $ref, title, labviewError

### Community 84 - "cern.japc.value.ParameterValue"
Cohesion: 0.18
Nodes (3): LabVIEWValue, NamedValue, Set

### Community 85 - "ValueSource"
Cohesion: 0.25
Nodes (4): 2. JSON and LabVIEW as interchangeable formats, Consequences, Context, ValueSource

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

### Community 93 - ".callAs"
Cohesion: 0.15
Nodes (12): 1. Route, 2. Service method, 3. Controller method, 5. Tests, 6. Specification and documentation, Adding an API version, Before you start, Checklist (+4 more)

### Community 95 - ".login"
Cohesion: 0.20
Nodes (3): RbacLogin, RbacLoginService, RbacLoginServiceTest

### Community 98 - "RADE JAPC service home page"
Cohesion: 0.15
Nodes (14): Alert routing to SNOW LabVIEW Support, RADE JAPC API docs landing page, asyncapi.yaml (AsyncAPI 3.1), openapi.yaml (OpenAPI 3.1), REST API (GET and SET), WebSocket API (subscriptions), RADE JAPC service home page, Background paper (doi 10.17181/x7kbq-qrv65) (+6 more)

### Community 99 - ".of"
Cohesion: 0.17
Nodes (9): 4. A new error, ErrorCode, BAD_REQUEST, CODEC_ERROR, INTERNAL_ERROR, JAPC_ERROR, RATE_LIMITED, RBAC_ERROR (+1 more)

### Community 100 - "ParameterServiceTest"
Cohesion: 0.32
Nodes (3): ParameterServiceTest, Call, StubParameter

### Community 102 - "jsonData"
Cohesion: 0.29
Nodes (7): contentType, description, examples, name, $ref, title, jsonData

### Community 103 - "jsonError"
Cohesion: 0.29
Nodes (7): contentType, description, examples, name, $ref, title, jsonError

### Community 104 - "org.slf4j.Logger"
Cohesion: 0.12
Nodes (3): ErrorHandler, ErrorLog, InitialiseService

### Community 105 - "AdminService"
Cohesion: 0.27
Nodes (3): AdminService, SessionView, SubscriptionRegistry

### Community 110 - "jakarta.servlet.http.HttpServletRequest"
Cohesion: 0.20
Nodes (5): Client, Operation, GET, SET, SUBSCRIBE

### Community 114 - "4. One error model for REST and WebSocket"
Cohesion: 0.40
Nodes (4): 4. One error model for REST and WebSocket, Consequences, Context, Decision

### Community 115 - "RADE JAPC admin page"
Cohesion: 0.20
Nodes (10): RADE JAPC admin page, admin.css, common.css, RBAC sessions tab (cached logins, revoke), Service info tab, Subscriptions tab, Admin panel (/admin), rade-services-japc-users e-group (+2 more)

### Community 116 - "Security"
Cohesion: 0.22
Nodes (6): Known limitations, Model, Reporting a problem, Secrets, Security, When you change code

### Community 117 - "Operations"
Cohesion: 0.29
Nodes (7): Health, Observing, Operations, Recurring maintenance, Release, Runbook, Where it runs

### Community 118 - "headers"
Cohesion: 0.33
Nodes (6): properties, required, type, Lv-Type-Descriptor, Sec-WebSocket-Protocol, headers

### Community 142 - "RADE JAPC service"
Cohesion: 0.33
Nodes (6): Documentation, Quick start, RADE JAPC service, Rules for changes, Support, Where things are

### Community 143 - "subprotocol"
Cohesion: 0.40
Nodes (5): subprotocol, default, description, enum, type

### Community 144 - "errorCode"
Cohesion: 0.50
Nodes (4): description, enum, type, errorCode

### Community 145 - "fields"
Cohesion: 0.50
Nodes (4): description, examples, type, fields

### Community 146 - "LvTypeDescriptor"
Cohesion: 0.50
Nodes (4): description, format, type, LvTypeDescriptor

### Community 147 - "LvTypeDescriptorOptional"
Cohesion: 0.50
Nodes (4): description, format, type, LvTypeDescriptorOptional

### Community 148 - "selector"
Cohesion: 0.50
Nodes (4): selector, description, examples, type

### Community 149 - "properties"
Cohesion: 0.67
Nodes (3): fields, selector, properties

## Knowledge Gaps
- **26 isolated node(s):** `GET`, `SET`, `SUBSCRIBE`, `EXPLICIT`, `KERBEROS` (+21 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 574 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **67 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Remote probe (down alert)` connect `targets` to `README.md`, `MONIT tenant rade`?**
  _High betweenness centrality (0.058) - this node is a cross-community bridge._
- **Are the 2 inferred relationships involving `ApiContractTest` (e.g. with `5. Tests` and `Where things are`) actually correct?**
  _`ApiContractTest` has 2 INFERRED edges - model-reasoned connections that need verification._
- **What connects `GET`, `SET`, `SUBSCRIBE` to the rest of the system?**
  _26 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `SubscriptionManager` be split into smaller, more focused modules?**
  _Cohesion score 0.13105413105413105 - nodes in this community are weakly interconnected._
- **Why does `MONIT setup checklist` connect `README.md` to `targets`?**
  _High betweenness centrality (0.057) - this node is a cross-community bridge._
- **Are the 5 inferred relationships involving `EndpointTest` (e.g. with `Tests` and `Recurring maintenance`) actually correct?**
  _`EndpointTest` has 5 INFERRED edges - model-reasoned connections that need verification._
- **Should `org.springframework.http.ResponseEntity` be split into smaller, more focused modules?**
  _Cohesion score 0.13306451612903225 - nodes in this community are weakly interconnected._