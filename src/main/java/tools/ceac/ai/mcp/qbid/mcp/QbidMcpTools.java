package tools.ceac.ai.mcp.qbid.mcp;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import tools.ceac.ai.mcp.qbid.model.dto.GuardarInformeRequest;
import tools.ceac.ai.mcp.qbid.model.dto.GuardarPlanRequest;
import tools.ceac.ai.mcp.qbid.service.ActividadService;
import tools.ceac.ai.mcp.qbid.service.AlumnoService;
import tools.ceac.ai.mcp.qbid.service.ConveniosService;
import tools.ceac.ai.mcp.qbid.service.EmpresaService;
import tools.ceac.ai.mcp.qbid.service.PlanActividadesService;
import tools.ceac.ai.mcp.qbid.service.QbidUrls;
import tools.ceac.ai.mcp.qbid.service.ReferenciaService;
import tools.ceac.ai.mcp.qbid.service.SesionCache;
import tools.ceac.ai.mcp.qbid.service.ValoracionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

/**
 * Herramientas MCP expuestas por el runtime qBid.
 *
 * <p>La autenticacion OAuth del recurso protegido la resuelve Spring Security a nivel HTTP. Una
 * vez dentro del proceso, estas tools reutilizan las credenciales qBid ya cargadas en memoria por
 * el launcher o por el modo standalone. Las llamadas a qBid siguen resolviendose a traves de
 * {@link SesionCache}, que obtiene o renueva el {@code JSESSIONID} cuando hace falta.
 */
@Component
public class QbidMcpTools {

    private static final Logger log = LoggerFactory.getLogger(QbidMcpTools.class);

    private final McpAuthHelper          mcpAuthHelper;
    private final SesionCache            sesionCache;
    private final ConveniosService       conveniosService;
    private final ActividadService       actividadService;
    private final PlanActividadesService planActividadesService;
    private final ReferenciaService      referenciaService;
    private final ValoracionService      valoracionService;
    private final AlumnoService          alumnoService;
    private final EmpresaService         empresaService;
    private final QbidUrls               qbidUrls;
    private final ObjectMapper           mapper;

    public QbidMcpTools(McpAuthHelper mcpAuthHelper,
                        SesionCache sesionCache,
                        ConveniosService conveniosService,
                        ActividadService actividadService,
                        PlanActividadesService planActividadesService,
                        ReferenciaService referenciaService,
                        ValoracionService valoracionService,
                        AlumnoService alumnoService,
                        EmpresaService empresaService,
                        QbidUrls qbidUrls,
                        ObjectMapper mapper) {
        this.mcpAuthHelper          = mcpAuthHelper;
        this.sesionCache            = sesionCache;
        this.conveniosService       = conveniosService;
        this.actividadService       = actividadService;
        this.planActividadesService = planActividadesService;
        this.referenciaService      = referenciaService;
        this.valoracionService      = valoracionService;
        this.alumnoService          = alumnoService;
        this.empresaService         = empresaService;
        this.qbidUrls               = qbidUrls;
        this.mapper                 = mapper;
    }

    private String jsessionid() throws Exception {
        String basicAuth = mcpAuthHelper.buildBasicAuthHeader();
        return sesionCache.resolveSession(basicAuth);
    }

    // ── Convenios ─────────────────────────────────────────────────────────────

    @Tool(description = """
            Lista todos los convenios FCT del tutor.
            El parámetro plan puede ser MODGEN (módulos genéricos) o BID (ciclos ordinarios).
            Devuelve codConvenio, codTemporal, codCuaderno, newSystem y datos del alumno y empresa.
            """)
    public String listarConvenios(String plan) throws Exception {
        log.info("[QbidMcpTools] TOOL listarConvenios  plan={}  thread={}", plan, Thread.currentThread().getName());
        String result = mapper.writeValueAsString(
                conveniosService.getListado(jsessionid(), plan != null ? plan : "MODGEN"));
        log.info("[QbidMcpTools] TOOL listarConvenios DONE  resultLen={}", result.length());
        return result;
    }

