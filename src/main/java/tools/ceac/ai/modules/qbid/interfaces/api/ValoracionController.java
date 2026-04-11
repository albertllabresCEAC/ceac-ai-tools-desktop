package tools.ceac.ai.modules.qbid.interfaces.api;

import tools.ceac.ai.modules.qbid.domain.model.ValoracionDTO;
import tools.ceac.ai.modules.qbid.application.service.ValoracionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/valoracion")
public class ValoracionController {

    private final ValoracionService service;
    private final QbidApiSessionProvider sessionProvider;

    public ValoracionController(ValoracionService service, QbidApiSessionProvider sessionProvider) {
        this.service = service;
        this.sessionProvider = sessionProvider;
    }

    private String session(String auth) throws Exception {
        return sessionProvider.currentSession();
    }

    /**
     * Ver valoraciÃ³n final emitida por la empresa.
     *
     * Los parÃ¡metros codVisitaValoracion y codVisita se obtienen del
     * SeguimientoDTO.Valoracion (codVisitaValoracion y codVisitaRef18).
     */
    @GetMapping("/{codConvenio}/{codTemporal}")
    public ResponseEntity<ValoracionDTO> getValoracion(
            @RequestHeader("Authorization") String auth,
            @PathVariable String codConvenio,
            @PathVariable String codTemporal,
            @RequestParam String codVisitaValoracion,
            @RequestParam String codVisita) throws Exception {
        return ResponseEntity.ok(service.getValoracion(
                session(auth), codConvenio, codTemporal, codVisitaValoracion, codVisita));
    }
}



