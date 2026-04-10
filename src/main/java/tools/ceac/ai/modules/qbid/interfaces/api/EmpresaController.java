package tools.ceac.ai.modules.qbid.interfaces.api;

import tools.ceac.ai.modules.qbid.domain.exception.SessionExpiredException;
import tools.ceac.ai.modules.qbid.domain.model.CentroTrabajoDTO;
import tools.ceac.ai.modules.qbid.domain.model.EmpresaDTO;
import tools.ceac.ai.modules.qbid.application.service.EmpresaService;
import tools.ceac.ai.modules.qbid.application.service.SesionCache;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/empresas")
public class EmpresaController {

    private final EmpresaService service;
    private final SesionCache sesionCache;

    public EmpresaController(EmpresaService service, SesionCache sesionCache) {
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

    /** Ver datos de la empresa y sus centros de trabajo */
    @GetMapping("/{codEmpresa}")
    public ResponseEntity<EmpresaDTO> getEmpresa(
            @RequestHeader("Authorization") String auth,
            @PathVariable String codEmpresa) throws Exception {
        return ResponseEntity.ok(service.getEmpresa(session(auth), codEmpresa));
    }

    /** Ver ficha de un centro de trabajo concreto */
    @GetMapping("/{codEmpresa}/centros/{codCentro}")
    public ResponseEntity<CentroTrabajoDTO> getCentroTrabajo(
            @RequestHeader("Authorization") String auth,
            @PathVariable String codEmpresa,
            @PathVariable String codCentro) throws Exception {
        return ResponseEntity.ok(service.getCentroTrabajo(session(auth), codEmpresa, codCentro));
    }
}



