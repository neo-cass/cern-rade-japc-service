# MONIT setup guide

How to connect the JAPC services to CERN MONIT: a remote probe (down alert), metrics and logs exported over OTLP,
Grafana dashboards and alerts. Anyone with the access listed under *Before you start* can follow it. The service
itself needs no further code: it starts sending data as soon as step B2 is done.

Order of work: part A needs no MONIT tenant and can start right away; part B needs the tenant from ticket A2; part C
needs the Grafana organization from ticket A3. Step numbers are referenced from other documents, so they stay stable.

## Values used in this guide

Replace these if you set the same thing up for another service.

| What | Value |
|---|---|
| MONIT tenant (also its username on MONIT endpoints) | `rade` |
| OpenShift project / deployments | `rade-services` / `japc`, `japc-master` |
| Team e-group (owns the tenant, reads the password, admins the Grafana org) | `rade-admins` |
| Grafana organization | `RADE` |
| Group in `monit-remote-probes` | `rade` |
| Probe target | `https://rade-services-japc.web.cern.ch/readyz` |
| Teigi service | `monit_rade` |
| Tenant user and its Teigi key (OTLP export, Prometheus) | `rade` / `monit_rade_password` |
| Read-only OpenSearch user and its Teigi key (logs in Grafana) | `rade_ro` / `monit_rade_ro_password` |

## Before you start

- **A CERN account** and the `oc` CLI, logged in with edit rights on the OpenShift project:
  `oc login --web https://api.paas.okd.cern.ch:443`. The login expires, so log in again if `oc` says *Unauthorized*.
- **Membership of the team e-group `rade-admins`**: it owns the MONIT tenant, its members can read the tenant password
  and they are Admins of the Grafana organization. Ask a current member (or the e-group owner) to add you.
