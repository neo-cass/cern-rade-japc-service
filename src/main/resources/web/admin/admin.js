"use strict";

const API = "/admin/api";
const REFRESH_MS = 5000;
const OPERATIONS = ["GET", "SET", "SUBSCRIBE"];

const panels = {
    info: loadInfo,
    sessions: loadSessions,
    subscriptions: loadSubscriptions,
    clients: loadClients,
};
let activeTab = "info";

// --- HTTP ---

function csrfToken() {
    const cookie = document.cookie.split("; ").find(entry => entry.startsWith("XSRF-TOKEN="));
    return cookie ? decodeURIComponent(cookie.substring("XSRF-TOKEN=".length)) : "";
}

async function request(method, path) {
    const headers = method === "GET" ? {} : {"X-XSRF-TOKEN": csrfToken()};
    const response = await fetch(API + path, {method, headers, credentials: "same-origin", cache: "no-store"});
    if (response.status === 401) {
        window.location.reload();
    }
    if (!response.ok) {
        throw new Error(method + " " + path + " failed with " + response.status);
    }
    return response.status === 204 ? null : response.json();
}

// --- Rendering helpers (DOM only, never innerHTML, so server data cannot inject markup) ---

function element(tag, text, className) {
    const node = document.createElement(tag);
    if (text !== undefined && text !== null) {
        node.textContent = text;
    }
    if (className) {
        node.className = className;
    }
    return node;
}

function button(label, onClick, className) {
    const node = element("button", label, className);
    node.addEventListener("click", onClick);
    return node;
}

function time(value) {
    return value ? new Date(value).toLocaleString() : "–";
}

function duration(iso) {
    const match = /^PT(?:(\d+)H)?(?:(\d+)M)?(?:([\d.]+)S)?$/.exec(iso || "");
    if (!match) {
        return iso || "–";
    }
    const [hours, minutes, seconds] = match.slice(1).map(part => Math.floor(Number(part || 0)));
    const days = Math.floor(hours / 24);
    return [days && days + "d", hours % 24 && (hours % 24) + "h", minutes && minutes + "m", seconds && seconds + "s"]
        .filter(Boolean).join(" ") || "0s";
}

/**
 * columns: [{label, value: row => string | Node, number: boolean}]
 * group (optional): {key: row => string, header: (key, rows) => Node[]}; rows must arrive sorted by key, and each
 * group starts with a full-width header row.
 */
function renderTable(table, columns, rows, emptyText, group) {
    const head = element("tr");
    columns.forEach(column => head.append(element("th", column.label)));
    const body = [];
    rows.forEach((row, index) => {
        if (group && (index === 0 || group.key(rows[index - 1]) !== group.key(row))) {
            const key = group.key(row);
            const td = element("td", null, "group");
            td.colSpan = columns.length;
            td.append(...group.header(key, rows.filter(other => group.key(other) === key)));
            const tr = element("tr");
            tr.append(td);
            body.push(tr);
        }
        const tr = element("tr");
        columns.forEach(column => {
            const value = column.value(row);
            const td = element("td", null, column.number ? "number" : "");
            td.append(value instanceof Node ? value : String(value ?? "–"));
            tr.append(td);
        });
        body.push(tr);
    });
    if (rows.length === 0) {
        const td = element("td", emptyText, "empty");
        td.colSpan = columns.length;
        const tr = element("tr");
        tr.append(td);
        body.push(tr);
    }
    const thead = element("thead");
    thead.append(head);
    const tbody = element("tbody");
    tbody.append(...body);
    table.replaceChildren(thead, tbody);
}

// --- Panels ---

async function loadMe() {
    const me = await request("GET", "/me");
    document.getElementById("me").textContent = me.user;
    document.getElementById("me-roles").textContent = "(" + me.loginType.toLowerCase() + ", " + me.roles.join(", ") + ")";
}

