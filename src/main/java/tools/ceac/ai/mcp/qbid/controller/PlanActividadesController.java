package tools.ceac.ai.mcp.qbid.controller;

import tools.ceac.ai.mcp.qbid.exception.SessionExpiredException;
import tools.ceac.ai.mcp.qbid.model.dto.GuardarPlanRequest;
import tools.ceac.ai.mcp.qbid.model.dto.PlanActividadesDTO;
import tools.ceac.ai.mcp.qbid.service.PlanActividadesService;
import tools.ceac.ai.mcp.qbid.service.SesionCache;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/plan-actividades")
public class PlanActividadesController {

    private final PlanActividadesService service;
    private final SesionCache sesionCache;

    public PlanActividadesController(PlanActividadesService service, SesionCache sesionCache) {
        this.service     = service;
        this.sesionCache = sesionCache;
    }

    private String session(String auth) throws Exception {
        try {
            return sesionCache.resolveSession(auth);
        } catch (SessionExpiredException e) {
            return sesionCache.renewSession(auth);
        }
    }

    @PostMapping("/{codConvenio}/validar")
    public ResponseEntity<List<PlanActividadesDTO>> validarPlan(
            @RequestHeader("Authorization") String auth,
            @PathVariable String codConvenio,
            @RequestBody GuardarPlanRequest req) throws Exception {
        req.setCodConvenio(codConvenio);
        return ResponseEntity.ok(service.validarPlan(session(auth), req));
    }

    @PostMapping("/{codConvenio}/guardar")
    public ResponseEntity<List<PlanActividadesDTO>> guardarPlan(
            @RequestHeader("Authorization") String auth,
            @PathVariable String codConvenio,
            @RequestBody GuardarPlanRequest req) throws Exception {
        req.setCodConvenio(codConvenio);
        return ResponseEntity.ok(service.guardarPlan(session(auth), req));
    }

    @GetMapping("/{codConvenio}")
    public ResponseEntity<List<PlanActividadesDTO>> getPlan(
            @RequestHeader("Authorization") String auth,
            @PathVariable String codConvenio,
            @RequestParam String codTemporal,
            @RequestParam(defaultValue = "") String newSystem) throws Exception {
        return ResponseEntity.ok(
                service.getPlan(session(auth), codConvenio, codTemporal, newSystem)
        );
    }
}
