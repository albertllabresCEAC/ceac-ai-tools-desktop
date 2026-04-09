# CEAC IA Tools

Launcher desktop Windows para CEAC IA Tools. El launcher autentica contra el control plane central y expone runtimes MCP locales por recurso.

Estado actual del producto:

- una pestana `Login`
- una pestana `Outlook MCP`
- una pestana `QBid MCP`

El launcher no administra Cloudflare, DNS ni Keycloak. Todo eso viene resuelto desde `dartmaker-tunnel-control-plane`.

## Vision general

El producto tiene tres piezas:

1. `dartmaker-tunnel-control-plane`
   expone login, bootstrap, provisioning, Cloudflare y contrato OAuth central.
2. `CEAC IA Tools`
   es este launcher desktop; mantiene la sesion del usuario y arranca runtimes locales.
3. runtimes MCP locales
   hoy:
   - Outlook por COM
   - qBid por HTTP scraping

## Que hace el launcher

- hace login contra `https://control.dartmaker.com`
- recibe un `accessToken` desktop y los `bootstrap` disponibles por recurso
- arranca `cloudflared` local con el `tunnelToken` de cada recurso
- arranca el runtime local del recurso elegido
- publica metadata OAuth correcta para ChatGPT, Claude y otros clientes MCP

No hace:

- crear o borrar tunnels
- tocar DNS de Cloudflare
- administrar Keycloak
- almacenar secretos globales de Cloudflare

## Pestanas de la UI

### Login

Es la sesion contra el panel de control.

Campos visibles:

- `Usuario o email`
- `Password`
- `Machine ID` solo lectura
- `Client version` solo lectura
- `Control plane URL` solo en modo desarrollo

Acciones:

- `Iniciar sesion en panel de control`
- `Cerrar sesion`

El login devuelve contexto y bootstrap para todos los recursos habilitados del usuario.

### Outlook MCP

Es la operativa del runtime Outlook.

Muestra:

- `Tunnel ID`
- `Host`
- `Issuer`
- `JWKS`
- `Audience`
- `Scope`
- `Resource name`
- estado de prerequisitos
- estado del tunnel
- estado del runtime
- URL de Swagger

Acciones:

- `Validar`
- `Arrancar MCP`
- `Parar MCP`
- `Copiar MCP URL`
- `Abrir Swagger`
- `Copiar Swagger`

### QBid MCP

Es la operativa del runtime qBid.

Ademas de la informacion de bootstrap, pide credenciales locales de qBid:

- `Usuario qBid`
- `Password qBid`

Acciones:

- `Validar credenciales`
- `Validar estado`
- `Arrancar MCP`
- `Parar MCP`
- `Copiar MCP URL`
- `Abrir Swagger`
- `Copiar Swagger`

Esas credenciales qBid no viajan al backend ni a Keycloak. Se usan solo en local para arrancar el runtime qBid.

## Requisitos

- Windows
- Java 21
- Maven 3.9+
- `cloudflared` instalado o accesible por `PATH`
- Outlook Desktop instalado para `Outlook MCP`
- proyecto `qBidScrAPI` disponible en disco para `QBid MCP`

## Scripts

### `run.bat`

Es el arranque normal del producto.

Hace:

- fija `DARTMAKER_DEV_MODE=false`
- usa `https://control.dartmaker.com` como control plane por defecto
- abre el launcher Swing

No hace:

- levantar Postgres
- levantar Keycloak
- arrancar el control plane local

### `run-full-local.bat`

Es tooling de desarrollo.

Hace:

- activa `DARTMAKER_DEV_MODE=true`
- levanta PostgreSQL y Keycloak del control plane local
- arranca el control plane local
- abre el launcher

### `run-dev.bat`

Es un alias legado de `run-full-local.bat`.

## Variables de entorno utiles

### Flujo normal

- `CONTROL_PLANE_LOGIN_USERNAME`
- `CONTROL_PLANE_LOGIN_PASSWORD`
- `CONTROL_PLANE_MACHINE_ID`
- `CONTROL_PLANE_CLIENT_VERSION`
- `CLOUDFLARED_CMD`
- `CEAC_QBID_PROJECT_ROOT`

### Desarrollo

- `DARTMAKER_DEV_MODE=true`
- `CONTROL_PLANE_URL`
- `DEV_CONTROL_PLANE_ROOT`
- `DEV_CONTROL_PLANE_PORT`
- `DEV_CONTROL_PLANE_KEYCLOAK_PORT`

Ejemplo:

```powershell
$env:CONTROL_PLANE_LOGIN_USERNAME = "tu-usuario"
$env:CONTROL_PLANE_LOGIN_PASSWORD = "tu-password"
.\run.bat
```

## Flujo normal de uso

1. ejecutar `run.bat`
2. autenticarse en `Login`
3. ir a `Outlook MCP` o `QBid MCP`
4. validar estado si quieres una comprobacion previa
5. arrancar el recurso
6. usar la URL publica `https://<slug>-<resource>.dartmaker.com/mcp` desde ChatGPT, Claude u otro cliente

## Flujo OAuth resumido

El launcher solo soporta `CENTRAL_AUTH`.

Eso significa:

- issuer publico central
- JWKS publico central
- audience y scope definidos por recurso
- nada de `localhost` como issuer para clientes externos

Valores tipicos:

- Outlook:
  - audience `outlookdesktop-mcp`
  - scope `mcp:tools`
- qBid:
  - audience `qbid-mcp`
  - scope `qbid:tools`

## URLs locales utiles

- Outlook Swagger: `http://localhost:8080/swagger-ui/index.html`
- Outlook MCP: `http://localhost:8080/mcp`
- qBid Swagger: `http://localhost:8082/swagger-ui/index.html`
- qBid MCP: `http://localhost:8082/mcp`

## Logs y archivos generados

- `logs/launcher-*.log`
- `logs/cloudflared-*.stdout.log`
- `logs/cloudflared-*.stderr.log`
- `logs/qbid-runtime-*.stdout.log`
- `logs/qbid-runtime-*.stderr.log`
- `.env.generated`

## Documentacion interna

- [docs/LAUNCHER_ARCHITECTURE.md](/C:/Users/alber/Documents/IdeaProjects/OutlookDesktop_COM_MCP/docs/LAUNCHER_ARCHITECTURE.md)
- [docs/REMOTE_MCP_SETUP.md](/C:/Users/alber/Documents/IdeaProjects/OutlookDesktop_COM_MCP/docs/REMOTE_MCP_SETUP.md)
- [docs/DEVELOPMENT_WORKFLOW.md](/C:/Users/alber/Documents/IdeaProjects/OutlookDesktop_COM_MCP/docs/DEVELOPMENT_WORKFLOW.md)

## Estado tecnico

El launcher ya esta adaptado a multi-recurso, pero el nombre historico del paquete Java sigue siendo `outlookdesktop` para evitar una migracion de paquetes con bajo retorno. El branding visible del producto ya es `CEAC IA Tools`.
