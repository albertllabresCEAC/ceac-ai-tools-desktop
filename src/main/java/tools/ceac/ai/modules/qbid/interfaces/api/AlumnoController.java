package tools.ceac.ai.modules.qbid.interfaces.api;

import tools.ceac.ai.modules.qbid.domain.model.FichaAlumnoDTO;
import tools.ceac.ai.modules.qbid.application.service.AlumnoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/alumnos")
public class AlumnoController {

    private final AlumnoService service;
    private final QbidApiSessionProvider sessionProvider;

    public AlumnoController(AlumnoService service, QbidApiSessionProvider sessionProvider) {
        this.service = service;
        this.sessionProvider = sessionProvider;
    }

    private String session(String auth) throws Exception {
        return sessionProvider.currentSession();
    }

    /** Ver ficha del alumno/a â€” datos personales, acadÃ©micos y de contacto */
    @GetMapping("/{codAlumno}")
    public ResponseEntity<FichaAlumnoDTO> getFicha(
            @RequestHeader("Authorization") String auth,
            @PathVariable String codAlumno) throws Exception {
        return ResponseEntity.ok(service.getFicha(session(auth), codAlumno));
    }
}



