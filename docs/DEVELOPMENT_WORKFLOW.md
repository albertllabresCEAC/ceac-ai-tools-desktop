# Development workflow

## Objetivo

Este documento deja clara una distincion importante del repo:

- que scripts forman parte del uso normal del cliente
- que scripts existen solo para comodidad de desarrollo local

## Regla simple

Si quieres usar el cliente desktop contra el entorno central, usa:

- [run.bat](C:/Users/alber/Documents/IdeaProjects/OutlookDesktop_COM_MCP/run.bat)

Si quieres levantar tambien el control plane local y sus dependencias para desarrollar de punta a punta, usa:

- [run-full-local.bat](C:/Users/alber/Documents/IdeaProjects/OutlookDesktop_COM_MCP/run-full-local.bat)

## Scripts del repo

### `run.bat`

Pertenece al producto.

Responsabilidad:

- arrancar el cliente desktop con defaults razonables del control plane central
- fijar `DARTMAKER_DEV_MODE=false`

No hace:

- levantar PostgreSQL
- levantar Keycloak del control plane
- arrancar el control plane local

### `run-full-local.bat`

Es tooling de desarrollo.

Responsabilidad:

- levantar dependencias del control plane local
- arrancar el control plane local
- exportar variables de entorno del launcher
- fijar `DARTMAKER_DEV_MODE=true`
- delegar despues en `run.bat`

Es util para:

- probar login
- probar bootstrap
- probar el launcher end-to-end
- depurar el contrato entre backend y cliente

No debe confundirse con el flujo normal del producto.

### `run-dev.bat`

Es un alias legado.

Se mantiene para no romper habitos ni referencias antiguas, pero el nombre recomendado es:

- `run-full-local.bat`

### `scripts/Start-Launcher.ps1`

Es el motor que usa `run.bat` para arrancar la app cliente.

### `scripts/dev/Run-Full-Local.ps1`

Es el motor real del flujo full-local de desarrollo.

### `scripts/Run-Dev.ps1`

Es un wrapper legado que delega en `scripts/dev/Run-Full-Local.ps1`.

## Por que esta separacion tiene sentido

Porque el cliente desktop y el entorno de desarrollo no son la misma cosa.

El cliente desktop real:

- solo necesita el control plane ya existente
- no deberia asumir que va a levantar infraestructura

El entorno de desarrollo:

- si necesita orquestar varios servicios
- mezcla este repo y el repo del control plane

Mantener nombres distintos ayuda a que esa frontera quede clara.

## Como se activa el modo desarrollo

El modo desarrollo no se autodetecta. Solo se activa si:

- ejecutas `run-full-local.bat`
- ejecutas `run-dev.bat`
- o defines manualmente `DARTMAKER_DEV_MODE=true`

En ese modo la UI desbloquea la URL del control plane y muestra un titulo con sufijo `[DEV]`.

Incluso en desarrollo, los unicos campos pensados para editar a mano en la pantalla de login son las credenciales. `machineId` y `clientVersion` se tratan como contexto del launcher y se muestran como solo lectura.