    @Tool(description = """
            Obtiene el detalle completo de un convenio: alumno, empresa, tutor empresa,
            fechas de inicio y fin, horas totales y estado.
            Requiere codConvenio, codTemporal y newSystem (obtenidos de listarConvenios).
            """)
    public String verDetalleConvenio(String codConvenio,
                                     String codTemporal,
                                     String newSystem) throws Exception {
        log.info("[QbidMcpTools] TOOL verDetalleConvenio  codConvenio={}  codTemporal={}  thread={}", codConvenio, codTemporal, Thread.currentThread().getName());
        String result = mapper.writeValueAsString(
                conveniosService.getDetalle(jsessionid(), codConvenio, codTemporal, newSystem));
        log.info("[QbidMcpTools] TOOL verDetalleConvenio DONE  resultLen={}", result.length());
        return result;
    }

    // ── Cuaderno ──────────────────────────────────────────────────────────────

    @Tool(description = """
            Obtiene el estado del cuaderno de prácticas (FFE) del alumno.
            Incluye estado de los documentos REF05, REF18, REF19 y REF20,
            si el cuaderno está cerrado, y datos del convenio asociado.
            Requiere codCuaderno (obtenido de listarConvenios).
            """)
    public String verCuaderno(String codCuaderno) throws Exception {
        log.info("[QbidMcpTools] TOOL verCuaderno  codCuaderno={}  thread={}", codCuaderno, Thread.currentThread().getName());
        String result = mapper.writeValueAsString(conveniosService.getCuaderno(jsessionid(), codCuaderno));
        log.info("[QbidMcpTools] TOOL verCuaderno DONE  resultLen={}", result.length());
        return result;
    }

    // ── Seguimiento FCT ───────────────────────────────────────────────────────

    @Tool(description = """
            Obtiene el seguimiento formativo FCT del alumno en el convenio.
            Incluye resultados de aprendizaje, criterios de evaluación y estado de cada uno.
            Requiere codConvenio, codTemporal y newSystem.
            """)
    public String verSeguimientoFCT(String codConvenio,
                                    String codTemporal,
                                    String newSystem) throws Exception {
        log.info("[QbidMcpTools] TOOL verSeguimientoFCT  codConvenio={}  codTemporal={}  thread={}", codConvenio, codTemporal, Thread.currentThread().getName());
        String result = mapper.writeValueAsString(
                conveniosService.getSeguimiento(jsessionid(), codConvenio, codTemporal, newSystem));
        log.info("[QbidMcpTools] TOOL verSeguimientoFCT DONE  resultLen={}", result.length());
        return result;
    }

    // ── Agenda ────────────────────────────────────────────────────────────────

    @Tool(description = """
            Obtiene la agenda mensual del alumno: días fichados, horas registradas
            y estado de cada día del mes actual.
            También incluye la lista de informes periódicos del convenio.
            Requiere codConvenio y codTemporal.
            """)
    public String verAgenda(String codConvenio, String codTemporal) throws Exception {
        log.info("[QbidMcpTools] TOOL verAgenda  codConvenio={}  codTemporal={}  thread={}", codConvenio, codTemporal, Thread.currentThread().getName());
        String result = mapper.writeValueAsString(
                conveniosService.getAgenda(jsessionid(), codConvenio, codTemporal));
        log.info("[QbidMcpTools] TOOL verAgenda DONE  resultLen={}", result.length());
        return result;
    }

    // ── Informes periódicos ───────────────────────────────────────────────────

    @Tool(description = """
            Lista todos los informes periódicos (mensuales) de un convenio.
            Devuelve conveniValoracioPk, periodo, estado de firma y hashCode de cada informe.
            Los valores conveniValoracioPk y hashCode son necesarios para verDetalleInforme.
            Requiere codConvenio y codTemporal.
            """)
    public String listarInformes(String codConvenio, String codTemporal) throws Exception {
        log.info("[QbidMcpTools] TOOL listarInformes  codConvenio={}  codTemporal={}  thread={}", codConvenio, codTemporal, Thread.currentThread().getName());
        String result = mapper.writeValueAsString(
                conveniosService.getInformes(jsessionid(), codConvenio, codTemporal));
        log.info("[QbidMcpTools] TOOL listarInformes DONE  resultLen={}", result.length());
        return result;
    }

