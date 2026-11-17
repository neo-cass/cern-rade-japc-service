# CERN SSO for the admin panel

How the admin panel (`/admin`) logs in with CERN Single Sign-On, the minimum code that makes it work, and every web
form that has to be filled in, with the values used by BE-CEM-MTA (RADE) for the `rade-services` PaaS project.

Only the admin panel uses SSO. API and WebSocket clients keep logging in with the `Rbac` header
(see [security.md](security.md)).

## How it works

```
browser ── /admin ──► CERN SSO login (auth.cern.ch, OIDC)
                            │  user access token, audience = our SSO app
                            ▼
service ── token exchange (auth.cern.ch) ──► access token, audience = rbac-pro
        ── RBAC OAuth login (rbac-pro) ────► RBAC master token → application token with the user's RBAC roles
        ── RBAC role check ────────────────► ROLE_RADE-ADMIN required for /admin and /admin/api
```

The panel checks the same RBAC roles as the API, not SSO roles. SSO only proves who the user is; RBAC decides what they
may do.

## Values used by the team

| What | Value |
|---|---|
| PaaS project | `rade-services` (`https://paas.cern.ch`) |
| Public host (route `japc-prod`) | `rade-services-japc.web.cern.ch` |
| SSO application (Application Portal) | `rade-services-japc-admin-panel` |
| Application administrators group | `rade-admins` |
| Admin RBAC role | `RADE-ADMIN` (members of the `rade-admins` e-group) |
| RBAC environment / token-exchange target | `rbac-pro` (`rbac-testbed` for TESTBED) |
| OpenShift secret with the SSO credentials | `rade-services-japc-admin-sso` (keys `SSO_CLIENT_ID`, `SSO_CLIENT_SECRET`) |
| Spring profile that enables SSO | `sso` (`SPRING_PROFILES_ACTIVE=prod,sso,...` in `deploy/app.yaml`) |

## Forms to fill in

Everything below is done once per SSO application. It has already been done for `rade-services-japc-admin-panel`;
repeat it only for a new application or host.

### 1. Application Portal: add the application

<https://application-portal.web.cern.ch> → **Add an Application**. The wizard has two steps; step 1 is
**Application details**.

| Field | Value | Notes |
|---|---|---|
| Application Identifier * | `rade-services-japc-admin-panel` | Lowercase, digits, `-` or `_`, 3–128 characters. Becomes the OIDC **client id** and the token audience. Cannot be changed later. |
| Name * | `rade-services-japc-admin-panel` (or a readable name, e.g. "RADE JAPC Admin Panel") | Must be unique. |
| Home Page | `https://rade-services-japc.web.cern.ch/admin/` | Informational. |
| Description | `Browser admin panel of the RADE JAPC REST service (PaaS project rade-services). Admins log in with CERN SSO; access is restricted to the RBAC role RADE-ADMIN.` | Informational. |
| Administrators → *Select Admin Group* | `rade-admins` | Lets any team admin manage the application. Whoever creates it becomes **Owner**. |
| Category * | **Official** | **Test** while experimenting. |

Click **Next: CERN SSO Registration**.

### 2. Application Portal: CERN SSO Registration (OIDC)

Step 2 of the same wizard (later: open the application → **SSO Registration** tab → **Add SSO Registration**).

| Field | Value | Notes |
|---|---|---|
| Protocol tabs (SAML / OIDC / No SSO) | **OIDC** | |
| Redirect URI(s) * | `https://rade-services-japc.web.cern.ch/login/oauth2/code/cern` | Spring Security's callback: `{baseUrl}/login/oauth2/code/{registrationId}`, registration id `cern` (`application-sso.yml`). Exact match, case-sensitive. The empty second row (green **+**) only adds more entries; leave it empty. |
| Base URL * | `https://rade-services-japc.web.cern.ch/admin/` | Where SSO links back to the app. Not used for the login callback. |
| Client Secret Configuration | **Confidential Client** | Not *Public Client* (the server keeps the secret, the token exchange needs it). Not *Confidential Client with Client Credential Flow* (the panel never gets tokens for itself). |

Save. The registration shows the **Client ID** (= the Application Identifier) and the **Client Secret**. Copy both for
step 5. Treat the secret like a password; do not commit it or paste it into tickets.

For an extra environment with another host (e.g. a test route), add its callback URL to **Redirect URI(s)**.

### 3. Application Portal: token exchange permission for RBAC

Lets the service exchange the user's SSO token for one the RBAC server accepts.

