# Launcher Architecture

## Goal

The desktop launcher is the local orchestration layer of CEAC AI Tools.

Its job is to:

- authenticate the user against the control plane
- load bootstrap per resource
- mint local API tokens per resource for Swagger and local REST checks
- start local tunnels
- start local MCP runtimes
- host resource-specific embedded UI when a runtime needs it, such as the Campus login modal

It is not the control plane.

The launcher should also be distinguished from the resource modules:

- `tools.ceac.ai.desktop` is the product shell
- `tools.ceac.ai.modules.*` are the functional modules
- `interfaces.mcp` is only one interface surface inside each module

## Package boundaries

### `tools.ceac.ai.desktop`

Desktop shell entry point.

### `tools.ceac.ai.desktop.ui`

Swing user interface for:

- login
- status display
- per-resource actions
- operator logs

### `tools.ceac.ai.desktop.launcher`

Infrastructure layer for:

- login against the control plane
- bootstrap retrieval
- `cloudflared` lifecycle
- local runtime startup
- local session state

### `tools.ceac.ai.modules.outlook`

Outlook module only.

Outlook now uses the same broad package vocabulary as the other runtimes:

- `application`
- `domain`
- `infrastructure`
- `interfaces`

### `tools.ceac.ai.modules.campus`

Campus module only, including embedded JCEF login and Moodle session reuse.

Campus is the most explicitly layered runtime:

- `application`
- `domain`
- `infrastructure`
- `interfaces`

### `tools.ceac.ai.modules.qbid`

qBid module only.

qBid now follows the same broad package vocabulary as Campus:

- `application`
- `domain`
- `infrastructure`
- `interfaces`

### `tools.ceac.ai.modules.trello`

Trello module only.

Trello follows the same broad package vocabulary as the other runtimes:

- `application`
- `domain`
- `infrastructure`
- `interfaces`

The important rule is:

- the shell owns the UI and orchestration
- each resource module owns only its runtime behavior

## Main launcher components

### `CeacLauncherWindow`

Builds and governs the Swing UI.

Responsibilities:

- keep session state in memory
- project bootstrap values into the UI
- project launcher-issued local API tokens into each module tab
- coordinate actions on each tab
- show operational logs and failures

### `RemoteLauncherService`

Adapter between the launcher, the control plane and `cloudflared`.

Responsibilities:

- `POST /api/client/login`
- `POST /api/client/bootstrap`
- minimal bootstrap validation
- start and stop one `cloudflared` process per resource
- generate `.env.generated`

### `ControlPlaneSession`

Represents the authenticated desktop session.

Contains:

- control-plane base URL
- desktop token
- resolved identity
- bootstrap per resource module
- list of available resource modules
- launcher signing context for local API tokens

### `QbidRuntimeService`

Runs qBid as an embedded Spring context.

Responsibilities:

- validate qBid credentials against qBid before startup
- inject OAuth and public URL settings from bootstrap
- inject launcher token validation settings for local-only API access
- start and stop the qBid runtime

### `CampusRuntimeService`

Runs Campus as an embedded Spring context.

Responsibilities:

- inject OAuth and public URL settings from bootstrap
- expose the embeddable JCEF panel used by the Campus login modal
- inject launcher token validation settings for local-only API access
- start and stop the Campus runtime

### `OutlookRuntimeService`

Runs Outlook as an embedded Spring context.

Responsibilities:

- start and stop the Outlook runtime
- wait for local OAuth metadata to become reachable
- inject launcher token validation settings for local-only API access
- keep Outlook startup symmetric with qBid and Campus

## UI model

### Login tab

Global desktop session tab.

Its output is:

- authenticated desktop session
- bootstrap for `outlook`
- bootstrap for `campus`
- bootstrap for `qbid`
- bootstrap for `trello`

### Outlook bot tab

Operates the Outlook runtime.

Depends on:

- active session
- Outlook bootstrap
- local `cloudflared`
- Outlook installed locally

### Campus bot tab

Operates the Campus runtime.

Depends on:

- active session
- Campus bootstrap
- local `cloudflared`
- local JCEF runtime
- Moodle session acquired through the embedded browser modal

### QBid bot tab

Operates the qBid runtime.

Depends on:

- active session
- qBid bootstrap
- local `cloudflared`
- local qBid credentials

### Trello bot tab

Operates the Trello runtime.

Depends on:

- active session
- Trello bootstrap
- local `cloudflared`
- browser-based Trello authorization captured by the launcher