    @Tool(description = """
            Obtiene el detalle completo de un informe periódico mensual:
            actividades realizadas, horas por actividad, valoraciones del tutor y empresa,
            observaciones de alumno/empresa/tutor, estado de firma y días no gestionados.
            Requiere codConvenio, codTemporal, conveniValoracioPk y hashCode
            (obtenidos de listarInformes).
            """)
    public String verDetalleInforme(String codConvenio,
                                    String codTemporal,
                                    String conveniValoracioPk,
                                    String hashCode) throws Exception {
        log.info("[QbidMcpTools] TOOL verDetalleInforme  codConvenio={}  codTemporal={}  pk={}  thread={}", codConvenio, codTemporal, conveniValoracioPk, Thread.currentThread().getName());
        String result = mapper.writeValueAsString(
                conveniosService.getDetalleInforme(
                        jsessionid(), codConvenio, codTemporal, conveniValoracioPk, hashCode));
        log.info("[QbidMcpTools] TOOL verDetalleInforme DONE  resultLen={}", result.length());
        return result;
    }

    // ── Guardar informe periódico ─────────────────────────────────────────────

    @Tool(description = """
            Guarda el borrador de un informe periódico mensual (acción reversible).
            El informe queda actualizado pero sigue siendo editable.
            Para bloquearlo definitivamente usa la acción de firmar.

            Parámetros de identificación (obtenidos de listarInformes o verDetalleInforme):
              conveniValoracioPk, codConvenio, codTemporal, hashCode

            valoracionesJson: array JSON con una entrada por cada actividad del informe.
            Formato:
              [{"actividadId":"92245","adecuacionTutor":"ADECUADA","valoracionEmpresa":"MUY_BUENA"}, ...]
            Los actividadId se obtienen de verDetalleInforme (actividades[].id).

            Valores para adecuacionTutor — adecuación de las tareas del alumno/a (inp_XXXXX):
              MUY_ADECUADA  → código 29
              ADECUADA      → código 30
              POCO_ADECUADA → código 31
              NADA_ADECUADA → código 32
              SIN_VALORACION→ código 33 (usar para ítems con 0h)

            Valores para valoracionEmpresa — valoración de la empresa sobre el alumno/a (inp_extra_XXXXX):
              MUY_BUENA     → código 81  (nota 8-10)
              BUENA         → código 82  (nota 6-8)
              SUFICIENTE    → código 83  (nota 5)
              PASIVA        → código 84  (nota 3-4)
              NEGATIVA      → código 85  (nota 0-2)
              SIN_VALORACION→ código 86

            Regla importante: si alguna valoracionEmpresa es NEGATIVA,
            observacionesTutor es OBLIGATORIO antes de guardar.

            observacionesAlumno, observacionesEmpresa, observacionesTutor:
              texto libre, máx 1000 caracteres; pasar null o "" si no se quiere incluir.
            signatarioEmpresa: nombre completo del tutor/a empresa (editable).

            Devuelve el DetalleInformeDTO actualizado con el nuevo estado guardado.
            """)
    public String guardarInforme(String conveniValoracioPk,
                                 String codConvenio,
                                 String codTemporal,
                                 String hashCode,
                                 String valoracionesJson,
                                 String observacionesAlumno,
                                 String observacionesEmpresa,
                                 String observacionesTutor,
                                 String signatarioEmpresa) throws Exception {
        log.info("[QbidMcpTools] TOOL guardarInforme  pk={}  codConvenio={}  thread={}", conveniValoracioPk, codConvenio, Thread.currentThread().getName());

        List<GuardarInformeRequest.ValoracionItem> valoraciones = mapper.readValue(
                valoracionesJson,
                new TypeReference<List<GuardarInformeRequest.ValoracionItem>>() {});

        GuardarInformeRequest req = new GuardarInformeRequest();
        req.setConveniValoracioPk(conveniValoracioPk);
        req.setCodConvenio(codConvenio);
        req.setCodTemporal(codTemporal);
        req.setHashCode(hashCode);
        req.setValoraciones(valoraciones);
        req.setObservacionesAlumno(observacionesAlumno);
        req.setObservacionesEmpresa(observacionesEmpresa);
        req.setObservacionesTutor(observacionesTutor);
        req.setSignatarioEmpresa(signatarioEmpresa);

        String result = mapper.writeValueAsString(conveniosService.guardarInforme(jsessionid(), req));
        log.info("[QbidMcpTools] TOOL guardarInforme DONE  resultLen={}", result.length());
        return result;
    }

    // ── Firmar informe periódico ──────────────────────────────────────────────

