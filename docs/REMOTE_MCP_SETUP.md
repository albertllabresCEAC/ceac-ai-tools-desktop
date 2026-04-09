# Remote MCP Setup

## Objetivo

Este documento explica como el launcher consume el bootstrap remoto y publica un MCP utilizable por clientes externos.

## Fuente de verdad

El backend es quien decide:

- hostname publico
- tunnel token
- issuer OAuth
- JWKS
- audience
- scope

El launcher no recalcula esos valores.

## Contrato minimo de bootstrap

Cada recurso devuelve:

- `resourceKey`
- `displayName`
- `tunnelId`
- `tunnelToken`
- `mcpHostname`
- `mcpPublicBaseUrl`
- `issuerUri`
- `jwkSetUri`
- `requiredAudience`
- `requiredScope`
- `resourceName`
- `localPort`
- `authExposureMode`

## Recursos actuales

### Outlook

- hostname: `https://<slug>-outlook-mcp.dartmaker.com/mcp`
- local port: `8080`
- audience: `outlookdesktop-mcp`
- scope: `mcp:tools`

### qBid

- hostname: `https://<slug>-qbid-mcp.dartmaker.com/mcp`
- local port: `8082`
- audience: `qbid-mcp`
- scope: `qbid:tools`

## Arranque real por recurso

1. validar que existe sesion
2. recuperar bootstrap del recurso
3. arrancar `cloudflared` con su token
4. arrancar el runtime local del recurso
5. comprobar `/.well-known/oauth-protected-resource`
6. usar la URL publica desde ChatGPT o Claude

## Verificaciones utiles

### Outlook local

- `http://localhost:8080/.well-known/oauth-protected-resource`
- `http://localhost:8080/mcp`

### qBid local

- `http://localhost:8082/.well-known/oauth-protected-resource`
- `http://localhost:8082/mcp`

### Publico

- `https://<slug>-outlook-mcp.dartmaker.com/.well-known/oauth-protected-resource`
- `https://<slug>-outlook-mcp.dartmaker.com/mcp`
- `https://<slug>-qbid-mcp.dartmaker.com/.well-known/oauth-protected-resource`
- `https://<slug>-qbid-mcp.dartmaker.com/mcp`

## Clientes MCP externos

La URL que se registra en ChatGPT o Claude es siempre la del endpoint MCP:

- `https://<slug>-outlook-mcp.dartmaker.com/mcp`
- `https://<slug>-qbid-mcp.dartmaker.com/mcp`

No la raiz del dominio.

## Problemas tipicos

### El cliente externo dice que el servidor no soporta OAuth

Revisar:

- `/.well-known/oauth-protected-resource`
- que `authorization_servers` apunte a `https://auth.dartmaker.com/...`
- que no haya `localhost` en issuer o JWKS

### El cliente externo no puede llegar al servidor

Revisar:

- tunnel arriba
- DNS correcto en Cloudflare
- handshake TLS correcto
- `cloudflared` local vivo

### El login OAuth falla

Revisar:

- audience y scope del recurso
- dynamic client registration en Keycloak
- whitelist de hosts de ChatGPT y Claude
