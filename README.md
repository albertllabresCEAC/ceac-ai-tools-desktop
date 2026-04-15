# CEAC AI Tools Desktop

Windows desktop product for CEAC AI Tools.

The desktop shell authenticates against the central control plane and then orchestrates one local runtime per enabled resource module.

The launcher refreshes the resource catalog only during desktop login. If the control plane enables
or repairs a resource later, the operator must sign out and sign in again to reload
`ControlPlaneSession.resources`.

Current product tabs:

- `Login`
- `Outlook bot`
- `QBid bot`
- `Campus bot`
- `Trello bot`

## Known limitations

- Campus PDF resource creation and upload are currently unstable.
- The `createPdfResource` flow depends on Moodle draft-file upload plus the final `modedit`
  submission, and either step may fail depending on the current Moodle response shape.
- Treat Campus resource upload as experimental for now. Prefer manual Moodle upload when the
  resource is operationally important.
- Campus assignment creation now exists through REST and MCP, but it still depends on the live
  Moodle `modedit.php` form shape. If Moodle changes that form, the serializer may need to be
  updated again.

The launcher does not manage Cloudflare, DNS or Keycloak administration directly. Those concerns stay in the control plane.

## Product structure

The desktop application now contains two top-level areas:

1. desktop shell
   - login against `https://control.dartmaker.com`
   - resource bootstrap
   - local tunnel lifecycle
   - shared runtime orchestration
2. resource modules
   - Outlook via COM
   - Campus via embedded JCEF login plus HTTP session reuse
   - qBid via HTTP scraping
   - Trello via browser authorization plus REST API
3. public MCP exposure per resource
   - `cloudflared` per resource
   - OAuth metadata per resource
   - public MCP URL consumed by ChatGPT, Claude and others

All resource modules now live inside the same Maven project and follow the same broad package vocabulary.

Public MCP hostnames use the username-derived slug stored by the control plane, not the mutable
display name shown in Swing. In production this yields hostnames such as
`albert-outlook.dartmaker.com`, `albert-campus.dartmaker.com`, `albert-qbid.dartmaker.com` and
`albert-trello.dartmaker.com`.

The interface convention is consistent across modules:

- REST and OpenAPI controllers live under `interfaces.api`
- MCP tools live under `interfaces.mcp`
- OAuth protected-resource metadata lives under `interfaces.oauth`

Internal structure still differs by module because the implementation model is different:

- Outlook is COM-oriented, but now also follows the broad `application / domain / infrastructure /
  interfaces` split
- Campus keeps the most explicit layered architecture
- qBid now follows the same broad `application / domain / infrastructure / interfaces` split, with
  a stronger parser-heavy infrastructure because it is scraping-heavy

## Package map

- `tools.ceac.ai.desktop`
  - desktop shell and product entry point
- `tools.ceac.ai.desktop.launcher`
  - control-plane integration, bootstrap handling and `cloudflared`
- `tools.ceac.ai.desktop.ui`
  - Swing user interface
- `tools.ceac.ai.modules.outlook`
  - Outlook resource module
  - `application`, `domain`, `infrastructure`, `interfaces`
- `tools.ceac.ai.modules.campus`
  - Campus resource module
  - `application`, `domain`, `infrastructure`, `interfaces`
- `tools.ceac.ai.modules.qbid`
  - qBid resource module
  - `application`, `domain`, `infrastructure`, `interfaces`
- `tools.ceac.ai.modules.trello`
  - Trello resource module
  - `application`, `domain`, `infrastructure`, `interfaces`

## What the launcher does

- logs in against the control plane
- receives desktop token plus bootstrap per resource
- mints one local API token per resource for Swagger and localhost REST checks
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

The result of login is a `ControlPlaneSession` that carries bootstrap for every available resource module.

That session is the authoritative resource snapshot for the current launcher run. A newly enabled
resource does not appear until the next successful login.

### Outlook bot

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

#### Outlook message listing contract

The Outlook local REST API and MCP wrapper now optimize message listing and detail retrieval for
desktop Outlook stores.

`GET /api/outlook/messages` and the `listMessages` MCP tool:

- return newest messages first by default with `sortOrder=desc`
- default `limit` to `20`
- default `unreadOnly` to `false`
- default `since` to `now - 7 days` when omitted by the caller
- include `body` directly in the listing response
- include `attachments` directly in the listing response
- fill `to`, `cc` and `bcc` in the listing response

Implementation notes:

- folder scanning is driven by the Outlook `Table` API for ordering and lightweight row access
- the runtime then resolves only the selected rows to `MailItem` objects to enrich `body`,
  recipients and attachments
- `getMessage` reuses a cached `storeId` when available so `GetItemFromID` does not have to probe
  every open Outlook store
- `getMessage` reads body content through `PropertyAccessor` first and falls back to `Body` /
  `HTMLBody` only when Outlook does not expose the MAPI properties directly

This keeps the response shape stable while avoiding the slowest COM paths for common listing and
detail flows.

### Campus bot

Shows bootstrap and runtime state for Campus.

Campus differs from qBid:

- it does not ask for username and password in Swing fields
- login happens only in the browser modal when the session is missing or relaunched
- Moodle cookies are copied to the Java HTTP client and then reused by REST and MCP

Current limitation:

- creating a PDF resource through Campus is not reliable yet
- the resource-upload path is still experimental and may fail during draft upload or final
  Moodle form submission

Campus authoring support currently includes:

- `createAssignment`
  - creates a Moodle `assign` activity in a target course section without uploading attachments
  - available through REST and MCP
  - accepts assignment description, activity instructions, visibility flags and optional dates
- `createPdfResource`
  - uploads a PDF draft file and submits the final Moodle resource form
  - still experimental

Implementation notes:

