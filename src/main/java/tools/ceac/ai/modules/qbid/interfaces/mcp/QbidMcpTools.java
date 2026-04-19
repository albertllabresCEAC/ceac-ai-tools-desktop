package tools.ceac.ai.modules.qbid.interfaces.mcp;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import tools.ceac.ai.modules.qbid.application.auth.QbidRuntimeCredentials;
import tools.ceac.ai.modules.qbid.application.port.out.QbidEndpointFactory;
import tools.ceac.ai.modules.qbid.domain.model.GuardarInformeRequest;
import tools.ceac.ai.modules.qbid.domain.model.GuardarPlanRequest;
import tools.ceac.ai.modules.qbid.application.service.ActividadService;
import tools.ceac.ai.modules.qbid.application.service.AlumnoService;
import tools.ceac.ai.modules.qbid.application.service.ConveniosService;
import tools.ceac.ai.modules.qbid.application.service.EmpresaService;
import tools.ceac.ai.modules.qbid.application.service.PlanActividadesService;
import tools.ceac.ai.modules.qbid.application.service.ReferenciaService;
import tools.ceac.ai.modules.qbid.application.service.SesionCache;
import tools.ceac.ai.modules.qbid.application.service.ValoracionService;
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

    private final QbidRuntimeCredentials runtimeCredentials;
    private final SesionCache            sesionCache;
    private final ConveniosService       conveniosService;
    private final ActividadService       actividadService;
    private final PlanActividadesService planActividadesService;
    private final ReferenciaService      referenciaService;
    private final ValoracionService      valoracionService;
    private final AlumnoService          alumnoService;
    private final EmpresaService         empresaService;
    private final QbidEndpointFactory    qbidUrls;
    private final ObjectMapper           mapper;

    public QbidMcpTools(QbidRuntimeCredentials runtimeCredentials,
                        SesionCache sesionCache,
                        ConveniosService conveniosService,
                        ActividadService actividadService,
                        PlanActividadesService planActividadesService,
                        ReferenciaService referenciaService,
                        ValoracionService valoracionService,
                        AlumnoService alumnoService,
                        EmpresaService empresaService,
                        QbidEndpointFactory qbidUrls,
                        ObjectMapper mapper) {
        this.runtimeCredentials     = runtimeCredentials;
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
        String basicAuth = runtimeCredentials.buildBasicAuthHeader();
        return sesionCache.resolveSession(basicAuth);
    }

    // Convenios

    @Tool(description = """
            Lista todos los convenios FCT del tutor.
            El parametro plan puede ser MODGEN (modulos genericos) o BID (ciclos ordinarios).
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
        if (codConvenio == null || codConvenio.isBlank()) {
            throw new IllegalArgumentException("verDetalleConvenio: codConvenio es obligatorio");
        }
        if (codTemporal == null || codTemporal.isBlank()) {
            throw new IllegalArgumentException("verDetalleConvenio: codTemporal es obligatorio");
        }
        if (newSystem == null || newSystem.isBlank()) {
            throw new IllegalArgumentException("verDetalleConvenio: newSystem es obligatorio");
        }
        log.info("[QbidMcpTools] TOOL verDetalleConvenio  codConvenio={}  codTemporal={}  thread={}", codConvenio, codTemporal, Thread.currentThread().getName());
        String result = mapper.writeValueAsString(
                conveniosService.getDetalle(jsessionid(), codConvenio, codTemporal, newSystem));
        log.info("[QbidMcpTools] TOOL verDetalleConvenio DONE  resultLen={}", result.length());
        return result;
    }

    // Cuaderno

    @Tool(description = """
            Obtiene el estado del cuaderno de practicas (FFE) del alumno.
            Incluye estado de los documentos REF05, REF18, REF19 y REF20,
            si el cuaderno esta cerrado, y datos del convenio asociado.
            Requiere codCuaderno (obtenido de listarConvenios).
            """)
    public String verCuaderno(String codCuaderno) throws Exception {
        if (codCuaderno == null || codCuaderno.isBlank()) {
            throw new IllegalArgumentException("verCuaderno: codCuaderno es obligatorio");
        }
        log.info("[QbidMcpTools] TOOL verCuaderno  codCuaderno={}  thread={}", codCuaderno, Thread.currentThread().getName());
        String result = mapper.writeValueAsString(conveniosService.getCuaderno(jsessionid(), codCuaderno));
        log.info("[QbidMcpTools] TOOL verCuaderno DONE  resultLen={}", result.length());
        return result;
    }

    // Seguimiento FCT

    @Tool(description = """
            Obtiene el seguimiento formativo FCT del alumno en el convenio.
            Incluye resultados de aprendizaje, criterios de evaluacion y estado de cada uno.
            Requiere codConvenio, codTemporal y newSystem.
            """)
    public String verSeguimientoFCT(String codConvenio,
                                    String codTemporal,
                                    String newSystem) throws Exception {
        if (codConvenio == null || codConvenio.isBlank()) {
            throw new IllegalArgumentException("verSeguimientoFCT: codConvenio es obligatorio");
        }
        if (codTemporal == null || codTemporal.isBlank()) {
            throw new IllegalArgumentException("verSeguimientoFCT: codTemporal es obligatorio");
        }
        if (newSystem == null || newSystem.isBlank()) {
            throw new IllegalArgumentException("verSeguimientoFCT: newSystem es obligatorio");
        }
        log.info("[QbidMcpTools] TOOL verSeguimientoFCT  codConvenio={}  codTemporal={}  thread={}", codConvenio, codTemporal, Thread.currentThread().getName());
        String result = mapper.writeValueAsString(
                conveniosService.getSeguimiento(jsessionid(), codConvenio, codTemporal, newSystem));
        log.info("[QbidMcpTools] TOOL verSeguimientoFCT DONE  resultLen={}", result.length());
        return result;
    }

    // Agenda

    @Tool(description = """
            Obtiene la agenda mensual del alumno: dias fichados, horas registradas
            y estado de cada dia del mes actual.
            Tambien incluye la lista de informes periodicos del convenio.
            Requiere codConvenio y codTemporal.
            """)
    public String verAgenda(String codConvenio, String codTemporal) throws Exception {
        if (codConvenio == null || codConvenio.isBlank()) {
            throw new IllegalArgumentException("verAgenda: codConvenio es obligatorio");
        }
        if (codTemporal == null || codTemporal.isBlank()) {
            throw new IllegalArgumentException("verAgenda: codTemporal es obligatorio");
        }
        log.info("[QbidMcpTools] TOOL verAgenda  codConvenio={}  codTemporal={}  thread={}", codConvenio, codTemporal, Thread.currentThread().getName());
        String result = mapper.writeValueAsString(
                conveniosService.getAgenda(jsessionid(), codConvenio, codTemporal));
        log.info("[QbidMcpTools] TOOL verAgenda DONE  resultLen={}", result.length());
        return result;
    }

    // Informes periodicos

    @Tool(description = """
            Lista todos los informes periodicos (mensuales) de un convenio.
            Devuelve conveniValoracioPk, periodo, estado de firma y hashCode de cada informe.
            Los valores conveniValoracioPk y hashCode son necesarios para verDetalleInforme.
            Requiere codConvenio y codTemporal.
            """)
    public String listarInformes(String codConvenio, String codTemporal) throws Exception {
        if (codConvenio == null || codConvenio.isBlank()) {
            throw new IllegalArgumentException("listarInformes: codConvenio es obligatorio");
        }
        if (codTemporal == null || codTemporal.isBlank()) {
            throw new IllegalArgumentException("listarInformes: codTemporal es obligatorio");
        }
        log.info("[QbidMcpTools] TOOL listarInformes  codConvenio={}  codTemporal={}  thread={}", codConvenio, codTemporal, Thread.currentThread().getName());
        String result = mapper.writeValueAsString(
                conveniosService.getInformes(jsessionid(), codConvenio, codTemporal));
        log.info("[QbidMcpTools] TOOL listarInformes DONE  resultLen={}", result.length());
        return result;
    }

    @Tool(description = """
            Obtiene el detalle completo de un informe periodico mensual:
            actividades realizadas, horas por actividad, valoraciones del tutor y empresa,
            observaciones de alumno/empresa/tutor, estado de firma y dias no gestionados.
            Requiere codConvenio, codTemporal, conveniValoracioPk y hashCode
            (obtenidos de listarInformes).
            """)
    public String verDetalleInforme(String codConvenio,
                                    String codTemporal,
                                    String conveniValoracioPk,
                                    String hashCode) throws Exception {
        if (codConvenio == null || codConvenio.isBlank()) {
            throw new IllegalArgumentException("verDetalleInforme: codConvenio es obligatorio");
        }
        if (codTemporal == null || codTemporal.isBlank()) {
            throw new IllegalArgumentException("verDetalleInforme: codTemporal es obligatorio");
        }
        if (conveniValoracioPk == null || conveniValoracioPk.isBlank()) {
            throw new IllegalArgumentException("verDetalleInforme: conveniValoracioPk es obligatorio");
        }
        if (hashCode == null || hashCode.isBlank()) {
            throw new IllegalArgumentException("verDetalleInforme: hashCode es obligatorio");
        }
        log.info("[QbidMcpTools] TOOL verDetalleInforme  codConvenio={}  codTemporal={}  pk={}  thread={}", codConvenio, codTemporal, conveniValoracioPk, Thread.currentThread().getName());
        String result = mapper.writeValueAsString(
                conveniosService.getDetalleInforme(
                        jsessionid(), codConvenio, codTemporal, conveniValoracioPk, hashCode));
        log.info("[QbidMcpTools] TOOL verDetalleInforme DONE  resultLen={}", result.length());
        return result;
    }

    // Guardado de informe periodico

    @Tool(description = """
            Guarda el borrador de un informe periodico mensual (accion reversible).
            El informe queda actualizado pero sigue siendo editable.
            Para bloquearlo definitivamente usa la accion de firmar.

            Parametros de identificacion (obtenidos de listarInformes o verDetalleInforme):
              conveniValoracioPk, codConvenio, codTemporal, hashCode

            valoracionesJson: array JSON con una entrada por cada actividad del informe.
            Formato:
              [{"actividadId":"92245","adecuacionTutor":"ADECUADA","valoracionEmpresa":"MUY_BUENA"}, ...]
            Los actividadId se obtienen de verDetalleInforme (actividades[].id).

            Valores para adecuacionTutor -> adecuacion de las tareas del alumno/a (inp_XXXXX):
              MUY_ADECUADA  -> codigo 29
              ADECUADA      -> codigo 30
              POCO_ADECUADA -> codigo 31
              NADA_ADECUADA -> codigo 32
              SIN_VALORACION -> codigo 33 (usar para items con 0h)

            Valores para valoracionEmpresa -> valoracion de la empresa sobre el alumno/a (inp_extra_XXXXX):
              MUY_BUENA     -> codigo 81  (nota 8-10)
              BUENA         -> codigo 82  (nota 6-8)
              SUFICIENTE    -> codigo 83  (nota 5)
              PASIVA        -> codigo 84  (nota 3-4)
              NEGATIVA      -> codigo 85  (nota 0-2)
              SIN_VALORACION -> codigo 86

            Regla importante: si alguna valoracionEmpresa es NEGATIVA,
            observacionesTutor es OBLIGATORIO antes de guardar.

            observacionesAlumno, observacionesEmpresa, observacionesTutor:
              texto libre, max 1000 caracteres; pasar null o "" si no se quiere incluir.
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
        if (conveniValoracioPk == null || conveniValoracioPk.isBlank()) {
            throw new IllegalArgumentException("guardarInforme: conveniValoracioPk es obligatorio");
        }
        if (codConvenio == null || codConvenio.isBlank()) {
            throw new IllegalArgumentException("guardarInforme: codConvenio es obligatorio");
        }
        if (codTemporal == null || codTemporal.isBlank()) {
            throw new IllegalArgumentException("guardarInforme: codTemporal es obligatorio");
        }
        if (hashCode == null || hashCode.isBlank()) {
            throw new IllegalArgumentException("guardarInforme: hashCode es obligatorio");
        }
        if (valoracionesJson == null || valoracionesJson.isBlank()) {
            throw new IllegalArgumentException("guardarInforme: valoracionesJson es obligatorio");
        }
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

    // Firma de informe periodico

    @Tool(description = """
            Firma (valida definitivamente) un informe periodico mensual. ACCION IRREVERSIBLE.
            Una vez firmado, el informe queda bloqueado y ya no puede ser modificado.
            Usar solo cuando alumno/a y empresa ya han dado su conformidad.

            Acepta exactamente los mismos parametros que guardarInforme:
            conveniValoracioPk, codConvenio, codTemporal, hashCode, valoracionesJson,
            observacionesAlumno, observacionesEmpresa, observacionesTutor, signatarioEmpresa.

            valoracionesJson: array JSON con una entrada por cada actividad del informe.
            Formato:
              [{"actividadId":"92245","adecuacionTutor":"ADECUADA","valoracionEmpresa":"MUY_BUENA"}, ...]

            Valores para adecuacionTutor (inp_XXXXX):
              MUY_ADECUADA -> 29 | ADECUADA -> 30 | POCO_ADECUADA -> 31 | NADA_ADECUADA -> 32 | SIN_VALORACION -> 33

            Valores para valoracionEmpresa (inp_extra_XXXXX):
              MUY_BUENA (8-10) -> 81 | BUENA (6-8) -> 82 | SUFICIENTE (5) -> 83
              PASIVA (3-4) -> 84 | NEGATIVA (0-2) -> 85 | SIN_VALORACION -> 86

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
        if (conveniValoracioPk == null || conveniValoracioPk.isBlank()) {
            throw new IllegalArgumentException("firmarInforme: conveniValoracioPk es obligatorio");
        }
        if (codConvenio == null || codConvenio.isBlank()) {
            throw new IllegalArgumentException("firmarInforme: codConvenio es obligatorio");
        }
        if (codTemporal == null || codTemporal.isBlank()) {
            throw new IllegalArgumentException("firmarInforme: codTemporal es obligatorio");
        }
        if (hashCode == null || hashCode.isBlank()) {
            throw new IllegalArgumentException("firmarInforme: hashCode es obligatorio");
        }
        if (valoracionesJson == null || valoracionesJson.isBlank()) {
            throw new IllegalArgumentException("firmarInforme: valoracionesJson es obligatorio");
        }
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

    // Referencias

    @Tool(description = """
            Descarga el documento REF19 (Valoracion global del cuaderno) en formato base64.
            Devuelve filename, contentType, encoding y data (base64) para que puedas
            guardarlo como fichero PDF.
            language puede ser SP (defecto), CA, EN o FR.
            Requiere quadernId (obtenido de verCuaderno).
            """)
    public String descargarRef19(String quadernId,
                                  String language) throws Exception {
        if (quadernId == null || quadernId.isBlank()) {
            throw new IllegalArgumentException("descargarRef19: quadernId es obligatorio");
        }
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
            Descarga el documento REF20 (Calificacion final FCT) en formato base64.
            Devuelve filename, contentType, encoding y data (base64) para que puedas
            guardarlo como fichero PDF.
            language puede ser SP (defecto), CA, EN o FR.
            Requiere quadernId (obtenido de verCuaderno).
            """)
    public String descargarRef20(String quadernId,
                                  String language) throws Exception {
        if (quadernId == null || quadernId.isBlank()) {
            throw new IllegalArgumentException("descargarRef20: quadernId es obligatorio");
        }
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
        if (quadernId == null || quadernId.isBlank()) {
            throw new IllegalArgumentException("descargarRef22: quadernId es obligatorio");
        }
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

    // Plan de actividades

    @Tool(description = """
            Obtiene el plan de actividades formativas asignadas al convenio FCT.
            Incluye la lista de items/funciones con su texto descriptivo, nivel jerarquico
            (0=raiz, 1=subactividad), si estan seleccionadas para ese convenio y si son editables.
            Requiere codConvenio, codTemporal y newSystem (obtenidos de listarConvenios).
            """)
    public String verPlanActividades(String codConvenio,
                                     String codTemporal,
                                     String newSystem) throws Exception {
        if (codConvenio == null || codConvenio.isBlank()) {
            throw new IllegalArgumentException("verPlanActividades: codConvenio es obligatorio");
        }
        if (codTemporal == null || codTemporal.isBlank()) {
            throw new IllegalArgumentException("verPlanActividades: codTemporal es obligatorio");
        }
        if (newSystem == null || newSystem.isBlank()) {
            throw new IllegalArgumentException("verPlanActividades: newSystem es obligatorio");
        }
        log.info("[QbidMcpTools] TOOL verPlanActividades  codConvenio={}  codTemporal={}  thread={}", codConvenio, codTemporal, Thread.currentThread().getName());
        String result = mapper.writeValueAsString(
                planActividadesService.getPlan(
                        jsessionid(), codConvenio, codTemporal, newSystem != null ? newSystem : ""));
        log.info("[QbidMcpTools] TOOL verPlanActividades DONE  resultLen={}", result.length());
        return result;
    }

    // Guardado del plan de actividades

    @Tool(description = """
            Guarda el plan de actividades formativas de un convenio (borrador, editable).
            Marca que actividades ha realizado el alumno/a y actualiza el responsable empresa.

            Parametros de identificacion:
              codConvenio, codTemporal, newSystem -> del convenio (listarConvenios).
              conveniPlaPk -> PK del plan a guardar (verPlanActividades -> PlanActividadesDTO.conveniPlaPk).

            actividadesSeleccionadasJson: array JSON con los IDs de las actividades a marcar.
            Incluir SIEMPRE tanto los IDs de nivel 0 (grupos raiz) como los de nivel 1 (subactividades).
            Los grupos raiz (nivel 0) deben incluirse explicitamente si se quieren activar,
            NO se gestionan automaticamente.
            Ejemplo: ["92244","92245","92246","92247","92248","92250"]
            Los IDs se obtienen de verPlanActividades (actividades[].id).
            
            responsableEmpresa: nombre completo del tutor/a empresa responsable del plan.
              Si se pasa null, se mantiene el nombre actual.

            recursosActivitat: descripcion de instalaciones y equipamientos del centro de trabajo.
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
        if (codConvenio == null || codConvenio.isBlank()) {
            throw new IllegalArgumentException("guardarPlanActividades: codConvenio es obligatorio");
        }
        if (codTemporal == null || codTemporal.isBlank()) {
            throw new IllegalArgumentException("guardarPlanActividades: codTemporal es obligatorio");
        }
        if (newSystem == null || newSystem.isBlank()) {
            throw new IllegalArgumentException("guardarPlanActividades: newSystem es obligatorio");
        }
        if (conveniPlaPk == null || conveniPlaPk.isBlank()) {
            throw new IllegalArgumentException("guardarPlanActividades: conveniPlaPk es obligatorio");
        }
        if (actividadesSeleccionadasJson == null || actividadesSeleccionadasJson.isBlank()) {
            throw new IllegalArgumentException("guardarPlanActividades: actividadesSeleccionadasJson es obligatorio");
        }
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
            ACCION IRREVERSIBLE: una vez validado el plan no puede editarse.

            Parametros iguales que guardarPlanActividades:
              codConvenio, codTemporal, newSystem, conveniPlaPk: identifican el convenio y el plan.

            actividadesSeleccionadasJson: array JSON con los IDs de las actividades a marcar.
            Incluir SIEMPRE tanto los IDs de nivel 0 (grupos raiz) como los de nivel 1 (subactividades).
            Los grupos raiz (nivel 0) deben incluirse explicitamente si se quieren activar,
            NO se gestionan automaticamente.
            Ejemplo: ["92244","92245","92246","92247","92248","92250"]
            Los IDs se obtienen de verPlanActividades (actividades[].id).

              responsableEmpresa: nombre completo del tutor/a empresa.
                Si se pasa null, se mantiene el nombre actual.

              recursosActivitat: descripcion de instalaciones y equipamientos.
                Si se pasa null, se mantiene el valor actual.

            Devuelve la lista actualizada de PlanActividadesDTO tras la validacion.
            """)
    public String validarPlanActividades(String codConvenio,
                                         String codTemporal,
                                         String newSystem,
                                         String conveniPlaPk,
                                         String actividadesSeleccionadasJson,
                                         String responsableEmpresa,
                                         String recursosActivitat) throws Exception {
        if (codConvenio == null || codConvenio.isBlank()) {
            throw new IllegalArgumentException("validarPlanActividades: codConvenio es obligatorio");
        }
        if (codTemporal == null || codTemporal.isBlank()) {
            throw new IllegalArgumentException("validarPlanActividades: codTemporal es obligatorio");
        }
        if (newSystem == null || newSystem.isBlank()) {
            throw new IllegalArgumentException("validarPlanActividades: newSystem es obligatorio");
        }
        if (conveniPlaPk == null || conveniPlaPk.isBlank()) {
            throw new IllegalArgumentException("validarPlanActividades: conveniPlaPk es obligatorio");
        }
        if (actividadesSeleccionadasJson == null || actividadesSeleccionadasJson.isBlank()) {
            throw new IllegalArgumentException("validarPlanActividades: actividadesSeleccionadasJson es obligatorio");
        }
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

    // Actividad diaria

    @Tool(description = """
            Obtiene lo que registro el alumno en un dia concreto:
            actividades realizadas, horas y observaciones.
            La fecha debe estar en formato YYYYMMDD.
            Requiere codAlumno (del detalle de convenio), codConvenio, codTemporal y fecha.
            """)
    public String verActividadDiaria(String codAlumno,
                                     String codConvenio,
                                     String codTemporal,
                                     String fecha) throws Exception {
        if (codAlumno == null || codAlumno.isBlank()) {
            throw new IllegalArgumentException("verActividadDiaria: codAlumno es obligatorio");
        }
        if (codConvenio == null || codConvenio.isBlank()) {
            throw new IllegalArgumentException("verActividadDiaria: codConvenio es obligatorio");
        }
        if (codTemporal == null || codTemporal.isBlank()) {
            throw new IllegalArgumentException("verActividadDiaria: codTemporal es obligatorio");
        }
        if (fecha == null || fecha.isBlank()) {
            throw new IllegalArgumentException("verActividadDiaria: fecha es obligatorio");
        }
        log.info("[QbidMcpTools] TOOL verActividadDiaria  codAlumno={}  fecha={}  thread={}", codAlumno, fecha, Thread.currentThread().getName());
        String result = mapper.writeValueAsString(
                actividadService.getActividad(jsessionid(), codAlumno, codConvenio, codTemporal, fecha));
        log.info("[QbidMcpTools] TOOL verActividadDiaria DONE  resultLen={}", result.length());
        return result;
    }

    // Valoracion final

    @Tool(description = """
            Obtiene la valoracion final emitida por la empresa para el alumno:
            calificacion global, subclasificacion, fecha, 21 criterios de evaluacion,
            datos de contacto del evaluador, observaciones y signatarios.
            codVisitaValoracion y codVisita se obtienen del seguimiento FCT
            (SeguimientoDTO.valoracion.codVisitaValoracion y codVisitaRef18).
            """)
    public String verValoracionFinal(String codConvenio,
                                     String codTemporal,
                                     String codVisitaValoracion,
                                     String codVisita) throws Exception {
        if (codConvenio == null || codConvenio.isBlank()) {
            throw new IllegalArgumentException("verValoracionFinal: codConvenio es obligatorio");
        }
        if (codTemporal == null || codTemporal.isBlank()) {
            throw new IllegalArgumentException("verValoracionFinal: codTemporal es obligatorio");
        }
        if (codVisitaValoracion == null || codVisitaValoracion.isBlank()) {
            throw new IllegalArgumentException("verValoracionFinal: codVisitaValoracion es obligatorio");
        }
        if (codVisita == null || codVisita.isBlank()) {
            throw new IllegalArgumentException("verValoracionFinal: codVisita es obligatorio");
        }
        log.info("[QbidMcpTools] TOOL verValoracionFinal  codConvenio={}  codVisitaVal={}  thread={}", codConvenio, codVisitaValoracion, Thread.currentThread().getName());
        String result = mapper.writeValueAsString(
                valoracionService.getValoracion(
                        jsessionid(), codConvenio, codTemporal, codVisitaValoracion, codVisita));
        log.info("[QbidMcpTools] TOOL verValoracionFinal DONE  resultLen={}", result.length());
        return result;
    }

    // Ficha del alumno/a

    @Tool(description = """
            Obtiene la ficha completa del alumno/a: datos personales (nombre, apellidos,
            fecha de nacimiento, sexo, direccion, telefono, email, INSS/NASS),
            usuario de acceso a qBID y lista de cuadernos asociados con su estado.
            codAlumno se obtiene de verSeguimientoFCT (cabecera.codAlumno)
            o de verDetalleConvenio.
            """)
    public String verFichaAlumno(String codAlumno) throws Exception {
        if (codAlumno == null || codAlumno.isBlank()) {
            throw new IllegalArgumentException("verFichaAlumno: codAlumno es obligatorio");
        }
        log.info("[QbidMcpTools] TOOL verFichaAlumno  codAlumno={}  thread={}", codAlumno, Thread.currentThread().getName());
        String result = mapper.writeValueAsString(alumnoService.getFicha(jsessionid(), codAlumno));
        log.info("[QbidMcpTools] TOOL verFichaAlumno DONE  resultLen={}", result.length());
        return result;
    }

    // Empresa y centros de trabajo

    @Tool(description = """
            Obtiene los datos de la empresa: CIF/NIF, nombre, tipo, sector,
            numero de trabajadores, direccion de actividad y lista de centros de trabajo
            con su nombre y codCentro.
            codEmpresa se obtiene de verDetalleConvenio (campo codEmpresa).
            Si codEmpresa viene vacio en el detalle, el HTML de qBID no expone ese dato
            en esa pantalla y no es posible usarlo directamente.
            """)
    public String verEmpresa(String codEmpresa) throws Exception {
        if (codEmpresa == null || codEmpresa.isBlank()) {
            throw new IllegalArgumentException("verEmpresa: codEmpresa es obligatorio");
        }
        log.info("[QbidMcpTools] TOOL verEmpresa  codEmpresa={}  thread={}", codEmpresa, Thread.currentThread().getName());
        String result = mapper.writeValueAsString(empresaService.getEmpresa(jsessionid(), codEmpresa));
        log.info("[QbidMcpTools] TOOL verEmpresa DONE  resultLen={}", result.length());
        return result;
    }

    @Tool(description = """
            Obtiene la ficha de un centro de trabajo: nomenclatura, estado, categoria,
            nombre, direccion, telefono, fax, correo electronico y actividad (CCAE).
            Es el contacto directo donde el alumno realiza las practicas.
            codEmpresa y codCentro se obtienen de verEmpresa (campo centros[].codCentro).
            """)
    public String verCentroTrabajo(String codEmpresa, String codCentro) throws Exception {
        if (codEmpresa == null || codEmpresa.isBlank()) {
            throw new IllegalArgumentException("verCentroTrabajo: codEmpresa es obligatorio");
        }
        if (codCentro == null || codCentro.isBlank()) {
            throw new IllegalArgumentException("verCentroTrabajo: codCentro es obligatorio");
        }
        log.info("[QbidMcpTools] TOOL verCentroTrabajo  codEmpresa={}  codCentro={}  thread={}", codEmpresa, codCentro, Thread.currentThread().getName());
        String result = mapper.writeValueAsString(
                empresaService.getCentroTrabajo(jsessionid(), codEmpresa, codCentro));
        log.info("[QbidMcpTools] TOOL verCentroTrabajo DONE  resultLen={}", result.length());
        return result;
    }

    // Referencias adicionales (PDF en base64)

    @Tool(description = """
            Descarga el REF07 (Seguimiento de la FPCT) en base64.
            language: SP (defecto), CA, EN, FR.
            codConvenio y codTemporal del convenio.
            """)
    public String descargarRef07(String codConvenio, String codTemporal, String language) throws Exception {
        if (codConvenio == null || codConvenio.isBlank()) {
            throw new IllegalArgumentException("descargarRef07: codConvenio es obligatorio");
        }
        if (codTemporal == null || codTemporal.isBlank()) {
            throw new IllegalArgumentException("descargarRef07: codTemporal es obligatorio");
        }
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
        if (codConvenio == null || codConvenio.isBlank()) {
            throw new IllegalArgumentException("descargarRef10: codConvenio es obligatorio");
        }
        if (codTemporal == null || codTemporal.isBlank()) {
            throw new IllegalArgumentException("descargarRef10: codTemporal es obligatorio");
        }
        if (estudiId == null || estudiId.isBlank()) {
            throw new IllegalArgumentException("descargarRef10: estudiId es obligatorio");
        }
        String lang = lang(language);
        return mapper.writeValueAsString(referenciaService.getDocumento(
                jsessionid(), qbidUrls.ref10(codConvenio, codTemporal, estudiId, lang),
                "REF10_" + codConvenio + ".pdf"));
    }

    @Tool(description = """
            Descarga el REF11 (Homologacion) en base64.
            language: SP (defecto), CA, EN, FR.
            estudiId se obtiene de DetalleConvenioDTO.
            """)
    public String descargarRef11(String codConvenio, String codTemporal,
                                  String estudiId, String language) throws Exception {
        if (codConvenio == null || codConvenio.isBlank()) {
            throw new IllegalArgumentException("descargarRef11: codConvenio es obligatorio");
        }
        if (codTemporal == null || codTemporal.isBlank()) {
            throw new IllegalArgumentException("descargarRef11: codTemporal es obligatorio");
        }
        if (estudiId == null || estudiId.isBlank()) {
            throw new IllegalArgumentException("descargarRef11: estudiId es obligatorio");
        }
        String lang = lang(language);
        return mapper.writeValueAsString(referenciaService.getDocumento(
                jsessionid(), qbidUrls.ref11(codConvenio, codTemporal, estudiId, lang),
                "REF11_" + codConvenio + ".pdf"));
    }

    @Tool(description = """
            Descarga el REF15 (Valoracion de evaluacion de la empresa) en base64.
            language: SP (defecto), CA, EN, FR.
            codVisita se obtiene del seguimiento FCT (valoracion.codVisitaRef18).
            """)
    public String descargarRef15(String codConvenio, String codTemporal,
                                  String codVisita, String language) throws Exception {
        if (codConvenio == null || codConvenio.isBlank()) {
            throw new IllegalArgumentException("descargarRef15: codConvenio es obligatorio");
        }
        if (codTemporal == null || codTemporal.isBlank()) {
            throw new IllegalArgumentException("descargarRef15: codTemporal es obligatorio");
        }
        if (codVisita == null || codVisita.isBlank()) {
            throw new IllegalArgumentException("descargarRef15: codVisita es obligatorio");
        }
        String lang = lang(language);
        return mapper.writeValueAsString(referenciaService.getDocumento(
                jsessionid(), qbidUrls.ref15(codConvenio, codTemporal, codVisita, lang),
                "REF15_" + codConvenio + ".pdf"));
    }

    @Tool(description = """
            Descarga el REF18 (Valoracion del expediente) en base64.
            language: SP (defecto), CA, EN, FR.
            codVisita se obtiene del seguimiento FCT (valoracion.codVisitaRef18).
            """)
    public String descargarRef18(String codConvenio, String codTemporal,
                                  String codVisita, String language) throws Exception {
        if (codConvenio == null || codConvenio.isBlank()) {
            throw new IllegalArgumentException("descargarRef18: codConvenio es obligatorio");
        }
        if (codTemporal == null || codTemporal.isBlank()) {
            throw new IllegalArgumentException("descargarRef18: codTemporal es obligatorio");
        }
        if (codVisita == null || codVisita.isBlank()) {
            throw new IllegalArgumentException("descargarRef18: codVisita es obligatorio");
        }
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
        if (codConvenio == null || codConvenio.isBlank()) {
            throw new IllegalArgumentException("descargarRef05: codConvenio es obligatorio");
        }
        if (codTemporal == null || codTemporal.isBlank()) {
            throw new IllegalArgumentException("descargarRef05: codTemporal es obligatorio");
        }
        if (hash == null || hash.isBlank()) {
            throw new IllegalArgumentException("descargarRef05: hash es obligatorio");
        }
        if (cursSeleccio == null || cursSeleccio.isBlank()) {
            throw new IllegalArgumentException("descargarRef05: cursSeleccio es obligatorio");
        }
        String lang = lang(language);
        return mapper.writeValueAsString(referenciaService.getDocumento(
                jsessionid(), qbidUrls.ref05(codConvenio, codTemporal, hash, cursSeleccio, lang),
                "REF05_" + codConvenio + ".pdf"));
    }

    @Tool(description = """
            Descarga el REF05-Baja (Documento de finalizacion anticipada del acuerdo) en base64.
            language: SP (defecto), CA, EN, FR.
            hash y cursSeleccio se obtienen de DetalleConvenioDTO (hashCodeRef05Baja y cursSeleccio).
            """)
    public String descargarRef05Baja(String codConvenio, String codTemporal,
                                      String hash, String cursSeleccio, String language) throws Exception {
        if (codConvenio == null || codConvenio.isBlank()) {
            throw new IllegalArgumentException("descargarRef05Baja: codConvenio es obligatorio");
        }
        if (codTemporal == null || codTemporal.isBlank()) {
            throw new IllegalArgumentException("descargarRef05Baja: codTemporal es obligatorio");
        }
        if (hash == null || hash.isBlank()) {
            throw new IllegalArgumentException("descargarRef05Baja: hash es obligatorio");
        }
        if (cursSeleccio == null || cursSeleccio.isBlank()) {
            throw new IllegalArgumentException("descargarRef05Baja: cursSeleccio es obligatorio");
        }
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
        if (codConvenio == null || codConvenio.isBlank()) {
            throw new IllegalArgumentException("descargarRef06: codConvenio es obligatorio");
        }
        if (codTemporal == null || codTemporal.isBlank()) {
            throw new IllegalArgumentException("descargarRef06: codTemporal es obligatorio");
        }
        if (estudiId == null || estudiId.isBlank()) {
            throw new IllegalArgumentException("descargarRef06: estudiId es obligatorio");
        }
        String lang = lang(language);
        return mapper.writeValueAsString(referenciaService.getDocumento(
                jsessionid(), qbidUrls.ref06(codConvenio, codTemporal, estudiId, lang),
                "REF06_" + codConvenio + ".pdf"));
    }

    // Utilidades

    private String lang(String language) {
        return (language != null && !language.isBlank()) ? language : "SP";
    }
}




