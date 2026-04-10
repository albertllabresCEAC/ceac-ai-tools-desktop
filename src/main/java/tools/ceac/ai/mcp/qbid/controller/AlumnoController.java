package tools.ceac.ai.mcp.qbid.controller;

import tools.ceac.ai.mcp.qbid.exception.SessionExpiredException;
import tools.ceac.ai.mcp.qbid.model.dto.FichaAlumnoDTO;
import tools.ceac.ai.mcp.qbid.service.AlumnoService;
import tools.ceac.ai.mcp.qbid.service.SesionCache;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/alumnos")
public class AlumnoController {

    private final AlumnoService service;
    private final SesionCache sesionCache;

    public AlumnoController(AlumnoService service, SesionCache sesionCache) {
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

    /** Ver ficha del alumno/a — datos personales, académicos y de contacto */
    @GetMapping("/{codAlumno}")
    public ResponseEntity<FichaAlumnoDTO> getFicha(
            @RequestHeader("Authorization") String auth,
            @PathVariable String codAlumno) throws Exception {
        return ResponseEntity.ok(service.getFicha(session(auth), codAlumno));
    }
}
