# Remote MCP Setup

## Goal

This document explains how the launcher consumes remote bootstrap, exposes public MCP endpoints for external clients and keeps the local REST API restricted to the local machine.

## Source of truth

The control plane decides:

- public hostname
- tunnel token
- OAuth issuer
- JWKS
- audience
- scope

The launcher does not recalculate those values.

The public hostname also comes from the control plane. It is based on the username-derived slug of
the operator, not on the display name shown in the UI.

The launcher does mint one local API token per resource after desktop login, but those tokens are
strictly local operational tokens. They are not part of the control-plane bootstrap contract.

Public MCP exposure is stateless HTTP on `/mcp`. The launcher does not depend on server-side
session affinity for external MCP clients.

## Minimal bootstrap contract

Each resource returns:

- `resourceKey`
- `displayName`
- `localPort`
- `tunnelId`
- `tunnelToken`
- `mcpHostname`
- `mcpPublicBaseUrl`
- `issuerUri`
- `jwkSetUri`
- `requiredAudience`
- `requiredScope`
- `resourceName`
- `authExposureMode`

## Current resource modules

### Outlook module

- public MCP URL: `https://<slug>-outlook.dartmaker.com/mcp`
- local port: `8080`
- audience: `ceac-ia-tools`
- scope: `outlook:tools`

### Campus module

- public MCP URL: `https://<slug>-campus.dartmaker.com/mcp`
- local port: `8081`
- audience: `campus-mcp`
- scope: `campus:tools`

### qBid module

- public MCP URL: `https://<slug>-qbid.dartmaker.com/mcp`
- local port: `8082`
- audience: `qbid-mcp`
- scope: `qbid:tools`

### Trello module

- public MCP URL: `https://<slug>-trello.dartmaker.com/mcp`
- local port: `8083`
- audience: `trello-mcp`
- scope: `trello:tools`

The current DNS pattern intentionally omits any `-mcp` suffix in the hostname. Internal display
names may still say `Outlook MCP`, `QBid MCP`, `Campus MCP` and `Trello MCP`, but the public DNS
names stay in the form `username-resource.dartmaker.com`.

## Actual startup sequence per resource module

1. validate that a desktop session exists
2. retrieve the bootstrap for the selected resource module
3. start `cloudflared` with the remote token
4. start the local runtime of the resource module
5. verify `/.well-known/oauth-protected-resource`
6. use the public URL from ChatGPT, Claude or another MCP client

In parallel, the launcher also prepares:

7. a local-only Swagger URL on `http://localhost:<port>/swagger-ui/index.html`
8. a local API token valid only for the local REST API of that resource

If the control plane adds a new resource to the catalog, the launcher sees it only after the next
desktop login because `resources` is refreshed during `POST /api/client/login`.

## Useful verification endpoints

### Outlook local

- `http://localhost:8080/.well-known/oauth-protected-resource`
- `http://localhost:8080/mcp`
- `http://localhost:8080/swagger-ui/index.html`
- `http://localhost:8080/api/...` only from the local machine with the launcher-issued local token

### Campus local

- `http://localhost:8081/.well-known/oauth-protected-resource`
- `http://localhost:8081/mcp`
- `http://localhost:8081/swagger-ui/index.html`
- `http://localhost:8081/api/...` only from the local machine with the launcher-issued local token

### qBid local

- `http://localhost:8082/.well-known/oauth-protected-resource`
- `http://localhost:8082/mcp`
- `http://localhost:8082/swagger-ui/index.html`
- `http://localhost:8082/api/...` only from the local machine with the launcher-issued local token

### Trello local

- `http://localhost:8083/.well-known/oauth-protected-resource`
- `http://localhost:8083/mcp`
- `http://localhost:8083/swagger-ui/index.html`
- `http://localhost:8083/api/...` only from the local machine with the launcher-issued local token

### Public

- `https://<slug>-outlook.dartmaker.com/.well-known/oauth-protected-resource`
- `https://<slug>-campus.dartmaker.com/.well-known/oauth-protected-resource`
- `https://<slug>-qbid.dartmaker.com/.well-known/oauth-protected-resource`
- `https://<slug>-trello.dartmaker.com/.well-known/oauth-protected-resource`

The URL registered in ChatGPT or Claude must be the MCP endpoint itself:

- `https://<slug>-outlook.dartmaker.com/mcp`
- `https://<slug>-campus.dartmaker.com/mcp`
- `https://<slug>-qbid.dartmaker.com/mcp`
- `https://<slug>-trello.dartmaker.com/mcp`

Do not register the domain root.

Trello-specific note:

- the launcher captures the Trello operator token locally through `http://127.0.0.1:43127/trello/callback`
- that callback origin must be allowed in the Trello app configuration

## Local API contract

The local REST API and Swagger are intentionally different from the public MCP contract:

- they bind to `127.0.0.1`
- they reject forwarded or tunnel-style requests
- they are intended for operator checks, parser debugging and manual Swagger tests
- they accept launcher-issued local tokens, not tokens returned by the control plane

This means:

- `MCP` remains the remotely reachable surface
- `API` and `Swagger` remain local-only operational surfaces

## Common problems

### External client says the server does not implement OAuth

Check:

- `/.well-known/oauth-protected-resource`
- `authorization_servers` points to `https://auth.dartmaker.com/...`
- no localhost issuer or JWKS leaked into the runtime

### External client cannot reach the server

Check:

- tunnel is alive
- DNS is correct in Cloudflare
- TLS handshake succeeds
- local `cloudflared` is still running

### OAuth login fails with invalid scope

Check:

- resource audience and scope
- Keycloak Dynamic Client Registration
- trusted-host configuration for ChatGPT and Claude

### Public tunnel points to the wrong local port

Check:

- the resource was reprovisioned after changing its origin port
- the stored ingress configuration in Cloudflare matches the runtime port

### Swagger works but external MCP login fails

Swagger only proves the local runtime is alive.

External MCP login additionally depends on:

- correct public hostname
- tunnel pointing to the right local origin
- correct OAuth metadata
- correct issuer, audience and scope

### Public MCP works but local API returns `403`

Check:

- the call is really hitting `localhost`
- no forwarding headers are being injected by a proxy or browser plugin
- you are using the local token shown in the launcher for that specific resource
