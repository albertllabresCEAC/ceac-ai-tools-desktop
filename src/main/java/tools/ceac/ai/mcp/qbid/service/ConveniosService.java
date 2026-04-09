package tools.ceac.ai.mcp.qbid.service;

import tools.ceac.ai.mcp.qbid.model.dto.*;
import tools.ceac.ai.mcp.qbid.parser.*;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ConveniosService {

    private final QbidHttpService http;
    private final QbidUrls urls;
    private final ConveniosParser parser;
    private final DetalleConvenioParser detalleParser;
    private final CuadernoParser cuadernoParser;
    private final SeguimientoParser seguimientoParser;
    private final AgendaParser agendaParser;
    private final InformePeriodicoParser informeParser;

    public ConveniosService(QbidHttpService http,
                            QbidUrls urls,
                            ConveniosParser parser,
                            DetalleConvenioParser detalleParser,
                            CuadernoParser cuadernoParser,
                            SeguimientoParser seguimientoParser,
                            AgendaParser agendaParser,
                            InformePeriodicoParser informeParser) {
        this.http              = http;
        this.urls              = urls;
        this.parser            = parser;
        this.detalleParser     = detalleParser;
        this.cuadernoParser    = cuadernoParser;
        this.seguimientoParser = seguimientoParser;
        this.agendaParser      = agendaParser;
        this.informeParser     = informeParser;
    }

    public List<ConvenioDTO> getListado(String jsessionid, String plan) throws Exception {
        String cambioUrl = "MODGEN".equalsIgnoreCase(plan)
                ? urls.cambiarSistemaModGen()
                : urls.cambiarSistemaBid();
        http.get(cambioUrl, jsessionid);

        String listadoUrl = "MODGEN".equalsIgnoreCase(plan)
                ? urls.listadoModGen()
                : urls.listadoBid();
        String html = http.get(listadoUrl, jsessionid);

        List<ConvenioDTO> result = parser.parseListado(html, plan.toUpperCase());
        if (result.isEmpty()) {
            Thread.sleep(1500);
            html = http.get(listadoUrl, jsessionid);
            result = parser.parseListado(html, plan.toUpperCase());
        }
        return result;
    }

    /**
     * Listado enriquecido: igual que getListado pero aÃ±ade codAlumno
     * navegando al detalle de cada convenio.
     * MÃ¡s lento â€” una request extra por alumno.
     */
    public List<ConvenioDTO> getListadoEnriquecido(String jsessionid, String plan) throws Exception {
        List<ConvenioDTO> listado = getListado(jsessionid, plan);

        for (int i = 0; i < listado.size(); i++) {
            ConvenioDTO c = listado.get(i);
            if (c.getCodConvenio().isBlank()) continue;

            try {
                String html       = http.get(urls.detalleConvenio(c.getCodConvenio(), c.getCodTemporal(), c.getNewSystem()), jsessionid);
                DetalleConvenioDTO detalle = detalleParser.parseDetalle(html);

                // Sustituir el DTO con codAlumno relleno
                listado.set(i, ConvenioDTO.builder()
                        .alumno(c.getAlumno())
                        .codAlumno(detalle.getCodAlumno())   // â† enriquecido
                        .estudio(c.getEstudio())
                        .estado(c.getEstado())
                        .codConvenio(c.getCodConvenio())
                        .codTemporal(c.getCodTemporal())
                        .codCuaderno(c.getCodCuaderno())
                        .newSystem(c.getNewSystem())
                        .plan(c.getPlan())
                        .build());
            } catch (Exception e) {
                // Si falla el detalle de un alumno, continuamos con los demÃ¡s
            }
        }

        return listado;
    }

    /**
     * Detalle estructurado de un convenio â€” devuelve DTO parseado.
     */
    public DetalleConvenioDTO getDetalle(String jsessionid,
                                         String codConvenio,
                                         String codTemporal,
                                         String newSystem) throws Exception {
        String html = http.get(urls.detalleConvenio(codConvenio, codTemporal, newSystem), jsessionid);
        return detalleParser.parseDetalle(html);
    }

    /**
     * Detalle HTML crudo â€” Ãºtil para depuraciÃ³n.
     */
    public String getDetalleHtml(String jsessionid,
                                 String codConvenio,
                                 String codTemporal,
                                 String newSystem) throws Exception {
        return http.get(urls.detalleConvenio(codConvenio, codTemporal, newSystem), jsessionid);
    }

    public CuadernoDTO getCuaderno(String jsessionid, String codCuaderno) throws Exception {
        String html = http.get(urls.cuaderno(codCuaderno), jsessionid);
        return cuadernoParser.parseCuaderno(html);
    }

    public String getCuadernoHtml(String jsessionid, String codCuaderno) throws Exception {
        return http.get(urls.cuaderno(codCuaderno), jsessionid);
    }

    public SeguimientoDTO getSeguimiento(String jsessionid,
                                         String codConvenio,
                                         String codTemporal,
                                         String newSystem) throws Exception {
        String html = http.get(urls.seguimiento(codConvenio, codTemporal, newSystem), jsessionid);
        return seguimientoParser.parseSeguimiento(html);
    }

    public String getSeguimientoHtml(String jsessionid,
                                     String codConvenio,
                                     String codTemporal,
                                     String newSystem) throws Exception {
        return http.get(urls.seguimiento(codConvenio, codTemporal, newSystem), jsessionid);
    }

    public AgendaDTO getAgenda(String jsessionid, String codConvenio, String codTemporal) throws Exception {
        return agendaParser.parseAgenda(http.get(urls.agendaAlumno(codConvenio, codTemporal), jsessionid));
    }

    public String getAgendaHtml(String jsessionid, String codConvenio, String codTemporal) throws Exception {
        return http.get(urls.agendaAlumno(codConvenio, codTemporal), jsessionid);
    }

    public List<AgendaDTO.InformePeriodico> getInformes(String jsessionid,
                                                        String codConvenio,
                                                        String codTemporal) throws Exception {
        String html = http.get(urls.agendaAlumno(codConvenio, codTemporal), jsessionid);
        return agendaParser.parseAgenda(html).getInformes();
    }

    public DetalleInformeDTO getDetalleInforme(String jsessionid,
                                               String codConvenio,
                                               String codTemporal,
                                               String conveniValoracioPk,
                                               String hashCode) throws Exception {
        String html = http.get(
                urls.informePeriodico(codConvenio, codTemporal, conveniValoracioPk, hashCode),
                jsessionid);
        return informeParser.parse(html);
    }

    public DetalleInformeDTO guardarInforme(String jsessionid,
                                            GuardarInformeRequest req) throws Exception {
        return postInforme(jsessionid, req, "save");
    }

    public DetalleInformeDTO firmarInforme(String jsessionid,
                                           GuardarInformeRequest req) throws Exception {
        return postInforme(jsessionid, req, "signar");
    }

    /**
     * EnvÃ­a el formulario de informe periÃ³dico a qBID.
     * {@code moduleaction} determina si es un guardado ("save") o una firma definitiva ("signar").
     */
    private DetalleInformeDTO postInforme(String jsessionid,
                                          GuardarInformeRequest req,
                                          String moduleaction) throws Exception {
        // GET previo para obtener curs_seleccio del formulario actual
        DetalleInformeDTO actual = getDetalleInforme(
                jsessionid, req.getCodConvenio(), req.getCodTemporal(),
                req.getConveniValoracioPk(), req.getHashCode());

        // ConstrucciÃ³n del cuerpo del POST (orden relevante para qBID)
        Map<String, String> params = new LinkedHashMap<>();
        params.put("moduleaction",              moduleaction);
        params.put("conveni_valoracio_pk",      req.getConveniValoracioPk());
        params.put("codi_conveni",              req.getCodConvenio());
        params.put("codi_conveni_provisional",  req.getCodTemporal());
        params.put("newSystem",                 "");
        params.put("hash_code",                 req.getHashCode());
        params.put("quadernId",                 "");
        params.put("query_page",                "");
        params.put("autoritza_page",            "");
        params.put("curs_seleccio",             actual.getCursSeleccio());
        params.put("language",                  "SP");
        params.put("config_valoraciones_negativas", "null");

        // Valoraciones por actividad: inp_XXXXX (adecuaciÃ³n tutor) + inp_extra_XXXXX (valoraciÃ³n empresa)
        for (GuardarInformeRequest.ValoracionItem item : req.getValoraciones()) {
            params.put("inp_"       + item.getActividadId(),
                    GuardarInformeRequest.AdecuacionTutor.fromNombre(item.getAdecuacionTutor()).getCodigo());
            params.put("inp_extra_" + item.getActividadId(),
                    GuardarInformeRequest.ValoracionEmpresa.fromNombre(item.getValoracionEmpresa()).getCodigo());
        }

        params.put("obs_alumne",  orEmpty(req.getObservacionesAlumno()));
        params.put("obs_empresa", orEmpty(req.getObservacionesEmpresa()));
        params.put("obs_tutor",   orEmpty(req.getObservacionesTutor()));
        params.put("signatari",   orEmpty(req.getSignatarioEmpresa()));

        String html = http.post(urls.guardarInformePeriodico(), params, jsessionid);
        return informeParser.parse(html);
    }

    private String orEmpty(String s) {
        return s != null ? s : "";
    }

    public String getDetalleInformeHtml(String jsessionid,
                                        String codConvenio,
                                        String codTemporal,
                                        String conveniValoracioPk,
                                        String hashCode) throws Exception {
        return http.get(
                urls.informePeriodico(codConvenio, codTemporal, conveniValoracioPk, hashCode),
                jsessionid);
    }
}

