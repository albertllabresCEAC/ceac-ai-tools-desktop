package tools.ceac.ai.mcp.qbid.controller;

import tools.ceac.ai.mcp.qbid.exception.SessionExpiredException;
import tools.ceac.ai.mcp.qbid.model.dto.*;
import tools.ceac.ai.mcp.qbid.service.ConveniosService;
import tools.ceac.ai.mcp.qbid.service.SesionCache;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/convenios")
public class ConveniosController {

    private final ConveniosService service;
    private final SesionCache sesionCache;

    public ConveniosController(ConveniosService service, SesionCache sesionCache) {
        this.service     = service;
        this.sesionCache = sesionCache;
    }

    // â”€â”€ Helper: obtiene sesiÃ³n con auto-renovaciÃ³n si expira â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    private String session(String auth) throws Exception {
        try {
            return sesionCache.resolveSession(auth);
        } catch (SessionExpiredException e) {
            return sesionCache.renewSession(auth);
        }
    }

    @GetMapping
    public ResponseEntity<List<ConvenioDTO>> getListado(
            @RequestHeader("Authorization") String auth,
            @RequestParam(defaultValue = "MODGEN") String plan) throws Exception {
        return ResponseEntity.ok(service.getListado(session(auth), plan));
    }

    @GetMapping("/enriquecido")
    public ResponseEntity<List<ConvenioDTO>> getListadoEnriquecido(
            @RequestHeader("Authorization") String auth,
            @RequestParam(defaultValue = "MODGEN") String plan) throws Exception {
        return ResponseEntity.ok(service.getListadoEnriquecido(session(auth), plan));
    }

    @GetMapping("/{codConvenio}/detalle")
    public ResponseEntity<DetalleConvenioDTO> getDetalle(
            @RequestHeader("Authorization") String auth,
            @PathVariable String codConvenio,
            @RequestParam String codTemporal,
            @RequestParam String newSystem) throws Exception {
        return ResponseEntity.ok(service.getDetalle(session(auth), codConvenio, codTemporal, newSystem));
    }

    @GetMapping("/{codConvenio}/detalle/html")
    public ResponseEntity<String> getDetalleHtml(
            @RequestHeader("Authorization") String auth,
            @PathVariable String codConvenio,
            @RequestParam String codTemporal,
            @RequestParam String newSystem) throws Exception {
        return ResponseEntity.ok().header("Content-Type", "text/html; charset=UTF-8")
                .body(service.getDetalleHtml(session(auth), codConvenio, codTemporal, newSystem));
    }

    @GetMapping("/cuaderno/{codCuaderno}")
    public ResponseEntity<CuadernoDTO> getCuaderno(
            @RequestHeader("Authorization") String auth,
            @PathVariable String codCuaderno) throws Exception {
        return ResponseEntity.ok(service.getCuaderno(session(auth), codCuaderno));
    }

    @GetMapping("/cuaderno/{codCuaderno}/html")
    public ResponseEntity<String> getCuadernoHtml(
            @RequestHeader("Authorization") String auth,
            @PathVariable String codCuaderno) throws Exception {
        return ResponseEntity.ok().header("Content-Type", "text/html; charset=UTF-8")
                .body(service.getCuadernoHtml(session(auth), codCuaderno));
    }

    @GetMapping("/{codConvenio}/seguimiento")
    public ResponseEntity<SeguimientoDTO> getSeguimiento(
            @RequestHeader("Authorization") String auth,
            @PathVariable String codConvenio,
            @RequestParam String codTemporal,
            @RequestParam String newSystem) throws Exception {
        return ResponseEntity.ok(service.getSeguimiento(session(auth), codConvenio, codTemporal, newSystem));
    }

    @GetMapping("/{codConvenio}/seguimiento/html")
    public ResponseEntity<String> getSeguimientoHtml(
            @RequestHeader("Authorization") String auth,
            @PathVariable String codConvenio,
            @RequestParam String codTemporal,
            @RequestParam String newSystem) throws Exception {
        return ResponseEntity.ok().header("Content-Type", "text/html; charset=UTF-8")
                .body(service.getSeguimientoHtml(session(auth), codConvenio, codTemporal, newSystem));
    }

    @GetMapping("/{codConvenio}/agenda")
    public ResponseEntity<AgendaDTO> getAgenda(
            @RequestHeader("Authorization") String auth,
            @PathVariable String codConvenio,
            @RequestParam String codTemporal) throws Exception {
        return ResponseEntity.ok(service.getAgenda(session(auth), codConvenio, codTemporal));
    }

    @GetMapping("/{codConvenio}/agenda/html")
    public ResponseEntity<String> getAgendaHtml(
            @RequestHeader("Authorization") String auth,
            @PathVariable String codConvenio,
            @RequestParam String codTemporal) throws Exception {
        return ResponseEntity.ok().header("Content-Type", "text/html; charset=UTF-8")
                .body(service.getAgendaHtml(session(auth), codConvenio, codTemporal));
    }

    @GetMapping("/{codConvenio}/informes")
    public ResponseEntity<List<AgendaDTO.InformePeriodico>> getInformes(
            @RequestHeader("Authorization") String auth,
            @PathVariable String codConvenio,
            @RequestParam String codTemporal) throws Exception {
        return ResponseEntity.ok(service.getInformes(session(auth), codConvenio, codTemporal));
    }

    @GetMapping("/{codConvenio}/informes/{conveniValoracioPk}")
    public ResponseEntity<DetalleInformeDTO> getDetalleInforme(
            @RequestHeader("Authorization") String auth,
            @PathVariable String codConvenio,
            @PathVariable String conveniValoracioPk,
            @RequestParam String codTemporal,
            @RequestParam String hashCode) throws Exception {
        return ResponseEntity.ok(
                service.getDetalleInforme(session(auth), codConvenio, codTemporal, conveniValoracioPk, hashCode));
    }

    @PostMapping("/{codConvenio}/informes/{conveniValoracioPk}/guardar")
    public ResponseEntity<DetalleInformeDTO> guardarInforme(
            @RequestHeader("Authorization") String auth,
            @PathVariable String codConvenio,
            @PathVariable String conveniValoracioPk,
            @RequestBody GuardarInformeRequest req) throws Exception {
        req.setCodConvenio(codConvenio);
        req.setConveniValoracioPk(conveniValoracioPk);
        return ResponseEntity.ok(service.guardarInforme(session(auth), req));
    }

    @PostMapping("/{codConvenio}/informes/{conveniValoracioPk}/firmar")
    public ResponseEntity<DetalleInformeDTO> firmarInforme(
            @RequestHeader("Authorization") String auth,
            @PathVariable String codConvenio,
            @PathVariable String conveniValoracioPk,
            @RequestBody GuardarInformeRequest req) throws Exception {
        req.setCodConvenio(codConvenio);
        req.setConveniValoracioPk(conveniValoracioPk);
        return ResponseEntity.ok(service.firmarInforme(session(auth), req));
    }

    @GetMapping("/{codConvenio}/informes/{conveniValoracioPk}/html")
    public ResponseEntity<String> getDetalleInformeHtml(
            @RequestHeader("Authorization") String auth,
            @PathVariable String codConvenio,
            @PathVariable String conveniValoracioPk,
            @RequestParam String codTemporal,
            @RequestParam String hashCode) throws Exception {
        return ResponseEntity.ok().header("Content-Type", "text/html; charset=UTF-8")
                .body(service.getDetalleInformeHtml(session(auth), codConvenio, codTemporal, conveniValoracioPk, hashCode));
    }
}