- **Membership of the e-group `ai-admins`.** All members of `rade-admins` are automatically members of `ai-admins`, so
  joining `rade-admins` is enough and there is nothing separate to request. You need it for two things:
  - to push branches to `monitoring/monit-remote-probes` (step A1). It gives Developer access to the project. Without
    it `git push` fails with `403 You are not allowed to push code to this project`;
  - to log in to `aiadm`, the only place where the tenant password can be read with `tbag` (step B1).

  If you are not in `rade-admins` yet, ask for that first. Only for a person outside the team, request `ai-admins`
  directly at https://cern.ch/egroups (search for `ai-admins`); the owner approves, usually within one or two working
  days. Background: [KB0000765](https://cern.service-now.com/service-portal?id=kb_article&n=KB0000765).

Do not wait for `ai-admins` before starting the tickets (A2, A3) and the token (A4). Only A1 and B1 depend on it.
Membership changes can take a little while to apply, so log out and in again if `403` or `Connection refused` persists.

---

## A. Setup that needs no tenant

### A1. Remote probe (down alert)

1. Clone the probes repository and create a branch:
   ```bash
   git clone https://gitlab.cern.ch/monitoring/monit-remote-probes.git
   cd monit-remote-probes
   git checkout -b add-rade-probes
   ```
2. Open `whitelist.yaml`. Add a group `rade` and, under it, the **CERN usernames** of the people allowed to change this
   group's probes. Copy the format of the existing entries. The whitelist holds usernames only, not e-groups.
3. Create the folder and copy the probe file from this repository:
   ```bash
   mkdir rade
   cp <path to japc-new>/deploy/monit/probes.yaml rade/probes.yaml
   ```
   The file carries `monit_shared: "rade"` as a label. It asks MONIT to forward the probe's metrics into the tenant.
   It does nothing until MONIT enables forwarding for the tenant (requested in A2, see B3), and is harmless before.
4. Commit and push:
   ```bash
   git add whitelist.yaml rade/probes.yaml
   git commit -m "Add RADE JAPC service probe"
   git push -u origin add-rade-probes
   ```
   If the push is refused with `403`, you are not a Developer on the project: join `ai-admins` (see *Before you start*).
   The fallback is a fork: click **Fork** on https://gitlab.cern.ch/monitoring/monit-remote-probes, push the branch
   to the fork and open the merge request from there. A merge request from a fork cannot merge itself: the pipeline's
   `approve_user_mr` job fails with `401` because the fork has no approval token, so someone from the MONIT team must
   approve and merge it by hand. Ask for that on the MR or in the MONIT ticket.
5. Open https://gitlab.cern.ch/monitoring/monit-remote-probes/-/merge_requests/new, select your branch as source,
   click **Compare branches and continue**, then **Create merge request**. For a branch pushed to the project the
   pipeline validates the configuration, approves and merges the MR on its own, and a pipeline on `master` then
   deploys it (job `configure_production`). This takes a few minutes.
6. Check the probe. Open
   https://monit-grafana.cern.ch/d/w57luRvnz/remote-probes-overview?orgId=1&var-producer=rade
   (the MONIT organization, dashboard *Remote Probes (Overview)*). The dashboard filters by **Producer** first: pick
   `rade` in the first drop-down, otherwise the probe is not listed. A green tile with the probe URL means the probe
   succeeds. For the numbers, run `probe_success{remote_producer="rade"}` in Explore on the data source *CERN IT
   Monit Prometheus LTS*: it returns `1` while `/readyz` answers 200.

   The probe does **not** appear in your own `RADE` organization until MONIT has enabled `monit_shared` forwarding
   for the tenant (B3).

### A2. Tenant ticket

1. Open https://cern.ch/monit-support (MONIT's Service Now form).
2. Paste:

   > **Subject:** New MONIT tenant "rade" (OTLP metrics and logs)
   >
   > Please create a MONIT tenant named **rade**. Responsible e-group: **rade-admins**. We do not manage Puppet nodes,
   > so please create the Teigi service and share the credentials with the e-group.
   >
   > New flow: metrics and logs from a Spring Boot service on PaaS (OpenShift project rade-services), sent via
   > OTLP/HTTP to monit-otlp.cern.ch:4319.
   > - Metrics: about 500–1,500 series per deployment, 30 s interval, 2–3 deployments.
   > - Logs: about 1–2 GB/day.
   >
   > Access: Grafana and OpenSearch. Please also:
   > - create the read-only OpenSearch user **rade_ro** on monit-timberprivate, with its own password in the Teigi
   >   service (the tenant user and password only work for OTLP and Prometheus, not for OpenSearch), and give the
   >   e-group **rade-admins** read access to our logs;
   > - enable forwarding of Remote Probes metrics to the tenant (the `monit_shared` label). It is off by default.

3. Submit. Keep the ticket number. The resolution names the Teigi service that holds the passwords (B1).

   Two things are **not** set up unless you ask for them, so keep both lines in the ticket:
   - the **read-only OpenSearch user `rade_ro` and its own password**. The tenant password does not log in to
     OpenSearch. Without `rade_ro`, Grafana cannot read the logs (C3);
   - **forwarding of the probe's metrics into the tenant (`monit_shared`)**. MONIT enables it per tenant on request,
     and it takes a while (B3).

### A3. Grafana organization ticket

1. Open https://cern.ch/monit-support again. It can also be asked in the same ticket as A2.
2. Paste:

   > **Subject:** New Grafana organization "RADE"
   >
   > Please create a Grafana organization in monit-grafana.cern.ch:
   > - Name: **RADE**
   > - Admin e-group: **rade-admins**
   > - Access level: **E-Group** (dashboards show user names and client addresses)
   >
   > Purpose: dashboards and alerts for RADE services on PaaS, data from MONIT tenant "rade" (requested in ticket
   > `<number from A2>`).

3. Submit.

### A4. Token for pod metrics (needed in C4)

Grafana reads pod metrics (restarts) from the PaaS metrics endpoint with a read-only service account and a token. This
step creates a token that **never expires**, so it needs no yearly renewal. `oc create token` cannot do that: it always
issues an expiring token (see the alternative at the end). A never-expiring token is a *service-account token secret*.

Run these in a terminal logged in with `oc`.

1. Create the service account and give it read-only access to the project:
   ```bash
   oc create serviceaccount monit-grafana-metrics-reader -n rade-services
   oc adm policy add-role-to-user view -z monit-grafana-metrics-reader -n rade-services
   ```
2. Create the token secret. Kubernetes fills in the token for the named service account within a few seconds.

   Bash:
   ```bash
   oc apply -n rade-services -f - <<'EOF'
   apiVersion: v1
   kind: Secret
   metadata:
     name: monit-grafana-metrics-reader-token
     annotations:
       kubernetes.io/service-account.name: monit-grafana-metrics-reader
   type: kubernetes.io/service-account-token
   EOF
   ```
   PowerShell:
   ```powershell
   @'
   apiVersion: v1
   kind: Secret
   metadata:
     name: monit-grafana-metrics-reader-token
     annotations:
       kubernetes.io/service-account.name: monit-grafana-metrics-reader
   type: kubernetes.io/service-account-token
   '@ | oc apply -n rade-services -f -
   ```
3. Read the token and put it on the clipboard, so it is not printed in the terminal.

   Bash (Linux: `xclip -selection clipboard`; macOS: `pbcopy`; Windows Git Bash: `clip`):
   ```bash
   oc get secret monit-grafana-metrics-reader-token -n rade-services -o jsonpath='{.data.token}' | base64 -d | clip
   ```
   PowerShell:
   ```powershell
   $b64 = oc get secret monit-grafana-metrics-reader-token -n rade-services -o jsonpath='{.data.token}'
   Set-Clipboard ([Text.Encoding]::UTF8.GetString([Convert]::FromBase64String($b64)))
   ```
   If nothing comes out, wait a few seconds and run it again. If the secret stays empty, the cluster does not issue
   these tokens: use the expiring alternative below.
4. Check it works (paste the token in place of `<token>`). It must print
   `system:serviceaccount:rade-services:monit-grafana-metrics-reader`:
   ```bash
   oc --token=<token> whoami
   ```
5. Keep the token in your password manager and paste it in C4 as `Bearer <token>`.

The token is read-only and limited to one project, but anyone who has it can read that project's pod metrics, so treat
it like a password. It stays valid until the secret or the service account is deleted. To revoke or replace it, delete
the secret and repeat steps 2 and 3, then update the header in C4:

```bash
oc delete secret monit-grafana-metrics-reader-token -n rade-services
```

**Alternative: expiring token.** If you prefer a token that expires, or the cluster does not fill the secret, use
`oc create token monit-grafana-metrics-reader -n rade-services --duration=8760h` instead of steps 2 and 3. The cluster
may cap the duration, and it expires after at most one year. Rerun the command before then and update the header
in C4.

---

## B. When the tenant ticket (A2) is resolved

### B1. Get the passwords

The Teigi service named in the ticket resolution (here `monit_rade`) holds two passwords. Members of the team e-group
can read them, but only from `aiadm`:

1. Log in to `aiadm` from your own machine (needs `ai-admins`, see *Before you start*):
   ```bash
   ssh <your CERN username>@aiadm.cern.ch
   ```
   Do not hop in from `lxplus`: connections from lxplus to aiadm are blocked on purpose
   ([KB0005353](https://cern.service-now.com/service-portal?id=kb_article&n=KB0005353)) and fail with
   `Connection refused`.
2. List the keys, then show each password:
   ```bash
   tbag showkeys --service monit_rade
   tbag show --service monit_rade monit_rade_password
   tbag show --service monit_rade monit_rade_ro_password
   ```
   The output is JSON; the password is the value of `"secret"`. The two keys are:

   | Key | User | Used for |
   |---|---|---|
   | `monit_rade_password` | `rade` | OTLP export (B2) and the Prometheus data source (C2) |
   | `monit_rade_ro_password` | `rade_ro` | The OpenSearch logs data source in Grafana (C3) |

   If `monit_rade_ro_password` is missing, the read-only user was not requested or not created yet: ask on the tenant
   ticket (A2).
3. Copy **only the value**, with no quotes and no spaces or line break around it, and keep both in your password
   manager. Each user only works with its own password: the `rade` password does **not** log in to OpenSearch, and
   `rade_ro` does not work for OTLP or Prometheus. A trailing space or line break makes a login fail with `401`.

### B2. Turn on the export

The services read `MONIT_ENABLED` and `MONIT_CREDENTIALS` from the secret `monit-tenant`. `MONIT_CREDENTIALS` must be
`base64("<tenant>:<password>")` with nothing extra: the app sends it as `Authorization: Basic <value>`. Use the tenant
user `rade` and the password `monit_rade_password` from B1 (not the read-only one).

Bash (the prompt keeps the password out of your shell history):

```bash
read -rsp 'MONIT password: ' PW; echo
oc create secret generic monit-tenant -n rade-services \
  --from-literal=MONIT_ENABLED=true \
  --from-literal=MONIT_CREDENTIALS="$(printf '%s' "rade:$PW" | base64 -w0)"
unset PW
```

PowerShell:

```powershell
$pw = (Read-Host "MONIT password").Trim()
$cred = [Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes("rade:$pw"))
oc create secret generic monit-tenant -n rade-services --from-literal=MONIT_ENABLED=true --from-literal=MONIT_CREDENTIALS=$cred
```

The pods read the secret only at start, so restart them. The restart drops open subscriptions; clients reconnect.

```bash
oc rollout restart deployment/japc deployment/japc-master -n rade-services
```

Check it worked, after about a minute:

```bash
# should print: rade
oc get secret monit-tenant -n rade-services -o jsonpath='{.data.MONIT_CREDENTIALS}' | base64 -d | base64 -d | sed 's/:.*//'
# should print: 0
oc logs deployment/japc -n rade-services --tail=500 | grep -c 'status code 401'
```

Then run `japc_subscriptions_active` in Grafana Explore on `MONIT metrics` (C2). It returns one series per deployment.

To change the password later, replace the secret and restart:

```bash
oc create secret generic monit-tenant -n rade-services --from-literal=MONIT_ENABLED=true \
  --from-literal=MONIT_CREDENTIALS="<new base64 value>" --dry-run=client -o yaml | oc apply -f -
```

### B3. Probe metrics in the tenant

The probe already carries the label `monit_shared: "rade"` (A1), but forwarding probe metrics into a tenant is **not
enabled by default**: MONIT turns it on per tenant when asked (A2), and it can take a while after the ticket answer.
Once it is on, `probe_success{remote_producer="rade"}` also appears in `MONIT metrics` in your Grafana organization.
If the ticket resolution does not say it is enabled, or the series is still missing hours later, ask on the tenant
ticket.

---

## C. When the Grafana organization ticket (A3) is resolved

### C1. Open the organization

1. Open https://monit-grafana.cern.ch and log in.
2. Click your profile picture (top right) → **Switch organization** → **RADE**.
3. Give the wider team access. Members of `rade-admins` are already Admins of the organization. The team e-group
   `be-dep-cem-mta` gets access through a Grafana **Team**: an org admin opens **Administration → Users and access →
   Teams → New team**, sets **Name** to `be-dep-cem-mta` and **Email** to `be-dep-cem-mta@cern.ch`, and saves. The
   team's membership then syncs from the e-group within about 30 minutes
   ([how it works](https://monit.web.cern.ch/dashboards/organizations/#e-group-synchronization-for-teams)).

   The same works for any other e-group: create a Team and set its email to the e-group address.

### C2. Data source: MONIT metrics

1. Open https://monit-grafana.cern.ch/connections/datasources/new and click **Prometheus**.
2. **Name:** `MONIT metrics`. Keep `MONIT` in the name: the dashboard in C5 only offers Prometheus data sources whose
   name contains it.
3. **Prometheus server URL:** `https://monit-prom-lts.cern.ch/prometheus/`
4. **Authentication method:** Basic authentication. **User:** `rade`. **Password:** `monit_rade_password` from B1.
5. If there is a switch **Manage alerts via Alerting UI**, turn it off.
6. Click **Save & test** at the bottom. Expect a green "Successfully queried the Prometheus API".

### C3. Data source: MONIT logs

Logs go to a data stream named `monit-logs-ss4o_rade` on `monit-timberprivate` (OpenSearch 3.4). You can always read
them in OpenSearch Dashboards at https://monit-timberprivate.cern.ch with CERN SSO.

OpenSearch has its own user. The tenant user `rade` and its password only work for OTLP and Prometheus, **not** for
OpenSearch. Grafana must use the read-only user **`rade_ro`** with its own password, `monit_rade_ro_password` (B1).
Basic auth is accepted on `/os`: the `WWW-Authenticate: Negotiate` header in a 401 answer only means the login failed.

1. Open https://monit-grafana.cern.ch/connections/datasources/new and click **OpenSearch** (if it is not in the list,
   reply on the org ticket asking for the OpenSearch plugin).
2. **Name:** `MONIT logs`. Keep `MONIT` in the name: the logs dashboard in C5 only offers OpenSearch data sources
   whose name contains it.
3. **URL:** `https://monit-timberprivate.cern.ch:443/os`. Access mode: Server (default).
4. **Basic auth** on. **User:** `rade_ro`. **Password:** `monit_rade_ro_password` from B1.
5. **Index name:** `monit-logs-ss4o_rade*`. **Pattern:** No pattern (a data stream has no date in its name).
   **Time field name:** `@timestamp`.
6. Under **Logs**: **Message field name** `body`, **Level field name** `severity.text`. Log lines then show the message
   and are coloured by level.
7. Click **Get Version and Save**. It should detect OpenSearch 3.4.0. Then click **Save & test**.

**Save & test can report `Index not found: monit-logs-ss4o_rade*` even when everything works.** Before the first logs
arrive the data stream does not exist yet. Afterwards the test still fails, because it looks for plain indices and the
logs live in a data stream. Check with **Explore**: choose `MONIT logs`, set **Lucene Query Type** to **Logs** and run.
Log lines appear when the connection works. A `401` instead means the wrong user or password (not `rade`).

### C4. Data source: PaaS metrics

1. Open https://monit-grafana.cern.ch/connections/datasources/new and click **Prometheus**.
2. **Name:** `PaaS metrics`. Keep `PaaS` in the name: the pods dashboard in C5 only offers Prometheus data sources
   whose name contains it.
3. **Prometheus server URL:** `https://user-project-metrics.paas.cern.ch`
4. **Authentication method:** No authentication.
5. **HTTP headers** → **Add header**: header `Authorization`, value `Bearer <token from A4>`.
6. Scroll to **Other**: **HTTP method** GET, **Custom query parameters** `namespace=rade-services`.
7. Click **Save & test**.
8. Open https://monit-grafana.cern.ch/explore, choose `PaaS metrics`, switch the query editor to **Code**, enter
   `kube_pod_container_status_restarts_total{container="japc"}` and click **Run query**. It should return one line
   per pod. If it returns nothing, skip the "Pod restarted" alert in C7 and ask PaaS support.

### C5. Dashboards

Three dashboards live in `deploy/grafana/`. Each one lists only the data sources of the right type whose name contains
the shown word, so create the data sources in C2 to C4 with those names first.

| File | Dashboard | Data source it offers |
|---|---|---|
| `japc-dashboard.json` | **RADE JAPC service**: requests, errors, subscriptions, RBAC sessions, heap, CPU, and a probe row (reachable, certificate expiry, response time, HTTP status) | Prometheus with `MONIT` in the name (`MONIT metrics`) |
| `japc-pods-dashboard.json` | **RADE JAPC pods**: pod running and ready, restarts, waiting and termination reason, memory and CPU against limits, network | Prometheus with `PaaS` in the name (`PaaS metrics`) |
| `japc-logs-dashboard.json` | **RADE JAPC logs**: error and warning counts, log volume by level, log lines, top loggers | OpenSearch with `MONIT` in the name (`MONIT logs`) |

For each file:

1. Open https://monit-grafana.cern.ch/dashboard/import
2. Click **Upload dashboard JSON file** and choose the file from this repository. Set **Folder** to `JAPC` (create it
   with **New** the first time; the alert rules in C7 use the same folder). To update an existing copy, import the
   file again, keep the folder `JAPC` and choose **Import (Overwrite)**. Importing into *Dashboards* instead moves
   the copy out of `JAPC`.
3. Click **Import**. At the top of the dashboard check the **Datasource**. The service dashboard also has
   **Deployment** (`japc`); the pods dashboard has **Deployment** (`japc`, `japc-master` or All); the logs dashboard
   has **Deployment** and **Level** (leave **All**).

Notes:

- The service dashboard fills once B2 is done. Use a short time range such as *Last 15 minutes* right after a restart.
  The request, error and subscription-update panels stay empty until the service has handled traffic, because MONIT
  only creates a counter after its first increment.
- The probe row at the bottom of the service dashboard needs MONIT to forward the probe metrics to your tenant (B3).
  Without that it shows "No data"; use MONIT's own Remote Probes Overview instead.
- The pods dashboard filters on pod names starting with `japc` (the **Deployment** selector offers `japc`,
  `japc-master` or All), because the OpenShift project also runs other workloads. The selector is a fixed list: a
  `label_values` query against `PaaS metrics` timed out, so add new deployments to the `deployment` variable by hand.
  The data source needs the custom query parameter `namespace=rade-services` (C4).
- The logs dashboard needs the **Message field name** and **Level field name** from C3. Its Lucene queries use the
  fields `severity.text` (level), `resource.service.name` (deployment), `body` (message) and
  `instrumentationScope.name` (logger).

### C6. Alert contact point and policy

1. Open https://monit-grafana.cern.ch/alerting/notifications and click **+ Add contact point**.
2. **Name:** `monit-alarms`. **Integration:** Webhook. **URL:** `http://monit-alarms.cern.ch:10016`. Click
   **Save contact point**.
3. Open https://monit-grafana.cern.ch/alerting/routes. On the **Default policy**, click **More → Edit**, set
   **Default contact point** to `monit-alarms`, click **Update default policy**.

### C7. Alert rules

For each row of the table below:

1. Open https://monit-grafana.cern.ch/alerting/new/alerting
2. **Rule name:** the Alert column.
3. **Define query and alert condition:** choose the Data source column, switch to **Code**, paste the Query column.
   Under **Expressions**, set the **Threshold** to the "Is above" value of the Condition column.
4. **Folder:** create `JAPC` the first time. **Evaluation group:** create `japc` with interval `1m` the first time.
   **Pending period:** the "for" value of the Condition column.
5. **Labels** → **Add labels**: `gni_entities` = `japc`, `snow_functional_element` = `LabVIEW Support`,
   `snow_auto_closing` = `true`, and `snow_watchlist` = the people to email about the ticket, comma-separated with no
   spaces: `be-dep-cem-mta@cern.ch,neo.michael.cass@cern.ch,odd.oyvind.andreassen@cern.ch,piotr.koziol@cern.ch`.
   The watch list is the only way you get email about a ticket: the caller is the GNI service account and the ticket
   is assigned to the LabVIEW Support group, so nobody else is notified.
   - **Use individual addresses.** ServiceNow mails the watch list exactly as written. In our tests the mail to an
     e-group address (`be-dep-cem-mta`, `rade-admins`) was logged as sent but never arrived, while every mail
     ServiceNow sends on its own goes to named people.
   - **`be-dep-cem-mta` stays in the list** in case group delivery starts working. Per KB0004555, ServiceNow can
     only mail an e-group that has no posting restrictions, or whose **Custom senders** contain **both**
     `service-desk` and `noreply-service-desk`. The KB's example also has **Restrict to authenticated senders**
     switched **off**. On https://groups-portal.web.cern.ch open the e-group, **Email Properties** → **Email posting
     restrictions** (its owner has to do this).
   - Keep the same list in `snow_watchlist` of the probe (`deploy/monit/probes.yaml`, A1) when people join or leave.

   Also add `email_to` = `rade-admins@cern.ch`. That makes MONIT's notification service (GNI) send a plain email
   itself, with no ServiceNow in between, and one rule can carry both the ticket and the email labels. It was tested:
   the alert record shows `targets: email` and `rade-admins` received the mail (after a delay). `email_cc`,
   `email_bcc`, `email_reply_to` and `email_send_ok` (also mail on recovery) exist too; see
   https://monit.docs.cern.ch/alerts/alertmanager/. Texts use `sms_to` (CERN numbers, `+4175411xxxx`) the same way.
6. **Annotations** → **Add custom annotation**: `gni_correlation` = `{{ $values.A.Value }}`.
7. Under **Configure no data and error handling**, set **Alert state if no data** to **Normal** and leave **Alert state
   if execution error** at its default (**Error**). No data is normal here: a counter such as `japc_requests` does not
   exist until its first increment, and `absent()` returns nothing while the metric is there. Setting it to *Alerting*
   would make "App not reporting" fire all the time.
8. Click **Save rule and exit**.

| Alert | Data source | Query | Condition |
|---|---|---|---|
| App not reporting | MONIT metrics | `absent(japc_subscriptions_active{job="japc"})` | is above 0, for 5m |
| CPU high | MONIT metrics | `process_cpu_usage{job="japc"}` | is above 0.8, for 10m |
| Heap nearly full | MONIT metrics | `sum(jvm_memory_used{job="japc",area="heap"}) / sum(jvm_memory_max{job="japc",area="heap"})` | is above 0.9, for 10m |
| Clients rate limited | MONIT metrics | `sum(rate(japc_requests{job="japc",status="429"}[5m]))` | is above 0, for 10m |
| Updates dropped | MONIT metrics | `sum(rate(japc_subscription_updates_dropped{job="japc"}[5m]))` | is above 0, for 15m |
| Internal errors | MONIT metrics | `sum(rate(japc_errors{job="japc",code="5004"}[5m]))` | is above 0.1, for 15m |
| Pod restarted | PaaS metrics | `increase(kube_pod_container_status_restarts_total{container="japc"}[15m])` | is above 0, for 0s |
| Probe failing | MONIT metrics | `min(probe_success{remote_producer="rade"})` | is **below** 1, for 5m |

**Probe failing** is the only rule with different labels: `gni_entities` = `japc`, `email_to` = `rade-admins@cern.ch`
and `email_send_ok` = `true`, and no `snow_*` labels. The remote probe (A1) opens its own ticket after 10 minutes, and
its labels accept only `snow_*`, so this rule is how an outage also reaches `rade-admins` by email. Use the
annotation `gni_correlation` = `probe_success = {{ $values.B.Value }}`. It needs `monit_shared` (B3).

MONIT drops the `_total` and `_bytes` suffixes that Prometheus adds, so the names above are the unsuffixed ones.
Counters such as `japc_requests` and `japc_errors` only appear after their first increment. Series from OTLP carry
only `job`, `area` and `id`-style labels, not `pod`.

The `JAPC` folder holds both the three dashboards (C5) and these alert rules. The rules are listed under **Alerting →
Alert rules**, not under **Dashboards**, so the dashboard list shows only the dashboards.

The rules only notify once C6 step 3 is done: until the default policy points at `monit-alarms`, they evaluate but
send nothing, so no SNOW ticket is opened.

Thresholds are starting points. If a rule shows "No data" for CPU or heap once B2 is done, look up the real metric
name in Explore (`MONIT metrics`, Metrics browser) and adjust the query.

### C8. Check alerts arrive

1. On the **Pod restarted** rule (its pending period is 0s, the others wait 5–15 minutes), temporarily set the
   threshold to `-1`, save, and wait 2–3 minutes. A real SNOW ticket opens, so tell LabVIEW Support first.
2. Open https://monit-opensearch.cern.ch and search the index pattern `monit_prod_alarm_raw_gni*` for `japc`.
   The alert should be there, and a SNOW ticket for LabVIEW Support should open.
3. Set the threshold back and save.

---

## Troubleshooting

| Symptom | Likely cause | What to do |
|---|---|---|
| `git push` to `monit-remote-probes` fails with `403` | Not a Developer on the project | Join `rade-admins` (it includes `ai-admins`), or use a fork (A1 step 4) |
| Merge request from a fork fails at `approve_user_mr` with `401` | The fork has no approval token | A MONIT team member must approve and merge by hand |
| `ssh aiadm` says `Connection refused` from lxplus | lxplus to aiadm is blocked on purpose | Connect from your own machine; needs `ai-admins` (included in `rade-admins`) |
| `ssh aiadm.cern.ch` asks for a password again and again | Wrong CERN account name or password, or no `ai-admins` access | Check the username, then confirm you are in `rade-admins` (which includes `ai-admins`) |
| Pod logs show `Failed to export … HTTP status code 401` | `MONIT_CREDENTIALS` is wrong, often a trailing space or line break from copy and paste | Recreate the secret as in B2, restart, rerun the checks |
| Probe not visible in the Remote Probes dashboard | **Producer** not set to `rade` | Pick `rade` in the first drop-down (A1 step 6) |
| Probe not visible in the `RADE` organization | MONIT has not enabled shared metrics for the tenant | Ask on the tenant ticket (B3) |
| Dashboard panels all say "No data" | Wrong data source, or no data yet | Select `MONIT metrics` and **Deployment** `japc`; check B2 |
| Request, error and update panels empty, others fine | MONIT creates a counter after its first increment | Send some traffic |
| `oc` says `You must be logged in to the server (Unauthorized)` | Login expired | `oc login --web https://api.paas.okd.cern.ch:443` |
| OpenSearch data source: "Failed to connect to server", or `401` | Wrong user or password. `rade` and its password do not work for OpenSearch | Use `rade_ro` with `monit_rade_ro_password` (C3). If that key does not exist, ask on the tenant ticket (A2) |
| OpenSearch Save & test says `Index not found: monit-logs-ss4o_rade*` | No log has arrived yet, or the test cannot see a data stream | Check in Explore with query type **Logs** (C3); wait for the first logs after B2 |
| Logs dashboard shows "No data" | Free-text or wildcard values in the variables (Grafana escapes `*`), or the data source has no **Message field name** / **Level field name** | Keep **Deployment** and **Level** on real values or **All**; set the field names in C3 |

---

## Request log level

Successful GETs are logged at DEBUG and therefore not sent to MONIT; their counts are in the `japc_requests` metric.
To log every request for a while (restarts the pod):

```bash
oc set env deployment/japc LOGGING_LEVEL_CERN_RADE_SERVICES_JAPC_STATS_REQUESTMETRICS=DEBUG -n rade-services
```

Undo:

```bash
oc set env deployment/japc LOGGING_LEVEL_CERN_RADE_SERVICES_JAPC_STATS_REQUESTMETRICS- -n rade-services
```
