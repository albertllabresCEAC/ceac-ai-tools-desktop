package tools.ceac.ai.modules.qbid.interfaces.api;

import tools.ceac.ai.modules.qbid.application.port.out.QbidEndpointFactory;
import tools.ceac.ai.modules.qbid.domain.model.ReferenciaDTO;
import tools.ceac.ai.modules.qbid.application.service.ReferenciaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/referencias")
public class ReferenciaController {

    private final ReferenciaService service;
    private final QbidEndpointFactory urls;
    private final QbidApiSessionProvider sessionProvider;

    public ReferenciaController(ReferenciaService service,
                                QbidEndpointFactory urls,
                                QbidApiSessionProvider sessionProvider) {
        this.service = service;
        this.urls = urls;
        this.sessionProvider = sessionProvider;
    }

    private String session(String auth) throws Exception {
        return sessionProvider.currentSession();
    }

    // â”€â”€ Cuaderno â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    /** REF19 â€” ValoraciÃ³n global del cuaderno */
    @GetMapping("/REF19/{quadernId}")
    public ResponseEntity<ReferenciaDTO> ref19(
            @RequestHeader("Authorization") String auth,
            @PathVariable String quadernId,
            @RequestParam(defaultValue = "SP") String language) throws Exception {
        return ResponseEntity.ok(service.getDocumento(
                session(auth),
                urls.ref19(quadernId, language),
                "REF19_" + quadernId + ".pdf"));
    }

    /** REF20 â€” CalificaciÃ³n final FCT */
    @GetMapping("/REF20/{quadernId}")
    public ResponseEntity<ReferenciaDTO> ref20(
            @RequestHeader("Authorization") String auth,
            @PathVariable String quadernId,
            @RequestParam(defaultValue = "SP") String language) throws Exception {
        return ResponseEntity.ok(service.getDocumento(
                session(auth),
                urls.ref20(quadernId, language),
                "REF20_" + quadernId + ".pdf"));
    }

    /** REF22 â€” Expediente del cuaderno */
    @GetMapping("/REF22/{quadernId}")
    public ResponseEntity<ReferenciaDTO> ref22(
            @RequestHeader("Authorization") String auth,
            @PathVariable String quadernId,
            @RequestParam(defaultValue = "SP") String language) throws Exception {
        return ResponseEntity.ok(service.getDocumento(
                session(auth),
                urls.ref22(quadernId, language),
                "REF22_" + quadernId + ".pdf"));
    }

    // â”€â”€ Seguimiento â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    /** REF07 â€” Seguimiento de la FPCT */
    @GetMapping("/REF07/{conveniId}/{conveniProvId}")
    public ResponseEntity<ReferenciaDTO> ref07(
            @RequestHeader("Authorization") String auth,
            @PathVariable String conveniId,
            @PathVariable String conveniProvId,
            @RequestParam(defaultValue = "SP") String language) throws Exception {
        return ResponseEntity.ok(service.getDocumento(
                session(auth),
                urls.ref07(conveniId, conveniProvId, language),
                "REF07_" + conveniId + ".pdf"));
    }

    /** REF10 â€” Cuestionario al Centro de Trabajo */
    @GetMapping("/REF10/{conveniId}/{conveniProvId}")
    public ResponseEntity<ReferenciaDTO> ref10(
            @RequestHeader("Authorization") String auth,
            @PathVariable String conveniId,
            @PathVariable String conveniProvId,
            @RequestParam String estudiId,
            @RequestParam(defaultValue = "SP") String language) throws Exception {
        return ResponseEntity.ok(service.getDocumento(
                session(auth),
                urls.ref10(conveniId, conveniProvId, estudiId, language),
                "REF10_" + conveniId + ".pdf"));
    }

