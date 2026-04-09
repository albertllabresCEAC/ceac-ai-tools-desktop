# Development Workflow

## Regla simple

- uso normal del producto: `run.bat`
- entorno local completo de desarrollo: `run-full-local.bat`

## `run.bat`

Es el punto de entrada del producto.

Hace:

- `DARTMAKER_DEV_MODE=false`
- control plane central por defecto
- arranque del launcher Swing

No hace:

- levantar el backend local
- levantar Docker del control plane
- preparar credenciales de desarrollo

## `run-full-local.bat`

Es tooling de desarrollo.

Hace:

- `DARTMAKER_DEV_MODE=true`
- `docker compose up -d` en el control plane
- arranque del backend local
- apertura del launcher

Tambien deja editable la URL del control plane para pruebas.

## `run-dev.bat`

Es un alias legado.

Se mantiene para no romper costumbre, pero el nombre correcto es `run-full-local.bat`.

## Variables utiles

- `DEV_CONTROL_PLANE_ROOT`
- `DEV_CONTROL_PLANE_PORT`
- `DEV_CONTROL_PLANE_KEYCLOAK_PORT`

## Cuándo usar cada modo

### Producto

Usa `run.bat` cuando quieras:

- entrar al panel de control real
- arrancar Outlook MCP
- arrancar QBid MCP
- probar el producto como usuario

### Desarrollo

Usa `run-full-local.bat` cuando quieras:

- depurar contrato backend/launcher
- levantar el stack local entero
- tocar el control plane y el launcher a la vez
- probar cambios antes de subirlos
