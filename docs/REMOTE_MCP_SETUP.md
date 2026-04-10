# Remote MCP Setup

## Goal

This document explains how the launcher consumes remote bootstrap and exposes MCP endpoints that can be used by external clients.

## Source of truth

The control plane decides:

- public hostname
- tunnel token
- OAuth issuer
- JWKS
- audience
- scope

The launcher does not recalculate those values.

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

## Current resources

### Outlook

- public MCP URL: `https://<slug>-outlook-mcp.dartmaker.com/mcp`
- local port: `8080`
- audience: `outlookdesktop-mcp`
- scope: `mcp:tools`

### Campus

- public MCP URL: `https://<slug>-campus-mcp.dartmaker.com/mcp`
- local port: `8081`
- audience: `campus-mcp`
- scope: `campus:tools`

### qBid

- public MCP URL: `https://<slug>-qbid-mcp.dartmaker.com/mcp`
- local port: `8082`
- audience: `qbid-mcp`
- scope: `qbid:tools`

## Actual startup sequence per resource

1. validate that a desktop session exists
2. retrieve the bootstrap for the selected resource
3. start `cloudflared` with the remote token
4. start the local runtime of the resource
5. verify `/.well-known/oauth-protected-resource`
6. use the public URL from ChatGPT, Claude or another MCP client

## Useful verification endpoints

### Outlook local

- `http://localhost:8080/.well-known/oauth-protected-resource`
- `http://localhost:8080/mcp`

### Campus local

- `http://localhost:8081/.well-known/oauth-protected-resource`
- `http://localhost:8081/mcp`

### qBid local

- `http://localhost:8082/.well-known/oauth-protected-resource`
- `http://localhost:8082/mcp`

### Public

- `https://<slug>-outlook-mcp.dartmaker.com/.well-known/oauth-protected-resource`
- `https://<slug>-campus-mcp.dartmaker.com/.well-known/oauth-protected-resource`
- `https://<slug>-qbid-mcp.dartmaker.com/.well-known/oauth-protected-resource`

The URL registered in ChatGPT or Claude must be the MCP endpoint itself:

- `https://<slug>-outlook-mcp.dartmaker.com/mcp`
- `https://<slug>-campus-mcp.dartmaker.com/mcp`
- `https://<slug>-qbid-mcp.dartmaker.com/mcp`

Do not register the domain root.

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