    /** REF11 â€” HomologaciÃ³n */
    @GetMapping("/REF11/{conveniId}/{conveniProvId}")
    public ResponseEntity<ReferenciaDTO> ref11(
            @RequestHeader("Authorization") String auth,
            @PathVariable String conveniId,
            @PathVariable String conveniProvId,
            @RequestParam String estudiId,
            @RequestParam(defaultValue = "SP") String language) throws Exception {
        return ResponseEntity.ok(service.getDocumento(
                session(auth),
                urls.ref11(conveniId, conveniProvId, estudiId, language),
                "REF11_" + conveniId + ".pdf"));
    }

    /** REF18 â€” ValoraciÃ³n expediente (requiere cod_visita del SeguimientoDTO.Valoracion) */
    @GetMapping("/REF18/{conveniId}/{conveniProvId}")
    public ResponseEntity<ReferenciaDTO> ref18(
            @RequestHeader("Authorization") String auth,
            @PathVariable String conveniId,
            @PathVariable String conveniProvId,
            @RequestParam String codVisita,
            @RequestParam(defaultValue = "SP") String language) throws Exception {
        return ResponseEntity.ok(service.getDocumento(
                session(auth),
                urls.ref18(conveniId, conveniProvId, codVisita, language),
                "REF18_" + conveniId + ".pdf"));
    }

    // â”€â”€ Convenio â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    /** REF15 â€” ValoraciÃ³n de evaluaciÃ³n (de la empresa) */
    @GetMapping("/REF15/{conveniId}/{conveniProvId}")
    public ResponseEntity<ReferenciaDTO> ref15(
            @RequestHeader("Authorization") String auth,
            @PathVariable String conveniId,
            @PathVariable String conveniProvId,
            @RequestParam String codVisita,
            @RequestParam(defaultValue = "SP") String language) throws Exception {
        return ResponseEntity.ok(service.getDocumento(
                session(auth),
                urls.ref15(conveniId, conveniProvId, codVisita, language),
                "REF15_" + conveniId + ".pdf"));
    }

    /** REF05 â€” Documento del acuerdo */
    @GetMapping("/REF05/{conveniId}/{conveniProvId}")
    public ResponseEntity<ReferenciaDTO> ref05(
            @RequestHeader("Authorization") String auth,
            @PathVariable String conveniId,
            @PathVariable String conveniProvId,
            @RequestParam String hash,
            @RequestParam String cursSeleccio,
            @RequestParam(defaultValue = "SP") String language) throws Exception {
        return ResponseEntity.ok(service.getDocumento(
                session(auth),
                urls.ref05(conveniId, conveniProvId, hash, cursSeleccio, language),
                "REF05_" + conveniId + ".pdf"));
    }

    /** REF05_Baja â€” Documento de finalizaciÃ³n anticipada del acuerdo */
    @GetMapping("/REF05-baja/{conveniId}/{conveniProvId}")
    public ResponseEntity<ReferenciaDTO> ref05Baja(
            @RequestHeader("Authorization") String auth,
            @PathVariable String conveniId,
            @PathVariable String conveniProvId,
            @RequestParam String hash,
            @RequestParam String cursSeleccio,
            @RequestParam(defaultValue = "SP") String language) throws Exception {
        return ResponseEntity.ok(service.getDocumento(
                session(auth),
                urls.ref05Baja(conveniId, conveniProvId, hash, cursSeleccio, language),
                "REF05_Baja_" + conveniId + ".pdf"));
    }

    /** REF06 â€” Plan de actividades del acuerdo */
    @GetMapping("/REF06/{conveniId}/{conveniProvId}")
    public ResponseEntity<ReferenciaDTO> ref06(
            @RequestHeader("Authorization") String auth,
            @PathVariable String conveniId,
            @PathVariable String conveniProvId,
            @RequestParam String estudiId,
            @RequestParam(defaultValue = "SP") String language) throws Exception {
        return ResponseEntity.ok(service.getDocumento(
                session(auth),
                urls.ref06(conveniId, conveniProvId, estudiId, language),
                "REF06_" + conveniId + ".pdf"));
    }
}