    @Tool(description = """
            Firma (valida definitivamente) un informe periódico mensual. ACCIÓN IRREVERSIBLE.
            Una vez firmado, el informe queda bloqueado y ya no puede ser modificado.
            Usar solo cuando alumno/a y empresa ya han dado su conformidad.

            Acepta exactamente los mismos parámetros que guardarInforme:
            conveniValoracioPk, codConvenio, codTemporal, hashCode, valoracionesJson,
            observacionesAlumno, observacionesEmpresa, observacionesTutor, signatarioEmpresa.

            valoracionesJson: array JSON con una entrada por cada actividad del informe.
            Formato:
              [{"actividadId":"92245","adecuacionTutor":"ADECUADA","valoracionEmpresa":"MUY_BUENA"}, ...]

            Valores para adecuacionTutor (inp_XXXXX):
              MUY_ADECUADA → 29 | ADECUADA → 30 | POCO_ADECUADA → 31 | NADA_ADECUADA → 32 | SIN_VALORACION → 33

            Valores para valoracionEmpresa (inp_extra_XXXXX):
              MUY_BUENA (8-10) → 81 | BUENA (6-8) → 82 | SUFICIENTE (5) → 83
              PASIVA (3-4) → 84 | NEGATIVA (0-2) → 85 | SIN_VALORACION → 86

            Regla: si alguna valoracionEmpresa es NEGATIVA, observacionesTutor es OBLIGATORIO.

            Devuelve el DetalleInformeDTO con editable=false y firmadoTutor=true si la firma fue correcta.
            """)
    public String firmarInforme(String conveniValoracioPk,
                                String codConvenio,
                                String codTemporal,
                                String hashCode,
                                String valoracionesJson,
                                String observacionesAlumno,
                                String observacionesEmpresa,
                                String observacionesTutor,
                                String signatarioEmpresa) throws Exception {
        log.info("[QbidMcpTools] TOOL firmarInforme  pk={}  codConvenio={}  thread={}", conveniValoracioPk, codConvenio, Thread.currentThread().getName());

        List<GuardarInformeRequest.ValoracionItem> valoraciones = mapper.readValue(
                valoracionesJson,
                new TypeReference<List<GuardarInformeRequest.ValoracionItem>>() {});

        GuardarInformeRequest req = new GuardarInformeRequest();
        req.setConveniValoracioPk(conveniValoracioPk);
        req.setCodConvenio(codConvenio);
        req.setCodTemporal(codTemporal);
        req.setHashCode(hashCode);
        req.setValoraciones(valoraciones);
        req.setObservacionesAlumno(observacionesAlumno);
        req.setObservacionesEmpresa(observacionesEmpresa);
        req.setObservacionesTutor(observacionesTutor);
        req.setSignatarioEmpresa(signatarioEmpresa);

        String result = mapper.writeValueAsString(conveniosService.firmarInforme(jsessionid(), req));
        log.info("[QbidMcpTools] TOOL firmarInforme DONE  resultLen={}", result.length());
        return result;
    }

    // ── Referencias (documentos PDF en base64) ────────────────────────────────

    @Tool(description = """
            Descarga el documento REF19 (Valoración global del cuaderno) en formato base64.
            Devuelve filename, contentType, encoding y data (base64) para que puedas
            guardarlo como fichero PDF.
            language puede ser SP (defecto), CA, EN o FR.
            Requiere quadernId (obtenido de verCuaderno).
            """)
    public String descargarRef19(String quadernId,
                                  String language) throws Exception {
        log.info("[QbidMcpTools] TOOL descargarRef19  quadernId={}  thread={}", quadernId, Thread.currentThread().getName());
        String lang = (language != null && !language.isBlank()) ? language : "SP";
        String result = mapper.writeValueAsString(
                referenciaService.getDocumento(
                        jsessionid(),
                        qbidUrls.ref19(quadernId, lang),
                        "REF19_" + quadernId + ".pdf"));
        log.info("[QbidMcpTools] TOOL descargarRef19 DONE  resultLen={}", result.length());
        return result;
    }

    @Tool(description = """
            Descarga el documento REF20 (Calificación final FCT) en formato base64.
            Devuelve filename, contentType, encoding y data (base64) para que puedas
            guardarlo como fichero PDF.
            language puede ser SP (defecto), CA, EN o FR.
            Requiere quadernId (obtenido de verCuaderno).
            """)
    public String descargarRef20(String quadernId,
                                  String language) throws Exception {
        log.info("[QbidMcpTools] TOOL descargarRef20  quadernId={}  thread={}", quadernId, Thread.currentThread().getName());
        String lang = (language != null && !language.isBlank()) ? language : "SP";
        String result = mapper.writeValueAsString(
                referenciaService.getDocumento(
                        jsessionid(),
                        qbidUrls.ref20(quadernId, lang),
                        "REF20_" + quadernId + ".pdf"));
        log.info("[QbidMcpTools] TOOL descargarRef20 DONE  resultLen={}", result.length());
        return result;
    }

