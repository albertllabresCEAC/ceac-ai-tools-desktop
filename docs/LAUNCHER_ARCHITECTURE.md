# Launcher Architecture

## Objetivo

El launcher desktop es la capa de orquestacion local de CEAC IA Tools. Su funcion es:

- autenticar al usuario contra el control plane
- cargar bootstrap por recurso
- arrancar tunnels locales
- arrancar runtimes MCP locales

No es el plano de control.

## Componentes principales

### `CeacLauncherWindow`

Construye y gobierna la UI Swing.

Responsabilidades:

- mantener el estado de sesion
- reflejar el bootstrap en pantalla
- coordinar acciones de cada pestana
- mostrar actividad y errores operativos

### `RemoteLauncherService`

Es el adaptador del launcher al control plane y a `cloudflared`.

Responsabilidades:

- `POST /api/client/login`
- `POST /api/client/bootstrap`
- validacion basica del bootstrap
- arranque y parada de `cloudflared` por recurso
- generacion de `.env.generated`

### `QbidRuntimeService`

Gestiona el runtime qBid como proceso local independiente.

Responsabilidades:

- validar credenciales qBid
- arrancar `qBidScrAPI`
- inyectarle las variables OAuth/publicas derivadas del bootstrap
- parar el proceso y reportar Swagger

### `ControlPlaneSession`

Representa la sesion autenticada del desktop.

Contiene:

- URL del control plane
- token desktop
- identidad resuelta
- bootstrap por recurso
- lista de recursos disponibles

## Modelo de UI

### Pestana `Login`

Es global. No depende de un recurso MCP concreto.

Su salida es:

- sesion del desktop
- bootstrap para `outlook`
- bootstrap para `qbid`

### Pestana `Outlook MCP`

Gestiona el runtime Outlook.

Depende de:

- sesion activa
- bootstrap `outlook`
- `cloudflared`
- Outlook local

### Pestana `QBid MCP`

Gestiona el runtime qBid.

Depende de:

- sesion activa
- bootstrap `qbid`
- `cloudflared`
- credenciales qBid locales
- repo `qBidScrAPI`

## Flujo Login -> Outlook MCP

1. el usuario se autentica
2. el launcher guarda `ControlPlaneSession`
3. la pestana `Outlook MCP` muestra bootstrap
4. al arrancar:
   - valida prerequisitos
   - levanta `cloudflared`
   - arranca Spring Boot local de Outlook

## Flujo Login -> QBid MCP

1. el usuario se autentica
2. el launcher guarda `ControlPlaneSession`
3. la pestana `QBid MCP` muestra bootstrap
4. el usuario introduce credenciales qBid
5. el launcher valida esas credenciales contra qBid
6. al arrancar:
   - valida prerequisitos
   - levanta `cloudflared`
   - arranca `qBidScrAPI` como proceso local

## Por que los dos MCP van separados

Porque son runtimes distintos:

- distinto puerto local
- distinto hostname publico
- distinto tunnel token
- distinto audience/scope OAuth
- distinta logica local

El launcher ya no asume "un solo MCP", sino "varios recursos MCP gestionados por la misma sesion".

## Reglas de seguridad

- el launcher solo soporta `CENTRAL_AUTH`
- las credenciales de Cloudflare nunca llegan al cliente
- las credenciales qBid no se envian al backend
- el issuer OAuth debe ser publico, no `localhost`

## Archivos y puntos de extension

- [CeacLauncherWindow.java](/C:/Users/alber/Documents/IdeaProjects/OutlookDesktop_COM_MCP/src/main/java/com/alber/outlookdesktop/ui/CeacLauncherWindow.java)
- [RemoteLauncherService.java](/C:/Users/alber/Documents/IdeaProjects/OutlookDesktop_COM_MCP/src/main/java/com/alber/outlookdesktop/launcher/RemoteLauncherService.java)
- [QbidRuntimeService.java](/C:/Users/alber/Documents/IdeaProjects/OutlookDesktop_COM_MCP/src/main/java/com/alber/outlookdesktop/launcher/QbidRuntimeService.java)
- [ManagedMcpKind.java](/C:/Users/alber/Documents/IdeaProjects/OutlookDesktop_COM_MCP/src/main/java/com/alber/outlookdesktop/launcher/ManagedMcpKind.java)
- [ControlPlaneSession.java](/C:/Users/alber/Documents/IdeaProjects/OutlookDesktop_COM_MCP/src/main/java/com/alber/outlookdesktop/launcher/ControlPlaneSession.java)
