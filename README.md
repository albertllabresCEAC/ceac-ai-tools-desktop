# CEAC AI Tools Desktop

Windows desktop launcher for CEAC AI Tools.

The launcher authenticates against the central control plane and exposes local MCP runtimes per resource.

Current product tabs:

- `Login`
- `Outlook MCP`
- `Campus MCP`
- `QBid MCP`

The launcher does not manage Cloudflare, DNS or Keycloak administration directly. Those concerns stay in the control plane.

## Product structure

The desktop application now contains three layers:

1. control-plane session shell
   - login against `https://control.dartmaker.com`
   - resource bootstrap
   - local tunnel lifecycle
2. local MCP runtimes
   - Outlook via COM
   - Campus via embedded JCEF login plus HTTP session reuse
   - qBid via HTTP scraping
3. public MCP exposure
   - `cloudflared` per resource
   - OAuth metadata per resource
   - public MCP URL consumed by ChatGPT, Claude and others

All three runtimes now live inside the same Maven project.

## Package map

- `tools.ceac.ai.desktop`
  - desktop shell and product entry point
- `tools.ceac.ai.desktop.launcher`
  - control-plane integration, bootstrap handling and `cloudflared`
- `tools.ceac.ai.desktop.ui`
  - Swing user interface
- `tools.ceac.ai.mcp.outlook`
  - Outlook runtime
- `tools.ceac.ai.mcp.campus`
  - Campus runtime
- `tools.ceac.ai.mcp.qbid`
  - qBid runtime

## What the launcher does

- logs in against the control plane
- receives desktop token plus bootstrap per resource
- starts one local `cloudflared` process per resource
- starts the corresponding local runtime
- publishes correct OAuth metadata for external MCP clients

It does not:

- create or delete tunnels directly in Cloudflare
- edit public DNS directly
- manage Keycloak realms or trusted-host rules
- store global Cloudflare secrets

## Tabs

### Login

This is the session against the control plane.

Visible fields:

- `Usuario o email`
- `Password`
- `Machine ID` read-only
- `Client version` read-only
- `Control plane URL` only in development mode

Actions:

- `Iniciar sesion en panel de control`
- `Cerrar sesion`

The result of login is a `ControlPlaneSession` that carries bootstrap for every available resource.

### Outlook MCP

Shows bootstrap and runtime state for Outlook.

It displays:

- tunnel id
- host
- issuer
- JWKS
- audience
- scope
- resource name
- prerequisite state
- tunnel state
- runtime state
- Swagger URL

Actions:

- `Validar`
- `Arrancar MCP`
- `Parar MCP`
- `Copiar MCP URL`
- `Abrir Swagger`
- `Copiar Swagger`

### Campus MCP

Shows bootstrap and runtime state for Campus and embeds the Campus login browser inside the tab.

Campus differs from qBid:

- it does not ask for username and password in Swing fields
- login happens directly inside the embedded JCEF browser
- Moodle cookies are copied to the Java HTTP client and then reused by REST and MCP

Actions:

- `Validar estado`
- `Arrancar MCP`
- `Parar MCP`
- `Copiar MCP URL`
- `Abrir Swagger`
- `Copiar Swagger`
- `Reiniciar login Campus`

### QBid MCP

Shows bootstrap and runtime state for qBid.

It also asks for local qBid credentials:

- `Usuario qBid`
- `Password qBid`

Actions:

- `Validar credenciales`
- `Validar estado`
- `Arrancar MCP`
- `Parar MCP`
- `Copiar MCP URL`
- `Abrir Swagger`
- `Copiar Swagger`

Those qBid credentials stay local. They are not sent to the control plane or stored in Keycloak.

## Requirements

- Windows
- Java 21
- Maven 3.9+
- `cloudflared` installed or available on `PATH`
- Outlook Desktop installed for Outlook MCP
- access to qBid or sBid for QBid MCP
- access to `campus.ceacfp.es` for Campus MCP

Campus also uses:

- `jcef-bundle/`
- `jcef-cache/`

The first Campus run may take longer because the embedded browser runtime is prepared locally.

## Scripts

### `run.bat`

Normal product startup.

It:

- forces `DARTMAKER_DEV_MODE=false`
- uses `https://control.dartmaker.com` as the control-plane URL
- opens the Swing launcher

It does not:

- start PostgreSQL
- start Keycloak
- start the local control plane

### `run-full-local.bat`

Full local development startup.

It:

- enables `DARTMAKER_DEV_MODE=true`
- starts PostgreSQL and Keycloak for the local control plane
- starts the local control plane
- opens the launcher

### `run-dev.bat`

Legacy alias for `run-full-local.bat`.

## Useful environment variables

### Normal usage

- `CONTROL_PLANE_LOGIN_USERNAME`
- `CONTROL_PLANE_LOGIN_PASSWORD`
- `CONTROL_PLANE_MACHINE_ID`
- `CONTROL_PLANE_CLIENT_VERSION`
- `CLOUDFLARED_CMD`

### Development

- `DARTMAKER_DEV_MODE=true`
- `CONTROL_PLANE_URL`
- `DEV_CONTROL_PLANE_ROOT`
- `DEV_CONTROL_PLANE_PORT`
- `DEV_CONTROL_PLANE_KEYCLOAK_PORT`

Example:

```powershell
$env:CONTROL_PLANE_LOGIN_USERNAME = "your-user"
$env:CONTROL_PLANE_LOGIN_PASSWORD = "your-password"
.\run.bat
```

## Runtime ports

- Outlook local runtime: `8080`
- Campus local runtime: `8081`
- qBid local runtime: `8082`

`cloudflared` metrics ports are separate and managed per resource by the launcher.

## Normal usage flow

1. run `run.bat`
2. authenticate in `Login`
3. open `Outlook MCP`, `Campus MCP` or `QBid MCP`
4. validate state if you want a pre-flight check
5. start the resource
6. use the public URL `https://<slug>-<resource>-mcp.dartmaker.com/mcp` from ChatGPT, Claude or another MCP client

## OAuth summary

The launcher only supports `CENTRAL_AUTH`.

That means:

- public central issuer
- public central JWKS
- audience and scope defined per resource
- no localhost issuer for external clients

Typical values:

- Outlook:
  - audience `outlookdesktop-mcp`
  - scope `mcp:tools`
- Campus:
  - audience `campus-mcp`
  - scope `campus:tools`
- qBid:
  - audience `qbid-mcp`
  - scope `qbid:tools`

## Useful local URLs

- Outlook Swagger: `http://localhost:8080/swagger-ui/index.html`
- Outlook MCP: `http://localhost:8080/mcp`
- Campus Swagger: `http://localhost:8081/swagger-ui/index.html`
- Campus MCP: `http://localhost:8081/mcp`
- qBid Swagger: `http://localhost:8082/swagger-ui/index.html`
- qBid MCP: `http://localhost:8082/mcp`

## Generated files and logs

- `logs/launcher-*.log`
- `logs/cloudflared-*.stdout.log`
- `logs/cloudflared-*.stderr.log`
- `.env.generated`

These are runtime artifacts and should not be treated as source-of-truth documentation.

## Internal docs

- [`docs/LAUNCHER_ARCHITECTURE.md`](docs/LAUNCHER_ARCHITECTURE.md)
- [`docs/REMOTE_MCP_SETUP.md`](docs/REMOTE_MCP_SETUP.md)
- [`docs/DEVELOPMENT_WORKFLOW.md`](docs/DEVELOPMENT_WORKFLOW.md)
