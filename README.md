# Outlook Desktop COM MCP

Aplicacion Spring Boot para automatizar Outlook Desktop en Windows mediante COM y exponer un servidor MCP local protegido por OAuth.

El proyecto cumple dos funciones al mismo tiempo:

- API y herramientas MCP para Outlook local
- launcher de escritorio que autentica contra el control plane y levanta el runtime necesario

## Mapa de documentacion

- [docs/LAUNCHER_ARCHITECTURE.md](C:/Users/alber/Documents/IdeaProjects/OutlookDesktop_COM_MCP/docs/LAUNCHER_ARCHITECTURE.md)
  Explica el launcher, las dos pestanas y el flujo Login -> MCP.
- [docs/REMOTE_MCP_SETUP.md](C:/Users/alber/Documents/IdeaProjects/OutlookDesktop_COM_MCP/docs/REMOTE_MCP_SETUP.md)
  Explica el bootstrap remoto, Cloudflare Tunnel, OAuth y como se publica el MCP.
- [docs/DEVELOPMENT_WORKFLOW.md](C:/Users/alber/Documents/IdeaProjects/OutlookDesktop_COM_MCP/docs/DEVELOPMENT_WORKFLOW.md)
  Explica que scripts pertenecen al producto y cuales son tooling de desarrollo.

## Que hace este proyecto

### Como aplicacion Outlook local

Expone:

- API REST con Swagger/OpenAPI
- servidor MCP por Streamable HTTP
- herramientas para listar mensajes, leerlos, crear borradores, adjuntar archivos y enviar correo

### Como cliente del control plane

Hace:

- login contra `dartmaker-tunnel-control-plane`
- recepcion de `bootstrap`
- arranque de `cloudflared` con `tunnelToken`
- arranque de la app local con la configuracion OAuth ya resuelta

No hace:

- crear tunnels en Cloudflare
- tocar DNS
- administrar Keycloak
- almacenar credenciales globales de Cloudflare

## Flujo funcional actual

La experiencia normal de usuario es:

1. abrir `run.bat`
2. iniciar sesion en la pestana `Login`
3. dejar que el backend devuelva el contexto y el bootstrap
4. ir a la pestana `Outlook MCP`
5. pulsar `Arrancar MCP`
6. usar la URL publica del MCP en ChatGPT, Claude u otro cliente compatible

La aplicacion esta pensada para el modo `CENTRAL_AUTH`. Si el backend devuelve `LOCAL_AUTH`, el launcher bloquea el arranque del MCP porque ese modo ya no forma parte del runtime soportado por el cliente actual.

## Requisitos

- Windows con Outlook Desktop instalado y configurado
- Java 21
- Maven 3.9+
- DLL de JACOB compatible con la arquitectura de tu JVM
- `cloudflared` instalado o accesible por `PATH`

## DLL de JACOB

La integracion COM usa JACOB. Puedes proporcionar la DLL de cualquiera de estas formas:

- `lib/jacob-x64.dll` o `lib/jacob-x86.dll`
- `lib/jacob-1.18-x64.dll` o `lib/jacob-1.18-x86.dll`
- `-Djacob.dll.path=C:\ruta\jacob-x64.dll`
- `jacob.dll-path` en `application.yml`

La arquitectura de la JVM, la DLL y Outlook debe coincidir.

## Scripts de arranque

Hay dos categorias distintas y conviene no mezclarlas mentalmente:

- scripts de producto: arrancan el cliente desktop
- scripts de desarrollo: levantan tambien un entorno local alrededor del cliente

### Script de producto

### `run.bat`

Modo normal del cliente.

Hace:

- fija modo normal con `DARTMAKER_DEV_MODE=false`
- usa un control plane fijo
- abre el launcher Swing
- no intenta levantar el control plane local

Defaults actuales:

- `CONTROL_PLANE_URL=https://control.dartmaker.com`
- `CONTROL_PLANE_CLIENT_VERSION=1.0.0`
- `CONTROL_PLANE_MACHINE_ID=%COMPUTERNAME%`

En este modo:

- la UI no deja editar la URL del control plane
- `Machine ID` y `Client version` se muestran como solo lectura
- usuario y password quedan en blanco hasta que el usuario los rellena

### Script de desarrollo recomendado: `run-full-local.bat`

Modo de desarrollo local de punta a punta.

Hace:

- activa `DARTMAKER_DEV_MODE=true`
- levanta PostgreSQL y Keycloak del control plane por Docker
- arranca el control plane local
- prepara variables de entorno del launcher
- llama a `run.bat`

Importante:

- el repo publico no lleva credenciales reales
- si quieres que el flujo full-local funcione sin teclear credenciales cada vez, debes cargarlas desde tu almacen privado antes de ejecutar el script

Puertos por defecto:

- cliente app: `http://localhost:8080`
- control plane local: `http://localhost:8090`
- Keycloak del control plane local: `http://localhost:8181`

### Alias legado: `run-dev.bat`

Sigue existiendo por compatibilidad, pero ya no es el nombre recomendado.

Su unica funcion ahora es:

- mostrar un aviso deprecado
- delegar en `run-full-local.bat`