## Flow: Login -> Outlook bot

1. the user logs in
2. the launcher stores `ControlPlaneSession`
3. the `Outlook bot` tab displays bootstrap data
4. on start:
   - prerequisites are validated
   - `cloudflared` is started
   - the Outlook Spring runtime is started
5. the launcher shows a local API token that is valid only for `localhost`

## Flow: Login -> Campus bot

1. the user logs in against the control plane
2. the launcher stores `ControlPlaneSession`
3. the `Campus bot` tab displays bootstrap data
4. on start:
   - prerequisites are validated
   - `cloudflared` is started
   - the Campus Spring runtime is started
   - the login modal is opened when Campus authentication is needed
5. the user logs in through the embedded browser modal
6. the runtime copies Moodle cookies into the Java HTTP client and reuses that session for REST and MCP
7. the launcher shows a local API token that is valid only for `localhost`

## Flow: Login -> QBid bot

1. the user logs in
2. the launcher stores `ControlPlaneSession`
3. the `QBid bot` tab displays bootstrap data
4. the user provides qBid credentials
5. the launcher validates those credentials against qBid
6. on start:
   - prerequisites are validated
   - `cloudflared` is started
   - the qBid Spring runtime is started
7. the launcher shows a local API token that is valid only for `localhost`

## Why the runtimes are separate

Each resource module has:

- a different local port
- a different public hostname
- a different tunnel token
- a different OAuth audience and scope
- different local prerequisites
- different local UI requirements

The launcher therefore models "multiple resource modules under one desktop session", not "one MCP with multiple modes".

The shell now treats all four runtimes through the same high-level pattern:

1. validate prerequisites
2. start `cloudflared`
3. start the embedded runtime service
4. expose the public MCP URL
5. expose local-only API and Swagger endpoints on `127.0.0.1`
6. mint a launcher-issued local API token for Swagger and manual REST checks

Trello follows the same runtime pattern, with one extra local step before startup:

- the launcher captures a Trello operator token through a localhost callback page

## Security rules

- the launcher only supports `CENTRAL_AUTH`
- Cloudflare administrative credentials never reach the desktop
- qBid credentials never leave the local machine
- the Trello operator token never reaches the control plane
- the OAuth issuer announced to external MCP clients must be public, never localhost
- local REST API, OpenAPI and Swagger are reachable only on `127.0.0.1`
- local API tokens are minted by the launcher, not by the control plane
- remote clients must use the public MCP endpoint and its central OAuth contract, never the local API token

## Extension points

Primary files:

- [`src/main/java/tools/ceac/ai/desktop/ui/CeacLauncherWindow.java`](../src/main/java/tools/ceac/ai/desktop/ui/CeacLauncherWindow.java)
- [`src/main/java/tools/ceac/ai/desktop/launcher/RemoteLauncherService.java`](../src/main/java/tools/ceac/ai/desktop/launcher/RemoteLauncherService.java)
- [`src/main/java/tools/ceac/ai/desktop/launcher/ControlPlaneSession.java`](../src/main/java/tools/ceac/ai/desktop/launcher/ControlPlaneSession.java)
- [`src/main/java/tools/ceac/ai/desktop/launcher/QbidRuntimeService.java`](../src/main/java/tools/ceac/ai/desktop/launcher/QbidRuntimeService.java)
- [`src/main/java/tools/ceac/ai/desktop/launcher/CampusRuntimeService.java`](../src/main/java/tools/ceac/ai/desktop/launcher/CampusRuntimeService.java)
- [`src/main/java/tools/ceac/ai/desktop/launcher/TrelloRuntimeService.java`](../src/main/java/tools/ceac/ai/desktop/launcher/TrelloRuntimeService.java)
- [`src/main/java/tools/ceac/ai/desktop/launcher/ManagedMcpKind.java`](../src/main/java/tools/ceac/ai/desktop/launcher/ManagedMcpKind.java)
- [`src/main/java/tools/ceac/ai/modules/outlook/package-info.java`](../src/main/java/tools/ceac/ai/modules/outlook/package-info.java)
- [`src/main/java/tools/ceac/ai/modules/campus/package-info.java`](../src/main/java/tools/ceac/ai/modules/campus/package-info.java)
- [`src/main/java/tools/ceac/ai/modules/qbid/package-info.java`](../src/main/java/tools/ceac/ai/modules/qbid/package-info.java)

