# Launcher Architecture

## Goal

The desktop launcher is the local orchestration layer of CEAC AI Tools.

Its job is to:

- authenticate the user against the control plane
- load bootstrap per resource
- start local tunnels
- start local MCP runtimes
- host resource-specific embedded UI when a runtime needs it, such as Campus

It is not the control plane.

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

### `tools.ceac.ai.mcp.outlook`

Outlook runtime only.

### `tools.ceac.ai.mcp.campus`

Campus runtime only, including embedded JCEF login and Moodle session reuse.

### `tools.ceac.ai.mcp.qbid`

qBid runtime only.

The important rule is:

- the shell owns the UI and orchestration
- each MCP package owns only its runtime behavior

## Main launcher components

### `CeacLauncherWindow`

Builds and governs the Swing UI.

Responsibilities:

- keep session state in memory
- project bootstrap values into the UI
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
- bootstrap per resource
- list of available resources

### `QbidRuntimeService`

Runs qBid as an embedded Spring context.

Responsibilities:

- validate qBid credentials locally
- inject OAuth and public URL settings from bootstrap
- start and stop the qBid runtime

### `CampusRuntimeService`

Runs Campus as an embedded Spring context.

Responsibilities:

- inject OAuth and public URL settings from bootstrap
- expose the embeddable JCEF panel used by the `Campus MCP` tab
- start and stop the Campus runtime

## UI model

### Login tab

Global desktop session tab.

Its output is:

- authenticated desktop session
- bootstrap for `outlook`
- bootstrap for `campus`
- bootstrap for `qbid`

### Outlook MCP tab

Operates the Outlook runtime.

Depends on:

- active session
- Outlook bootstrap
- local `cloudflared`
- Outlook installed locally

### Campus MCP tab

Operates the Campus runtime.

Depends on:

- active session
- Campus bootstrap
- local `cloudflared`
- local JCEF runtime
- Moodle session acquired through the embedded browser

### QBid MCP tab

Operates the qBid runtime.

Depends on:

- active session
- qBid bootstrap
- local `cloudflared`
- local qBid credentials

## Flow: Login -> Outlook MCP

1. the user logs in
2. the launcher stores `ControlPlaneSession`
3. the `Outlook MCP` tab displays bootstrap data
4. on start:
   - prerequisites are validated
   - `cloudflared` is started
   - the Outlook Spring runtime is started

## Flow: Login -> Campus MCP

1. the user logs in against the control plane
2. the launcher stores `ControlPlaneSession`
3. the `Campus MCP` tab displays bootstrap data
4. on start:
   - prerequisites are validated
   - `cloudflared` is started
   - the Campus Spring runtime is started
   - the JCEF panel is mounted in the tab
5. the user logs in through the embedded browser
6. the runtime copies Moodle cookies into the Java HTTP client and reuses that session for REST and MCP

## Flow: Login -> QBid MCP

1. the user logs in
2. the launcher stores `ControlPlaneSession`
3. the `QBid MCP` tab displays bootstrap data
4. the user provides qBid credentials
5. the launcher validates those credentials locally
6. on start:
   - prerequisites are validated
   - `cloudflared` is started
   - the qBid Spring runtime is started

## Why the runtimes are separate

Each MCP resource has:

- a different local port
- a different public hostname
- a different tunnel token
- a different OAuth audience and scope
- different local prerequisites
- different local UI requirements

The launcher therefore models "multiple MCP resources under one desktop session", not "one MCP with multiple modes".

## Security rules

- the launcher only supports `CENTRAL_AUTH`
- Cloudflare administrative credentials never reach the desktop
- qBid credentials never leave the local machine
- the OAuth issuer announced to external MCP clients must be public, never localhost

## Extension points

Primary files:

- [`src/main/java/tools/ceac/ai/desktop/ui/CeacLauncherWindow.java`](../src/main/java/tools/ceac/ai/desktop/ui/CeacLauncherWindow.java)
- [`src/main/java/tools/ceac/ai/desktop/launcher/RemoteLauncherService.java`](../src/main/java/tools/ceac/ai/desktop/launcher/RemoteLauncherService.java)
- [`src/main/java/tools/ceac/ai/desktop/launcher/ControlPlaneSession.java`](../src/main/java/tools/ceac/ai/desktop/launcher/ControlPlaneSession.java)
- [`src/main/java/tools/ceac/ai/desktop/launcher/QbidRuntimeService.java`](../src/main/java/tools/ceac/ai/desktop/launcher/QbidRuntimeService.java)
- [`src/main/java/tools/ceac/ai/desktop/launcher/CampusRuntimeService.java`](../src/main/java/tools/ceac/ai/desktop/launcher/CampusRuntimeService.java)
- [`src/main/java/tools/ceac/ai/desktop/launcher/ManagedMcpKind.java`](../src/main/java/tools/ceac/ai/desktop/launcher/ManagedMcpKind.java)