1. Application Portal → open `rade-services-japc-admin-panel` → **SSO Registration** tab.
2. In the **OpenID Connect** row, **Actions** column, click the **green lock** icon (tooltip "Token exchange
   permissions").
3. Under **Allowed Permissions**, click **Request token exchange permissions**.
4. Search and select **`rbac-pro`** (add **`rbac-testbed`** only if you test against TESTBED). Submit.

The RBAC team approves the request in the portal; afterwards `rbac-pro` is listed under *Allowed Permissions*. Until it
is approved, the SSO login works but the panel answers "RBAC login failed". Leave *Granted Permissions* alone (that is
other applications exchanging tokens for ours). If the RBAC team asks why: "browser admin panel of the RADE JAPC
service; admins log in with CERN SSO and the server exchanges their token for audience rbac-pro to log in to RBAC with
`loginMasterOauth`; access is limited to RADE-ADMIN".

### 4. Application Portal: roles (optional)

Not needed for the panel to work (it checks `RADE-ADMIN` from RBAC). Optional, to stop non-admins at the CERN login page
instead of with a 403 from the service:

Application → **Roles** tab → edit the **default role** → untick **This role applies to all authenticated users** →
Submit → add the group **`rade-admins`**.

### 5. OpenShift: secret with the client id and secret

Project `rade-services` (`oc project rade-services` after `oc login --web https://api.paas.okd.cern.ch`):

```bash
oc create secret generic rade-services-japc-admin-sso -n rade-services --from-literal=SSO_CLIENT_ID='rade-services-japc-admin-panel' --from-literal=SSO_CLIENT_SECRET='<client secret from step 2>'
```

`deploy/app.yaml` reads both keys into the `SSO_CLIENT_ID` / `SSO_CLIENT_SECRET` environment variables. After changing
the secret, restart the deployment so it reads the new values:

```bash
oc rollout restart deployment/<deployment> -n rade-services
```

Pitfall: if the values come from a file with Windows line endings, the client id ends in a carriage return and CERN SSO
answers **"Client not found"** (the login URL shows `client_id=...%0D`). Strip `\r` or type the values directly.

### 6. Access for a new admin (Groups Portal)

The panel requires the RBAC role `RADE-ADMIN`, which comes from the `rade-admins` e-group. A new team member requests
membership of `rade-admins` in the Groups Portal (<https://groups-portal.web.cern.ch> → search `rade-admins` →
subscribe), approved by its administrators. Role changes apply to new RBAC tokens: log out and in again.

## Minimum code

What SSO needs, and nothing else. Paths are relative to `src/main/java/cern/rade/services/japc/`.

### Dependencies (`build.gradle.kts`)

```kotlin
implementation("org.springframework.boot:spring-boot-starter-security:$springBootVersion")
implementation("org.springframework.boot:spring-boot-starter-oauth2-client:$springBootVersion")
// already present for RBAC; OAuth2ExchangeClient (token exchange) is in rbac-util
implementation("cern.accsoft.security:rbac-client:10.11.1")
implementation("cern.accsoft.security:rbac-util:10.11.1")
```

### Configuration

`src/main/resources/application-sso.yml` (active only with the `sso` profile, so local runs and CI need no SSO
credentials):

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          cern:                                  # registration id → callback /login/oauth2/code/cern
            client-id: ${SSO_CLIENT_ID}
            client-secret: ${SSO_CLIENT_SECRET}
            scope: openid,profile,email
        provider:
          cern:
            issuer-uri: https://auth.cern.ch/auth/realms/cern   # endpoints come from its .well-known config
server:
  servlet:
    session:
      cookie:
        secure: true        # TLS ends at the OpenShift router, Tomcat itself sees plain HTTP
        same-site: lax
```

`src/main/resources/application.yml`:

```yaml
server:
  forward-headers-strategy: framework   # use the router's X-Forwarded-* so redirect_uri is https://<host>/...
```

Without `forward-headers-strategy` Spring builds `http://...` callback URLs and CERN SSO rejects them with
"Invalid parameter: redirect_uri".

`deploy/app.yaml` (container env):

```yaml
- name: SPRING_PROFILES_ACTIVE
  value: "prod,sso,..."
- name: SSO_CLIENT_ID
  valueFrom: { secretKeyRef: { name: rade-services-japc-admin-sso, key: SSO_CLIENT_ID } }
- name: SSO_CLIENT_SECRET
  valueFrom: { secretKeyRef: { name: rade-services-japc-admin-sso, key: SSO_CLIENT_SECRET } }
```

### Spring Security (`security/SecurityConfiguration.java`, admin filter chain)

```java
http.securityMatcher("/admin", "/admin/**", "/oauth2/**", "/login/**", "/logout")
        .authorizeHttpRequests(requests -> requests
                .requestMatchers("/oauth2/**", "/login/**").permitAll()            // the login flow itself
                .anyRequest().hasAuthority("ROLE_" + properties.adminRole()));     // ROLE_RADE-ADMIN
ClientRegistrationRepository registrations = ssoRegistrations.getIfAvailable();   // absent without the sso profile
if (registrations != null) {
    http.oauth2Login(login -> login.userInfoEndpoint(userInfo ->
                    userInfo.oidcUserService(new RbacOidcUserService(gateway, loginService, tokenSupport))))
        .logout(logout -> logout.logoutSuccessHandler(new OidcClientInitiatedLogoutSuccessHandler(registrations)));
}
```

`oauth2Login()` provides the redirect to CERN SSO, the callback endpoint and the session. The only custom part is the
user service, which turns the SSO login into an RBAC login.

### SSO → RBAC (`security/sso/`)

`RbacOidcUserService` (Spring calls it once per login, on the callback request):

```java
public OidcUser loadUser(OidcUserRequest request) {
    OidcUser user = delegate.loadUser(request);                       // standard OIDC user from CERN SSO
    ClientRegistration registration = request.getClientRegistration();
    String rbacToken = gateway.exchangeForRbac(registration.getClientId(), registration.getClientSecret(),
            request.getAccessToken().getTokenValue());               // step 3 permission needed here
    RbacSession session = loginService.login(new RbacCredentials(OauthLogin.SCHEME, rbacToken, currentClient()));
    return new RbacOidcUser(user, session, tokenSupport.authorities(session));   // ROLE_<RBAC role>
}
```

- `rbac/gateway/RbacGateway.exchangeForRbac` → `OAuth2ExchangeClient.getInstance().exchange(clientId, clientSecret,
  userAccessToken)` (rbac-util). The target audience comes from `rbac.env` (default PRO → `rbac-pro`).
- `rbac/login/OauthLogin` → `AuthenticationClient.loginMasterOauth(token, lifetime)`, then the usual renewable RBAC
  session.
- `RbacOidcUser` = `DefaultOidcUser` whose authorities are the RBAC roles, plus the `RbacSession` (used by logout and
  revocation).

A failed exchange or RBAC login is turned into `OAuth2AuthenticationException("rbac_login_failed", ...)`, so the user
sees Spring's login error page instead of a stack trace.

### Not required for SSO itself

These live in the same chain but are about sessions, not SSO: `InactiveSessionFilter` (ends a browser login whose RBAC
session was revoked), the logout handler that revokes the RBAC session, CSRF/CSP settings and the admin panel/API.

## Checking it

1. Open `https://rade-services-japc.web.cern.ch/admin` in a private window → CERN SSO login → the panel loads and the
   header shows the user with login type `oauth` and their RBAC roles.
2. Admin panel → **RBAC sessions**: an `oauth` session for the user.
3. **Log out** → CERN SSO logout page; `/admin` asks for the CERN login again.
4. A user without `RADE-ADMIN` gets **403** after logging in.

## Troubleshooting

| Symptom | Cause | Fix |
|---|---|---|
| CERN SSO page: "Client not found", URL has `client_id=...%0D` | Carriage return in the client id (secret created from a CRLF file) | Recreate/patch the secret without `\r`, restart the deployment |
| CERN SSO page: "Invalid parameter: redirect_uri" | Callback URL not registered exactly, or built with `http://` | Compare the decoded `redirect_uri` with step 2; check `server.forward-headers-strategy: framework` |
| Back on the service: "RBAC login failed" | Token exchange for `rbac-pro` not approved, or `rbac.env` not PRO | Step 3; check *Allowed Permissions* lists `rbac-pro` |
| 403 after a successful login | User lacks `RADE-ADMIN` in the RBAC token | Join `rade-admins` (step 6), log out and in |
| `/admin` returns 401 JSON instead of redirecting to SSO | `sso` profile not active or secret missing (no `ClientRegistrationRepository`) | Check `SPRING_PROFILES_ACTIVE` and the secret in `deploy/app.yaml` |
| Service fails at startup | `issuer-uri` unreachable (Spring loads the SSO configuration at startup) | Network from the pod to `auth.cern.ch` |

## References

- CERN Auth docs: [adding an application](https://auth.docs.cern.ch/applications/adding-application/),
  [SSO registration](https://auth.docs.cern.ch/applications/sso-registration/),
  [OIDC configuration](https://auth.docs.cern.ch/user-documentation/oidc/config/),
  [token exchange](https://auth.docs.cern.ch/user-documentation/oidc/exchange-for-api/)
- RBAC: Authentication Guide, section "OAuth 2.0 login" (`acc-co/security/rbac`, `docs/AUTHENTICATION.md`)
- Spring Security: OAuth 2.0 Login (`oauth2Login()`, `OidcClientInitiatedLogoutSuccessHandler`)
