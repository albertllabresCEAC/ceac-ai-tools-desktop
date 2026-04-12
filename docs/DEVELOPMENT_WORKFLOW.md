# Development Workflow

## Simple rule

- normal product usage: `run.bat`
- full local development stack: `run-full-local.bat`

## `run.bat`

This is the product entry point.

It:

- sets `DARTMAKER_DEV_MODE=false`
- uses the central control plane by default
- starts the Swing launcher

It does not:

- start the local backend
- start Docker for the control plane
- prepare local development credentials

Use it when you want to test the real product flow against the current central environment.

## `run-full-local.bat`

This is development tooling.

It:

- sets `DARTMAKER_DEV_MODE=true`
- runs `docker compose up -d` in the control plane
- starts the local control plane
- opens the launcher

It also makes the control-plane URL editable in the UI, which is useful when switching between local and non-production backends.

## `run-dev.bat`

Legacy alias kept for compatibility with older habits.

The canonical name is `run-full-local.bat`.

## Useful development variables

- `DEV_CONTROL_PLANE_ROOT`
- `DEV_CONTROL_PLANE_PORT`
- `DEV_CONTROL_PLANE_KEYCLOAK_PORT`
- `CONTROL_PLANE_URL`
- `CLOUDFLARED_CMD`

## When to use each mode

### Product mode

Use `run.bat` when you want to:

- sign in to the real control plane
- start the Outlook module
- start the Campus module
- start the QBid module
- start the Trello module
- validate the desktop product as an operator
- validate local Swagger and local API checks with launcher-issued tokens

### Development mode

Use `run-full-local.bat` when you want to:

- debug launcher and backend together
- change the bootstrap contract
- test local Keycloak or local provisioning behavior
- validate a change before pushing it

## Local identity notes

The local control-plane realm import includes seed users for development.

Those users may be created with required actions such as `UPDATE_PASSWORD`.

If that happens:

- password-grant desktop login will fail
- you must first complete the password change through local Keycloak

This behavior is expected and comes from Keycloak.

## Cleaning the local environment

If you run into stale local state, check:

- ports `8080`, `8081`, `8082`, `8083`
- `logs/`
- `.env.generated`
- local Docker volumes for Keycloak and PostgreSQL
- `jcef-cache/` if Campus behaves as if an old Moodle session were still alive

Typical cleanup actions:

- stop previous launcher instances
- stop stale `cloudflared` processes
- recreate local Docker volumes when the Keycloak import changed

## Recommended verification loop

1. start the desired mode
2. log in
3. start the target resource
4. verify local endpoints:
   - Outlook module on `8080`
   - Campus module on `8081`
   - qBid module on `8082`
   - Trello module on `8083`
5. verify that local Swagger and `/api` are reachable only on `localhost`
6. verify public MCP metadata through the Cloudflare hostname

## Local API notes

After desktop login, the launcher mints one local API token per resource and shows it in the
resource tabs.

Those tokens are intended only for:

- local Swagger
- local REST checks
- parser and contract verification on the operator machine

They are not control-plane tokens and they are not valid for the public MCP endpoints.