## Modo desarrollo

El cliente entra en modo desarrollo solo si se fuerza explicitamente:

- ejecutando `run-full-local.bat`
- ejecutando `run-dev.bat`
- o exportando `DARTMAKER_DEV_MODE=true`

Que cambia en ese modo:

- la ventana muestra `[DEV]` en el titulo
- la URL del control plane vuelve a ser editable
- se permite apuntar el launcher a `localhost` u otros entornos de prueba

## Launcher Swing

La UI tiene dos pestanas.

### Login

Responsabilidad:

- autenticar contra el control plane
- resolver la identidad del desktop
- obtener un `bootstrap` valido
- dejar listo el contexto que usara la pestana `Outlook MCP`

Campos:

- `Control plane URL`
- `Usuario o email`
- `Password`
- `Machine ID`
- `Client version`

Solo `Usuario o email` y `Password` son editables. `Control plane URL`, `Machine ID` y `Client version` se consideran contexto del launcher y no datos que deba manipular el usuario final en el flujo normal.

Acciones:

- `Iniciar sesion en panel de control`
- `Cerrar sesion`

Los botones se alternan segun el estado:

- antes del login, solo esta activo `Iniciar sesion en panel de control`
- despues del login, solo esta activo `Cerrar sesion`

### Outlook MCP

Responsabilidad:

- validar prerequisitos
- arrancar `cloudflared`
- arrancar la app local con las variables derivadas del bootstrap
- exponer las utilidades operativas del runtime una vez existe bootstrap

Campos mostrados:

- `Tunnel ID`
- `MCP host`
- `Issuer URI`
- `JWK Set URI`
- `Audience`
- `Scope`
- `Resource name`

Acciones:

- `Validar estado`
- `Arrancar MCP`
- `Parar MCP`
- `Copiar MCP URL`
- `Abrir Swagger`
- `Copiar Swagger`

## Variables de entorno principales

- `CONTROL_PLANE_URL`
- `CONTROL_PLANE_LOGIN_USERNAME`
- `CONTROL_PLANE_LOGIN_PASSWORD`
- `CONTROL_PLANE_MACHINE_ID`
- `CONTROL_PLANE_CLIENT_VERSION`
- `CLOUDFLARED_CMD`
- `MCP_PUBLIC_BASE_URL`
- `MCP_AUTH_ENABLED`
- `MCP_OAUTH_ISSUER_URI`
- `MCP_OAUTH_JWK_SET_URI`
- `MCP_OAUTH_REQUIRED_AUDIENCE`
- `MCP_OAUTH_REQUIRED_SCOPE`
- `MCP_RESOURCE_NAME`

Las `MCP_*` no suelen definirse a mano en el flujo normal. El launcher las genera en `.env.generated` a partir del bootstrap.

## Uso rapido

### Entorno central

```powershell
.\run.bat
```

El repo publico ya no incluye credenciales operativas. Debes proporcionar:

- `CONTROL_PLANE_LOGIN_USERNAME`
- `CONTROL_PLANE_LOGIN_PASSWORD`

### Entorno local completo

```powershell
.\run-full-local.bat
```

## URLs utiles

### Locales

- Swagger: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI: `http://localhost:8080/v3/api-docs`
- MCP local: `http://localhost:8080/mcp`
- metadata OAuth local: `http://localhost:8080/.well-known/oauth-protected-resource`

### Publicas

La URL publica se deriva del bootstrap, por ejemplo:

- `https://user-mcp.example.com/mcp`
- `https://user-mcp.example.com/.well-known/oauth-protected-resource`

## Seguridad del MCP

Cuando el launcher arranca el runtime desde un bootstrap valido, la app local publica un recurso protegido OAuth con:

- issuer central
- JWK set central
- audience requerida
- scope requerido

Ejemplo tipico de despliegue:

- issuer: `https://auth.example.com/realms/outlookdesktop-mcp`
- audience: `outlookdesktop-mcp`
- scope: `mcp:tools`

## Logs y archivos generados

- `run-dev.log`
- `logs/launcher-*.log`
- `logs/cloudflared-*.stdout.log`
- `logs/cloudflared-*.stderr.log`
- `.env.generated`

## Troubleshooting rapido

### El launcher no puede hacer login

Revisa:

- `CONTROL_PLANE_URL`
- usuario y password
- que el control plane configurado responda en `/actuator/health`

### El launcher no encuentra `cloudflared`

Revisa:

- `PATH`
- `CLOUDFLARED_CMD`

### ChatGPT o Claude no conectan al MCP

Revisa:

- que el launcher haya completado `Login` y luego `Arrancar MCP`
- que `/.well-known/oauth-protected-resource` publique issuer publico
- que la URL usada en el cliente MCP sea `https://<slug>-mcp.<base-domain>/mcp`

## Documentacion del codigo

Las clases operativas del launcher, seguridad remota y metadata OAuth estan documentadas con Javadoc para que otra sesion pueda reconstruir rapidamente:

- que hace el launcher
- como se usa el bootstrap
- donde se validan issuer, audience y scope
- como se publica la metadata OAuth del recurso protegido
