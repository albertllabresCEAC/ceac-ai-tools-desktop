package tools.ceac.ai.modules.qbid.interfaces.api;

import tools.ceac.ai.modules.qbid.domain.model.ActividadDTO;
import tools.ceac.ai.modules.qbid.application.service.ActividadService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/actividad")
public class ActividadController {

    private final ActividadService service;
    private final QbidApiSessionProvider sessionProvider;

    public ActividadController(ActividadService service, QbidApiSessionProvider sessionProvider) {
        this.service = service;
        this.sessionProvider = sessionProvider;
    }

    private String session(String auth) throws Exception {
        return sessionProvider.currentSession();
    }

    @GetMapping("/{codAlumno}/{fecha}")
    public ResponseEntity<ActividadDTO> getActividad(
            @RequestHeader("Authorization") String auth,
            @PathVariable String codAlumno,
            @PathVariable String fecha,
            @RequestParam String codConvenio,
            @RequestParam String codTemporal) throws Exception {
        return ResponseEntity.ok(
                service.getActividad(session(auth), codAlumno, codConvenio, codTemporal, fecha)
        );
    }

    @GetMapping("/{codAlumno}/{fecha}/html")
    public ResponseEntity<String> getActividadHtml(
            @RequestHeader("Authorization") String auth,
            @PathVariable String codAlumno,
            @PathVariable String fecha,
            @RequestParam String codConvenio,
            @RequestParam String codTemporal) throws Exception {
        return ResponseEntity.ok()
                .header("Content-Type", "text/html; charset=UTF-8")
                .body(service.getActividadHtml(session(auth), codAlumno, codConvenio, codTemporal, fecha));
    }
}




