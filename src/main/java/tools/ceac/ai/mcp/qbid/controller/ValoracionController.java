package tools.ceac.ai.mcp.qbid.controller;

import tools.ceac.ai.mcp.qbid.exception.SessionExpiredException;
import tools.ceac.ai.mcp.qbid.model.dto.ValoracionDTO;
import tools.ceac.ai.mcp.qbid.service.SesionCache;
import tools.ceac.ai.mcp.qbid.service.ValoracionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/valoracion")
public class ValoracionController {

    private final ValoracionService service;
    private final SesionCache sesionCache;

    public ValoracionController(ValoracionService service, SesionCache sesionCache) {
        this.service    = service;
        this.sesionCache = sesionCache;
    }

    private String session(String auth) throws Exception {
        try {
            return sesionCache.resolveSession(auth);
        } catch (SessionExpiredException e) {
            return sesionCache.renewSession(auth);
        }
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