    @Tool(description = """
            Descarga el documento REF22 (Expediente del cuaderno) en formato base64.
            Devuelve filename, contentType, encoding y data (base64) para que puedas
            guardarlo como fichero PDF.
            language puede ser SP (defecto), CA, EN o FR.
            Requiere quadernId (obtenido de verCuaderno).
            """)
    public String descargarRef22(String quadernId,
                                  String language) throws Exception {
        log.info("[QbidMcpTools] TOOL descargarRef22  quadernId={}  thread={}", quadernId, Thread.currentThread().getName());
        String lang = (language != null && !language.isBlank()) ? language : "SP";
        String result = mapper.writeValueAsString(
                referenciaService.getDocumento(
                        jsessionid(),
                        qbidUrls.ref22(quadernId, lang),
                        "REF22_" + quadernId + ".pdf"));
        log.info("[QbidMcpTools] TOOL descargarRef22 DONE  resultLen={}", result.length());
        return result;
    }

    // ── Plan de actividades ───────────────────────────────────────────────────

    @Tool(description = """
            Obtiene el plan de actividades formativas asignadas al convenio FCT.
            Incluye la lista de ítems/funciones con su texto descriptivo, nivel jerárquico
            (0=raíz, 1=subactividad), si están seleccionadas para ese convenio y si son editables.
            Requiere codConvenio, codTemporal y newSystem (obtenidos de listarConvenios).
            """)
    public String verPlanActividades(String codConvenio,
                                     String codTemporal,
                                     String newSystem) throws Exception {
        log.info("[QbidMcpTools] TOOL verPlanActividades  codConvenio={}  codTemporal={}  thread={}", codConvenio, codTemporal, Thread.currentThread().getName());
        String result = mapper.writeValueAsString(
                planActividadesService.getPlan(
                        jsessionid(), codConvenio, codTemporal, newSystem != null ? newSystem : ""));
        log.info("[QbidMcpTools] TOOL verPlanActividades DONE  resultLen={}", result.length());
        return result;
    }

    // ── Guardar plan de actividades ───────────────────────────────────────────

    @Tool(description = """
            Guarda el plan de actividades formativas de un convenio (borrador, editable).
            Marca qué actividades ha realizado el alumno/a y actualiza el responsable empresa.

            Parámetros de identificación:
              codConvenio, codTemporal, newSystem — del convenio (listarConvenios).
              conveniPlaPk — PK del plan a guardar (verPlanActividades → PlanActividadesDTO.conveniPlaPk).

            actividadesSeleccionadasJson: array JSON con los IDs de las actividades a marcar.
            Incluir SIEMPRE tanto los IDs de nivel 0 (grupos raíz) como los de nivel 1 (subactividades).
            Los grupos raíz (nivel 0) deben incluirse explícitamente si se quieren activar,
            NO se gestionan automáticamente.
            Ejemplo: ["92244","92245","92246","92247","92248","92250"]
            Los IDs se obtienen de verPlanActividades (actividades[].id).
            
            responsableEmpresa: nombre completo del tutor/a empresa responsable del plan.
              Si se pasa null, se mantiene el nombre actual.

            recursosActivitat: descripción de instalaciones y equipamientos del centro de trabajo.
              Si se pasa null, se mantiene el valor actual.

            Devuelve la lista actualizada de PlanActividadesDTO con el nuevo estado del plan.
            """)
    public String guardarPlanActividades(String codConvenio,
                                         String codTemporal,
                                         String newSystem,
                                         String conveniPlaPk,
                                         String actividadesSeleccionadasJson,
                                         String responsableEmpresa,
                                         String recursosActivitat) throws Exception {
        log.info("[QbidMcpTools] TOOL guardarPlanActividades  codConvenio={}  pk={}  thread={}", codConvenio, conveniPlaPk, Thread.currentThread().getName());

        List<String> actividades = mapper.readValue(
                actividadesSeleccionadasJson,
                new TypeReference<List<String>>() {});

        GuardarPlanRequest req = new GuardarPlanRequest();
        req.setCodConvenio(codConvenio);
        req.setCodTemporal(codTemporal);
        req.setNewSystem(newSystem);
        req.setConveniPlaPk(conveniPlaPk);
        req.setActividadesSeleccionadas(actividades);
        req.setResponsableEmpresa(responsableEmpresa);
        req.setRecursosActivitat(recursosActivitat);

        String result = mapper.writeValueAsString(planActividadesService.guardarPlan(jsessionid(), req));
        log.info("[QbidMcpTools] TOOL guardarPlanActividades DONE  resultLen={}", result.length());
        return result;
    }

