# Launcher architecture

## Objetivo

El launcher existe para reducir el runtime del cliente desktop a dos pasos visibles:

1. `Login`
2. `Outlook MCP`

Todo lo demas debe quedar encapsulado.

## Regla de diseño

La aplicacion desktop no es el plano de control. Solo consume el plano de control.

Eso implica:

- no crea tunnels
- no gestiona DNS
- no arranca ni administra Keycloak
- no decide issuer, audience ni scope

Todo eso viene del backend mediante `bootstrap`.

## Componentes principales

### `LauncherWindow`

Clase Swing que:

- construye la UI
- mantiene el estado de sesion local
- coordina las acciones de los botones
- refleja en pantalla el bootstrap recibido y el estado del runtime

### `RemoteLauncherService`

Servicio de infraestructura del launcher. Hace el trabajo no visual:

- login HTTP contra el control plane
- fetch de bootstrap
- validacion minima del bootstrap
- arranque y parada de `cloudflared`
- escritura de `.env.generated`
- validacion de prerequisitos del runtime

### `ControlPlaneSession`

Record que representa la sesion del desktop:

- URL del control plane
- token del desktop
- identidad del usuario
- bootstrap recibido

### `RuntimeSettings`

Adaptador entre el `BootstrapResponse` y las properties Spring con las que se levanta la app local.

## Flujo Login -> MCP

### 1. Login

La pestana `Login` recoge:

- `username`
- `password`
- `machineId`
- `clientVersion`

En el flujo normal solo `username` y `password` son editables. `controlPlaneUrl`, `machineId` y `clientVersion` se muestran como contexto calculado o fijo del launcher.

Con eso llama a `POST /api/client/login`.

Si todo va bien:

- guarda `ControlPlaneSession`
- pinta el `BootstrapResponse`
- habilita la pestana `Outlook MCP`

En esta pestana solo viven las acciones de sesion:

- iniciar sesion en panel de control
- cerrar sesion

No contiene acciones operativas del runtime MCP.

### Modo normal frente a modo desarrollo

En modo normal:

- el control plane se considera fijo
- la URL no se muestra como campo editable
- `machineId` y `clientVersion` se muestran como campos solo lectura

En modo desarrollo:

- la URL del control plane vuelve a ser editable
- se usa para pruebas locales o entornos alternativos
- el titulo de la ventana incluye `[DEV]`

### 2. Arranque del Outlook MCP

La pestana `Outlook MCP` solo puede arrancar si hay sesion valida.

Pasos:

1. comprobar que existe `bootstrap`
2. comprobar que `authExposureMode = CENTRAL_AUTH`
3. arrancar o reutilizar `cloudflared`
4. validar prerequisitos
5. traducir bootstrap a `RuntimeSettings`
6. escribir `.env.generated`
7. arrancar Spring Boot con esas properties

Las acciones visibles de esta pestana son:

- validar estado
- arrancar MCP
- parar MCP
- copiar MCP URL
- abrir Swagger
- copiar Swagger

## Por que `CENTRAL_AUTH` es obligatorio aqui

El launcher del desktop actual se ha simplificado para el escenario real de clientes MCP externos.

Si el backend devolviera `LOCAL_AUTH`, el issuer OAuth dependeria de un servicio de autenticacion por usuario. Eso no forma parte del runtime soportado por el cliente actual y haria la experiencia mucho mas fragil.

## `.env.generated`

El launcher no modifica `application.yml`. En su lugar genera `.env.generated` con:

- `MCP_PUBLIC_BASE_URL`
- `MCP_AUTH_ENABLED`
- `MCP_OAUTH_ISSUER_URI`
- `MCP_OAUTH_JWK_SET_URI`
- `MCP_OAUTH_REQUIRED_AUDIENCE`
- `MCP_OAUTH_REQUIRED_SCOPE`
- `MCP_RESOURCE_NAME`

Estas variables son la forma en que el bootstrap del backend se convierte en configuracion efectiva del runtime local.

## Interaccion con la app Spring local

La app local solo se arranca desde la pestana `Outlook MCP`.

En entorno grafico:

- `main()` abre el launcher
- no se arranca la API ni el MCP automaticamente

En entorno headless:

- `main()` si arranca Spring Boot directamente

## Logs

### Launcher

- `logs/launcher-*.log`

### cloudflared

- `logs/cloudflared-*.stdout.log`
- `logs/cloudflared-*.stderr.log`

### Variables generadas

- `.env.generated`

## Extension points

Si en el futuro cambia el contrato con el backend, los puntos mas sensibles son:

- [RemoteLauncherService.java](C:/Users/alber/Documents/IdeaProjects/OutlookDesktop_COM_MCP/src/main/java/com/alber/outlookdesktop/launcher/RemoteLauncherService.java)
- [LauncherWindow.java](C:/Users/alber/Documents/IdeaProjects/OutlookDesktop_COM_MCP/src/main/java/com/alber/outlookdesktop/ui/LauncherWindow.java)
- [McpRemoteProperties.java](C:/Users/alber/Documents/IdeaProjects/OutlookDesktop_COM_MCP/src/main/java/com/alber/outlookdesktop/config/McpRemoteProperties.java)
- [SecurityConfig.java](C:/Users/alber/Documents/IdeaProjects/OutlookDesktop_COM_MCP/src/main/java/com/alber/outlookdesktop/config/SecurityConfig.java)
