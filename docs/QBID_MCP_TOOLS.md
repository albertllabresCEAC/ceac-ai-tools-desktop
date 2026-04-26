# QBid MCP Tools

Resumen rapido de las tools expuestas por el MCP de qBid (`QbidMcpTools`).

| Tool | Que hace |
| --- | --- |
| `listarConvenios` | Lista los convenios FCT del tutor. Devuelve ids clave como `codConvenio`, `codTemporal`, `codCuaderno` y `newSystem`. |
| `verDetalleConvenio` | Muestra el detalle completo de un convenio: alumno, empresa, fechas, horas y estado. |
| `verCuaderno` | Consulta el estado del cuaderno de practicas. Incluye estado de REF05, REF18, REF19 y REF20. |
| `verSeguimientoFCT` | Recupera el seguimiento formativo FCT del alumno. Incluye resultados de aprendizaje y criterios de evaluacion. |
| `verAgenda` | Obtiene la agenda mensual del alumno con dias fichados, horas y estado por dia. Incluye los informes periodicos del convenio. |
| `listarInformes` | Lista los informes periodicos del convenio. Devuelve los identificadores necesarios para consultar o editar cada informe. |
| `verDetalleInforme` | Muestra el detalle de un informe mensual: actividades, horas, valoraciones, observaciones y estado de firma. |
| `guardarInforme` | Guarda un informe periodico como borrador editable. Actualiza valoraciones y observaciones sin bloquear el informe. |
| `firmarInforme` | Firma definitivamente un informe periodico. Es una accion irreversible y deja el informe bloqueado. |
| `descargarRef19` | Descarga el documento REF19 en base64. Devuelve el PDF listo para guardarse localmente. |
| `descargarRef20` | Descarga el documento REF20 en base64. Devuelve nombre de fichero, tipo y contenido del PDF. |
| `descargarRef22` | Descarga el documento REF22 en base64. Sirve para obtener el expediente del cuaderno en PDF. |
| `verPlanActividades` | Consulta el plan de actividades del convenio. Indica items, jerarquia, seleccion y si cada uno es editable. |
| `guardarPlanActividades` | Guarda el plan de actividades como borrador. Permite actualizar actividades, responsable y recursos del centro. |
| `validarPlanActividades` | Valida y firma definitivamente el plan de actividades. Es irreversible y deja el plan sin edicion. |
| `verActividadDiaria` | Muestra lo registrado por el alumno en una fecha concreta. Incluye actividades, horas y observaciones. |
| `verValoracionFinal` | Recupera la valoracion final emitida por la empresa. Incluye nota global, criterios, observaciones y firmantes. |
| `verFichaAlumno` | Obtiene la ficha completa del alumno. Incluye datos personales, acceso a qBid y cuadernos asociados. |
| `verEmpresa` | Consulta la ficha de una empresa. Devuelve datos fiscales, sector, direccion y centros de trabajo. |
| `verCentroTrabajo` | Muestra el detalle de un centro de trabajo concreto. Incluye contacto, direccion, estado y actividad. |
| `descargarRef07` | Descarga el REF07 en base64. Admite idioma `SP`, `CA`, `EN` o `FR`. |
| `descargarRef10` | Descarga el REF10 en base64. Genera el PDF del cuestionario al centro de trabajo. |
| `descargarRef11` | Descarga el REF11 en base64. Genera el PDF de homologacion del convenio. |
| `descargarRef15` | Descarga el REF15 en base64. Devuelve el PDF de valoracion de evaluacion de la empresa. |
| `descargarRef18` | Descarga el REF18 en base64. Devuelve el PDF de valoracion del expediente. |
| `descargarRef05` | Descarga el REF05 en base64. Corresponde al documento del acuerdo o convenio firmado. |
| `descargarRef05Baja` | Descarga el REF05-Baja en base64. Corresponde al documento de finalizacion anticipada del acuerdo. |
| `descargarRef06` | Descarga el REF06 en base64. Devuelve el PDF del plan de actividades del acuerdo. |

Fuente: [QbidMcpTools.java](/C:/Users/alber/Documents/IdeaProjects/ceac-ai-tools-desktop/src/main/java/tools/ceac/ai/modules/qbid/interfaces/mcp/QbidMcpTools.java)