    @Tool(description = """
            Valida (firma definitivamente) el plan de actividades de un convenio FCT.
            ACCIÓN IRREVERSIBLE: una vez validado el plan no puede editarse.

            Parámetros iguales que guardarPlanActividades:
              codConvenio, codTemporal, newSystem, conveniPlaPk: identifican el convenio y el plan.

            actividadesSeleccionadasJson: array JSON con los IDs de las actividades a marcar.
            Incluir SIEMPRE tanto los IDs de nivel 0 (grupos raíz) como los de nivel 1 (subactividades).
            Los grupos raíz (nivel 0) deben incluirse explícitamente si se quieren activar,
            NO se gestionan automáticamente.
            Ejemplo: ["92244","92245","92246","92247","92248","92250"]
            Los IDs se obtienen de verPlanActividades (actividades[].id).

              responsableEmpresa: nombre completo del tutor/a empresa.
                Si se pasa null, se mantiene el nombre actual.

              recursosActivitat: descripción de instalaciones y equipamientos.
                Si se pasa null, se mantiene el valor actual.

            Devuelve la lista actualizada de PlanActividadesDTO tras la validación.
            """)
    public String validarPlanActividades(String codConvenio,
                                         String codTemporal,
                                         String newSystem,
                                         String conveniPlaPk,
                                         String actividadesSeleccionadasJson,
                                         String responsableEmpresa,
                                         String recursosActivitat) throws Exception {
        log.info("[QbidMcpTools] TOOL validarPlanActividades  codConvenio={}  pk={}  thread={}", codConvenio, conveniPlaPk, Thread.currentThread().getName());

        List<String> actividades = mapper.readValue(
                actividadesSeleccionadasJson,
                new TypeReference<List<String>>() {});

        GuardarPlanRequest req = new GuardarPlanRequest();
        req.setCodConvenio(codConvenio);
        req.setCodTemporal(codTemporal);
        req.setNewSystem(newSystem);
        req.setConveniPlaPk(conveniPlaPk);
        req.setActividadesSeleccionadas(actividades);
        req.setResponsableEmpresa(responsableEmpresa);
        req.setRecursosActivitat(recursosActivitat);

        String result = mapper.writeValueAsString(planActividadesService.validarPlan(jsessionid(), req));
        log.info("[QbidMcpTools] TOOL validarPlanActividades DONE  resultLen={}", result.length());
        return result;
    }

    // ── Actividad diaria ──────────────────────────────────────────────────────

    @Tool(description = """
            Obtiene lo que registró el alumno en un día concreto:
            actividades realizadas, horas y observaciones.
            La fecha debe estar en formato YYYYMMDD.
            Requiere codAlumno (del detalle de convenio), codConvenio, codTemporal y fecha.
            """)
    public String verActividadDiaria(String codAlumno,
                                     String codConvenio,
                                     String codTemporal,
                                     String fecha) throws Exception {
        log.info("[QbidMcpTools] TOOL verActividadDiaria  codAlumno={}  fecha={}  thread={}", codAlumno, fecha, Thread.currentThread().getName());
        String result = mapper.writeValueAsString(
                actividadService.getActividad(jsessionid(), codAlumno, codConvenio, codTemporal, fecha));
        log.info("[QbidMcpTools] TOOL verActividadDiaria DONE  resultLen={}", result.length());
        return result;
    }

    // ── Valoración final ──────────────────────────────────────────────────────

