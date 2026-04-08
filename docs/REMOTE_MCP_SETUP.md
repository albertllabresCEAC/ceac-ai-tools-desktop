# Remote MCP setup

## Objetivo

Este cliente ya no gestiona Cloudflare, DNS ni Keycloak por su cuenta. El flujo correcto es:

1. el `dartmaker-tunnel-control-plane` autentica al usuario desktop
2. el control plane provisiona o recupera el tunnel y devuelve bootstrap
3. `OutlookDesktop_COM_MCP` consume ese contexto
4. el cliente arranca `cloudflared` con el `tunnelToken`
5. la app local publica el MCP protegido por el OAuth central del backend

## Fuente de verdad

El payload de bootstrap es la fuente de verdad para el runtime local. El cliente no debe inventar ni recalcular:

- hostnames publicos
- issuer
- JWK set
- audience
- scope

Todo eso viene del backend.

## Payload esperado

El control plane devuelve, como minimo:

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
- `cloudflaredManagedRemotely`

En `CENTRAL_AUTH` ademas se espera:

- `authHostname = null`
- `authPublicBaseUrl = null`

## Modo soportado por el launcher desktop

El launcher desktop actual exige `CENTRAL_AUTH`.

En este modo el bootstrap devuelve:

- `mcpPublicBaseUrl` por usuario
- `issuerUri` global
- `jwkSetUri` global
- `requiredAudience` global
- `requiredScope` global

El launcher:

- arranca `cloudflared`
- no arranca Keycloak local
- no necesita exponer `slug-auth`
- consume `issuerUri` y `jwkSetUri` ya calculados por el backend

Si el backend se configura en `LOCAL_AUTH`, el launcher bloquea el arranque del MCP porque ese modo ya no forma parte del runtime soportado por el desktop.

## Variables del launcher

Puedes definirlas a mano o dejar que `run.bat` cargue los defaults.

Variables:

- `CONTROL_PLANE_URL`
- `CONTROL_PLANE_LOGIN_USERNAME`
- `CONTROL_PLANE_LOGIN_PASSWORD`
- `CONTROL_PLANE_MACHINE_ID`
- `CONTROL_PLANE_CLIENT_VERSION`
- `CLOUDFLARED_CMD`

Ejemplo manual:

```powershell
$env:CONTROL_PLANE_URL = "https://control.example.com"
$env:CONTROL_PLANE_LOGIN_USERNAME = "desktop-user"
$env:CONTROL_PLANE_LOGIN_PASSWORD = "change-me"
$env:CONTROL_PLANE_MACHINE_ID = $env:COMPUTERNAME
$env:CONTROL_PLANE_CLIENT_VERSION = "1.0.0"
```

## Arranque recomendado

### Entorno central

```powershell
.\run.bat
```

Flujo esperado en la UI:

1. en `Login`, el usuario se autentica contra el control plane
2. el backend devuelve `bootstrap`
3. en `MCP`, el launcher arranca `cloudflared`
4. en `MCP`, el launcher arranca la app Spring Boot local

### Entorno local completo

```powershell
.\run-full-local.bat
```

Este script:

- levanta PostgreSQL y Keycloak del control plane
- arranca el control plane local
- precarga credenciales de desktop
- abre el launcher

`run-dev.bat` sigue existiendo como alias legado, pero el nombre recomendado para este flujo es `run-full-local.bat`.

## Que hace el launcher al arrancar el MCP

1. valida que existe sesion activa
2. valida que el bootstrap usa `CENTRAL_AUTH`
3. valida que `cloudflared` esta disponible
4. arranca o reutiliza `cloudflared`
5. verifica `127.0.0.1:20241/metrics`
6. escribe `.env.generated`
7. arranca Spring Boot local con las properties derivadas

## Verificacion local

Comprueba localmente:

```text
http://localhost:8080/.well-known/oauth-protected-resource
http://localhost:8080/mcp
```

Y publicamente, si el tunnel esta arriba:

```text
https://<slug>-mcp.<base-domain>/.well-known/oauth-protected-resource
https://<slug>-mcp.<base-domain>/mcp
```

## Contrato OAuth del recurso protegido

La app local publica:

- `authorization_servers`
- `scopes_supported`
- `resource`

Y valida:

- `iss`
- `aud`
- `scope`

Claims esperados:

- `iss`: debe coincidir con `MCP_OAUTH_ISSUER_URI`
- `aud`: debe incluir `MCP_OAUTH_REQUIRED_AUDIENCE`
- `scope`: debe incluir `MCP_OAUTH_REQUIRED_SCOPE`

## Uso desde ChatGPT o Claude

La URL que se debe registrar en el cliente MCP es:

- `https://<slug>-mcp.<base-domain>/mcp`

No la raiz del dominio.

Ademas, para que funcione:

- el launcher debe haber terminado `Login -> Arrancar MCP`
- el issuer publicado no puede ser `localhost`
- Keycloak debe permitir Dynamic Client Registration para el host del cliente MCP

## Notas operativas

- Si el PC esta apagado, el MCP deja de estar disponible.
- Outlook COM sigue siendo local y Windows-only.
- El control plane es el unico que debe gestionar Cloudflare Tunnel y DNS.
- El cliente no debe volver a crear rutas DNS ni hacer login administrativo a Cloudflare.