- Campus creation flows now share the same generic `course/mod.php -> course/modedit.php`
  gateway instead of duplicating a resource-only path
- assignment creation uses the real Moodle form fields from `modedit.php`, not an undocumented
  REST shortcut
- the form extractor explicitly ignores empty multiselect widgets such as `tags[]` while keeping
  required hidden companions such as `_qf__force_multiselect_submission`

Validation notes for assignment creation:

- `section` and `name` are required
- date fields accept ISO-8601 local values such as `2026-04-30T23:59`, plain dates such as
  `2026-04-30`, or zoned timestamps
- if Moodle keeps the user on `course/modedit.php`, the runtime treats that as a creation
  validation failure

### QBid bot

Shows bootstrap and runtime state for qBid.

It also asks for local qBid credentials:

- `Usuario qBid`
- `Password qBid`

Those qBid credentials stay local. They are not sent to the control plane or stored in Keycloak.

### Trello bot

Shows bootstrap and runtime state for Trello.

It adds one local browser authorization step before startup:

- `Conectar Trello` opens the system browser
- Trello redirects to `http://127.0.0.1:43127/trello/callback`
- the launcher captures and validates the token locally
- the token stays on the operator machine and never reaches the control plane
- the token is persisted locally for the current Windows user, so the operator does not need to
  re-authorize on every desktop login
- `Desconectar` removes the local token and also attempts to revoke it in Trello

Like the other tabs, `Trello bot` depends on the resource list returned by login. If `Trello MCP`
is missing from `Login -> Recursos`, the tab remains in `pending` until the user logs in again
against a control plane that already returns that resource.

### Trello wrapper contract

The Trello runtime exposes a local REST API and MCP tools that wrap part of the Trello REST API.

Important wrapper semantics:

- fields that are omitted from a JSON request are deserialized as `null`
- for partial update requests, `null` means "leave unchanged"
- an empty string is not a no-op
- for `name`, `description` and `due`, an empty string is forwarded to Trello and may clear data
  or trigger Trello-side validation
- blank values for some routing fields such as `position`, `listId` or `checklistId` may be
  ignored by the wrapper instead of forwarded

This means that these values are intentionally different:

- omitted field: no change
- `null`: no change
- `""`: explicit value, not "leave unchanged"

#### Trello card state semantics

For cards, these actions are different and must not be confused:

- `closed=true`: archives the card
- `dueComplete=true`: marks the due date as completed without archiving the card
- moving a card to a list such as `Done` or `Finalizada`: only changes the list, it does not imply
  `closed=true` and does not imply `dueComplete=true`

If the intent is to move a card between lists, prefer the explicit move operation over a generic
card update carrying `listId`.

#### Trello field formats

Expected field formats in the wrapper:

- `due`: Trello-compatible datetime, usually ISO-8601 such as
  `2026-05-02T10:30:00.000Z`
- `position`: values such as `top`, `bottom` or a numeric string accepted by Trello

#### Destructive operations

The following operations are destructive and should be treated accordingly in tests and operator
flows:

- delete card
- delete checklist
- delete checklist item
- archive card via `closed=true`

## Public MCP vs local API

Each resource exposes two different surfaces:

- `MCP`: public hostname behind Cloudflare and central OAuth
- `API` and `Swagger`: local-only operational surface on `127.0.0.1`

The local surface exists for operator checks, parser verification and manual Swagger tests. It uses
launcher-issued local tokens and is intentionally different from the public OAuth contract consumed
by ChatGPT, Claude and similar MCP clients.

## Requirements

- Windows
- Java 21
- Maven 3.9+
- `cloudflared` installed or available on `PATH`
- Outlook Desktop installed for Outlook MCP
- access to qBid or sBid for QBid MCP
- access to `campus.ceacfp.es` for Campus MCP
- a Trello app key whose callback configuration allows `http://127.0.0.1:43127`

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
- Trello local runtime: `8083`

`cloudflared` metrics ports are separate and managed per resource by the launcher.

## Normal usage flow

1. run `run.bat`
2. authenticate in `Login`
3. open `Outlook bot`, `QBid bot`, `Campus bot` or `Trello bot`
4. for Trello, connect the account first
5. start the resource
6. use the public URL `https://<slug>-<resource>.dartmaker.com/mcp` from ChatGPT, Claude or another MCP client

## Architecture summary

- `desktop` owns product UI, control-plane login, bootstrap and tunnel lifecycle
- `modules.*` own resource-specific runtime behavior
- `interfaces.mcp` is only one interface of a module, not the module itself

## OAuth summary

The launcher only supports `CENTRAL_AUTH`.

That means:

- public central issuer
- public central JWKS
- audience and scope defined per resource
- no localhost issuer for external clients

Typical values:

- Outlook:
  - audience `ceac-ia-tools`
  - scope `outlook:tools`
- Campus:
  - audience `campus-mcp`
  - scope `campus:tools`
- qBid:
  - audience `qbid-mcp`
  - scope `qbid:tools`
- Trello:
  - audience `trello-mcp`
  - scope `trello:tools`

## Useful local URLs

- Outlook Swagger: `http://localhost:8080/swagger-ui/index.html`
- Outlook MCP: `http://localhost:8080/mcp`
- Campus Swagger: `http://localhost:8081/swagger-ui/index.html`
- Campus MCP: `http://localhost:8081/mcp`
- qBid Swagger: `http://localhost:8082/swagger-ui/index.html`
- qBid MCP: `http://localhost:8082/mcp`
- Trello Swagger: `http://localhost:8083/swagger-ui/index.html`
- Trello MCP: `http://localhost:8083/mcp`

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
- [`docs/CAMPUS_ACTIVITY_AUTHORING.md`](docs/CAMPUS_ACTIVITY_AUTHORING.md)