    @Tool(description = """
            Obtiene la valoración final emitida por la empresa para el alumno:
            calificación global, subclasificación, fecha, 21 criterios de evaluación,
            datos de contacto del evaluador, observaciones y signatarios.
            codVisitaValoracion y codVisita se obtienen del seguimiento FCT
            (SeguimientoDTO.valoracion.codVisitaValoracion y codVisitaRef18).
            """)
    public String verValoracionFinal(String codConvenio,
                                     String codTemporal,
                                     String codVisitaValoracion,
                                     String codVisita) throws Exception {
        log.info("[QbidMcpTools] TOOL verValoracionFinal  codConvenio={}  codVisitaVal={}  thread={}", codConvenio, codVisitaValoracion, Thread.currentThread().getName());
        String result = mapper.writeValueAsString(
                valoracionService.getValoracion(
                        jsessionid(), codConvenio, codTemporal, codVisitaValoracion, codVisita));
        log.info("[QbidMcpTools] TOOL verValoracionFinal DONE  resultLen={}", result.length());
        return result;
    }

    // ── Ficha alumno/a ────────────────────────────────────────────────────────

    @Tool(description = """
            Obtiene la ficha completa del alumno/a: datos personales (nombre, apellidos,
            fecha de nacimiento, sexo, dirección, teléfono, email, INSS/NASS),
            usuario de acceso a qBID y lista de cuadernos asociados con su estado.
            codAlumno se obtiene de verSeguimientoFCT (cabecera.codAlumno)
            o de verDetalleConvenio.
            """)
    public String verFichaAlumno(String codAlumno) throws Exception {
        log.info("[QbidMcpTools] TOOL verFichaAlumno  codAlumno={}  thread={}", codAlumno, Thread.currentThread().getName());
        String result = mapper.writeValueAsString(alumnoService.getFicha(jsessionid(), codAlumno));
        log.info("[QbidMcpTools] TOOL verFichaAlumno DONE  resultLen={}", result.length());
        return result;
    }

    // ── Empresa y centros de trabajo ──────────────────────────────────────────

    @Tool(description = """
            Obtiene los datos de la empresa: CIF/NIF, nombre, tipo, sector,
            número de trabajadores, dirección de actividad y lista de centros de trabajo
            con su nombre y codCentro.
            codEmpresa se obtiene de verDetalleConvenio (campo codEmpresa).
            Si codEmpresa viene vacío en el detalle, el HTML de qBID no expone ese dato
            en esa pantalla y no es posible usarlo directamente.
            """)
    public String verEmpresa(String codEmpresa) throws Exception {
        log.info("[QbidMcpTools] TOOL verEmpresa  codEmpresa={}  thread={}", codEmpresa, Thread.currentThread().getName());
        String result = mapper.writeValueAsString(empresaService.getEmpresa(jsessionid(), codEmpresa));
        log.info("[QbidMcpTools] TOOL verEmpresa DONE  resultLen={}", result.length());
        return result;
    }

    @Tool(description = """
            Obtiene la ficha de un centro de trabajo: nomenclatura, estado, categoría,
            nombre, dirección, teléfono, fax, correo electrónico y actividad (CCAE).
            Es el contacto directo donde el alumno realiza las prácticas.
            codEmpresa y codCentro se obtienen de verEmpresa (campo centros[].codCentro).
            """)
    public String verCentroTrabajo(String codEmpresa, String codCentro) throws Exception {
        log.info("[QbidMcpTools] TOOL verCentroTrabajo  codEmpresa={}  codCentro={}  thread={}", codEmpresa, codCentro, Thread.currentThread().getName());
        String result = mapper.writeValueAsString(
                empresaService.getCentroTrabajo(jsessionid(), codEmpresa, codCentro));
        log.info("[QbidMcpTools] TOOL verCentroTrabajo DONE  resultLen={}", result.length());
        return result;
    }

    // ── Referencias adicionales (PDFs en base64) ──────────────────────────────

    @Tool(description = """
            Descarga el REF07 (Seguimiento de la FPCT) en base64.
            language: SP (defecto), CA, EN, FR.
            codConvenio y codTemporal del convenio.
            """)
    public String descargarRef07(String codConvenio, String codTemporal, String language) throws Exception {
        String lang = lang(language);
        return mapper.writeValueAsString(referenciaService.getDocumento(
                jsessionid(), qbidUrls.ref07(codConvenio, codTemporal, lang),
                "REF07_" + codConvenio + ".pdf"));
    }

