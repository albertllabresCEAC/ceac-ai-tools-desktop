# Outlook Desktop COM MCP

Aplicacion Spring Boot para automatizar Outlook Desktop en Windows mediante COM. Expone:

- API REST con Swagger/OpenAPI
- Servidor MCP con herramientas reutilizando la misma capa de negocio
- Operaciones de correo: listar, leer, crear borradores, adjuntar archivos, enviar y leer adjuntos
- Ventana local de estado con URL de Swagger y panel de logs en tiempo real

## Requisitos

- Windows con Outlook Desktop instalado y configurado
- Java 21
- Maven 3.9+
- DLL de JACOB compatible con la arquitectura de tu JVM

## DLL de JACOB

La libreria Java usa JACOB para COM. Debes dejar la DLL en una de estas opciones:

- `lib/jacob-x64.dll` o `lib/jacob-x86.dll` en la raiz del proyecto
- `lib/jacob-1.18-x64.dll` o `lib/jacob-1.18-x86.dll` en la raiz del proyecto
- o definir `-Djacob.dll.path=C:\ruta\jacob-x64.dll`
- o configurar `jacob.dll-path` en `application.yml`

La arquitectura de la JVM, la DLL y Outlook deben coincidir.

## Arranque

```bash
mvn spring-boot:run
```

Al arrancar en un entorno grafico se abre una ventana con:

- Estado de la aplicacion
- URL directa a Swagger
- Botones para abrir Swagger o copiar la URL
- Logs en tiempo real

## Swagger

- `http://localhost:8080/swagger-ui.html`
- `http://localhost:8080/v3/api-docs`

## Endpoints principales

- `GET /api/outlook/messages`
- `GET /api/outlook/messages/{entryId}`
- `GET /api/outlook/messages/{entryId}/attachments`
- `GET /api/outlook/messages/{entryId}/attachments/{attachmentIndex}`
- `POST /api/outlook/drafts`
- `PUT /api/outlook/drafts/{entryId}`
- `DELETE /api/outlook/drafts/{entryId}`
- `POST /api/outlook/drafts/{entryId}/attachments`
- `POST /api/outlook/drafts/{entryId}/send`
- `POST /api/outlook/send`

## Modos de composicion

Los endpoints `POST /api/outlook/drafts` y `POST /api/outlook/send` soportan:

- `mode: NEW`
- `mode: REPLY`
- `mode: REPLY_ALL`

Para `REPLY` y `REPLY_ALL` debes enviar `originalEntryId`.

Ejemplo de correo nuevo:

```json
{
  "mode": "NEW",
  "to": "destino@dominio.com",
  "subject": "Asunto",
  "body": "Texto"
}
```

Ejemplo de respuesta:

```json
{
  "mode": "REPLY",
  "originalEntryId": "00000000...",
  "body": "Gracias"
}
```

Descarte de borrador:

- `DELETE /api/outlook/drafts/{entryId}` mueve el borrador a Eliminados
- `DELETE /api/outlook/drafts/{entryId}?permanent=true` lo borra definitivamente

## Notas

- El acceso COM solo funciona localmente en Windows.
- `entryId` es el identificador interno de Outlook del correo o borrador.
- Los adjuntos de entrada y salida se intercambian en Base64 dentro del JSON.
- Internamente la integracion COM necesita materializar archivos temporales, pero la API y el MCP no exponen rutas ni directorios del sistema de ficheros.
