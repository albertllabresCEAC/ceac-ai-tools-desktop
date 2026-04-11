package tools.ceac.ai.modules.qbid.interfaces.api;

import tools.ceac.ai.modules.qbid.domain.model.GuardarPlanRequest;
import tools.ceac.ai.modules.qbid.domain.model.PlanActividadesDTO;
import tools.ceac.ai.modules.qbid.application.service.PlanActividadesService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/plan-actividades")
public class PlanActividadesController {

    private final PlanActividadesService service;
    private final QbidApiSessionProvider sessionProvider;

    public PlanActividadesController(PlanActividadesService service, QbidApiSessionProvider sessionProvider) {
        this.service = service;
        this.sessionProvider = sessionProvider;
    }

    private String session(String auth) throws Exception {
        return sessionProvider.currentSession();
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