    @Tool(description = """
            Descarga el REF10 (Cuestionario al Centro de Trabajo) en base64.
            language: SP (defecto), CA, EN, FR.
            estudiId se obtiene del seguimiento FCT (cabecera.codAlumno o DetalleConvenioDTO.estudiId).
            """)
    public String descargarRef10(String codConvenio, String codTemporal,
                                  String estudiId, String language) throws Exception {
        String lang = lang(language);
        return mapper.writeValueAsString(referenciaService.getDocumento(
                jsessionid(), qbidUrls.ref10(codConvenio, codTemporal, estudiId, lang),
                "REF10_" + codConvenio + ".pdf"));
    }

    @Tool(description = """
            Descarga el REF11 (Homologación) en base64.
            language: SP (defecto), CA, EN, FR.
            estudiId se obtiene de DetalleConvenioDTO.
            """)
    public String descargarRef11(String codConvenio, String codTemporal,
                                  String estudiId, String language) throws Exception {
        String lang = lang(language);
        return mapper.writeValueAsString(referenciaService.getDocumento(
                jsessionid(), qbidUrls.ref11(codConvenio, codTemporal, estudiId, lang),
                "REF11_" + codConvenio + ".pdf"));
    }

    @Tool(description = """
            Descarga el REF15 (Valoración de evaluación de la empresa) en base64.
            language: SP (defecto), CA, EN, FR.
            codVisita se obtiene del seguimiento FCT (valoracion.codVisitaRef18).
            """)
    public String descargarRef15(String codConvenio, String codTemporal,
                                  String codVisita, String language) throws Exception {
        String lang = lang(language);
        return mapper.writeValueAsString(referenciaService.getDocumento(
                jsessionid(), qbidUrls.ref15(codConvenio, codTemporal, codVisita, lang),
                "REF15_" + codConvenio + ".pdf"));
    }

    @Tool(description = """
            Descarga el REF18 (Valoración del expediente) en base64.
            language: SP (defecto), CA, EN, FR.
            codVisita se obtiene del seguimiento FCT (valoracion.codVisitaRef18).
            """)
    public String descargarRef18(String codConvenio, String codTemporal,
                                  String codVisita, String language) throws Exception {
        String lang = lang(language);
        return mapper.writeValueAsString(referenciaService.getDocumento(
                jsessionid(), qbidUrls.ref18(codConvenio, codTemporal, codVisita, lang),
                "REF18_" + codConvenio + ".pdf"));
    }

    @Tool(description = """
            Descarga el REF05 (Documento del acuerdo/convenio firmado) en base64.
            language: SP (defecto), CA, EN, FR.
            hash y cursSeleccio se obtienen de DetalleConvenioDTO.
            """)
    public String descargarRef05(String codConvenio, String codTemporal,
                                  String hash, String cursSeleccio, String language) throws Exception {
        String lang = lang(language);
        return mapper.writeValueAsString(referenciaService.getDocumento(
                jsessionid(), qbidUrls.ref05(codConvenio, codTemporal, hash, cursSeleccio, lang),
                "REF05_" + codConvenio + ".pdf"));
    }

    @Tool(description = """
            Descarga el REF05-Baja (Documento de finalización anticipada del acuerdo) en base64.
            language: SP (defecto), CA, EN, FR.
            hash y cursSeleccio se obtienen de DetalleConvenioDTO (hashCodeRef05Baja y cursSeleccio).
            """)
    public String descargarRef05Baja(String codConvenio, String codTemporal,
                                      String hash, String cursSeleccio, String language) throws Exception {
        String lang = lang(language);
        return mapper.writeValueAsString(referenciaService.getDocumento(
                jsessionid(), qbidUrls.ref05Baja(codConvenio, codTemporal, hash, cursSeleccio, lang),
                "REF05_Baja_" + codConvenio + ".pdf"));
    }

    @Tool(description = """
            Descarga el REF06 (Plan de actividades del acuerdo) en base64.
            language: SP (defecto), CA, EN, FR.
            estudiId se obtiene de DetalleConvenioDTO.
            """)
    public String descargarRef06(String codConvenio, String codTemporal,
                                  String estudiId, String language) throws Exception {
        String lang = lang(language);
        return mapper.writeValueAsString(referenciaService.getDocumento(
                jsessionid(), qbidUrls.ref06(codConvenio, codTemporal, estudiId, lang),
                "REF06_" + codConvenio + ".pdf"));
    }

    // ── Util ──────────────────────────────────────────────────────────────────

    private String lang(String language) {
        return (language != null && !language.isBlank()) ? language : "SP";
    }
}