async function loadInfo() {
    const info = await request("GET", "/info");
    const rows = [
        ["Version", info.version],
        ["Started", time(info.startedAt)],
        ["Uptime", duration(info.uptime)],
        ["RBAC environment", info.rbacEnvironment],
        ["Required role", info.requiredRole],
        ["Admin role", info.adminRole],
        ["Token lifetime", duration(info.tokenLifetime)],
        ["Session lifetime", duration(info.sessionLifetime)],
        ["Session idle timeout", duration(info.sessionIdleTimeout)],
        ["Cached sessions", info.sessions],
        ["Open subscriptions", info.subscriptions],
    ];
    document.getElementById("info").replaceChildren(...rows.flatMap(([label, value]) => [element("dt", label), element("dd", value)]));
}

async function loadSessions() {
    const sessions = await request("GET", "/sessions");
    renderTable(document.getElementById("sessions"), [
        {label: "Login", value: session => session.loginType.toLowerCase()},
        {label: "Address", value: session => session.address},
        {label: "Client", value: session => session.userAgent},
        {label: "Roles", value: session => session.roles.join(", ")},
        {label: "Last used", value: session => time(session.lastUsed)},
        {label: "Expires", value: session => time(session.sessionExpiry)},
        {label: "", value: session => button("Revoke", () => act("DELETE", "/sessions/" + encodeURIComponent(session.handle)), "danger")},
    ], sessions, "No cached sessions", {
        key: session => session.user,
        header: (user, userSessions) => [
            element("strong", user),
            element("span", " · " + userSessions.length + (userSessions.length === 1 ? " session " : " sessions "), "muted"),
            button("Revoke all for " + user, () => act("DELETE", "/users/" + encodeURIComponent(user) + "/sessions"), "danger"),
        ],
    });
}

async function loadSubscriptions() {
    const subscriptions = await request("GET", "/subscriptions");
    renderTable(document.getElementById("subscriptions"), [
        {label: "Parameter", value: subscription => subscription.parameter},
        {label: "Selector", value: subscription => subscription.selector},
        {label: "Format", value: subscription => subscription.format},
        {label: "User", value: subscription => subscription.user},
        {label: "Address", value: subscription => subscription.address},
        {label: "Since", value: subscription => time(subscription.openedAt)},
        {label: "", value: subscription => button("Close", () => act("DELETE", "/subscriptions/" + encodeURIComponent(subscription.id)), "danger")},
    ], subscriptions, "No open subscriptions");
}

async function loadClients() {
    const clients = await request("GET", "/clients");
    const operationColumns = OPERATIONS.flatMap(operation => [
        {label: operation + " / min", value: client => client.operations[operation].lastMinute, number: true},
        {label: operation + " total", value: client => client.operations[operation].total, number: true},
    ]);
    renderTable(document.getElementById("clients"), [
        {label: "User", value: client => client.user},
        {label: "Address", value: client => client.address},
        ...operationColumns,
        {label: "Last seen", value: client => time(client.lastSeen)},
    ], clients, "No requests yet");
}

// --- Actions, tabs and refresh ---

async function act(method, path) {
    await request(method, path);
    await refresh();
}

async function refresh() {
    const error = document.getElementById("error");
    try {
        await panels[activeTab]();
        error.hidden = true;
        document.getElementById("refreshed").textContent = new Date().toLocaleTimeString();
    } catch (e) {
        error.textContent = e.message;
        error.hidden = false;
    }
}

function selectTab(tab) {
    activeTab = tab;
    document.querySelectorAll("[data-tab]").forEach(node => node.setAttribute("aria-selected", String(node.dataset.tab === tab)));
    document.querySelectorAll("[data-panel]").forEach(node => node.hidden = node.dataset.panel !== tab);
    refresh();
}

document.querySelectorAll("[data-tab]").forEach(node => node.addEventListener("click", () => selectTab(node.dataset.tab)));
document.querySelector("[data-action=revoke-all]").addEventListener("click", () => {
    if (window.confirm("Revoke all cached RBAC sessions? Every client will have to log in again.")) {
        act("DELETE", "/sessions");
    }
});

loadMe().catch(e => document.getElementById("me").textContent = e.message);
refresh();
setInterval(refresh, REFRESH_MS);
