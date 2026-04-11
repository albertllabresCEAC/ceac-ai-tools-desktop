package tools.ceac.ai.modules.qbid.interfaces.api;

import tools.ceac.ai.modules.qbid.domain.model.CentroTrabajoDTO;
import tools.ceac.ai.modules.qbid.domain.model.EmpresaDTO;
import tools.ceac.ai.modules.qbid.application.service.EmpresaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/empresas")
public class EmpresaController {

    private final EmpresaService service;
    private final QbidApiSessionProvider sessionProvider;

    public EmpresaController(EmpresaService service, QbidApiSessionProvider sessionProvider) {
        this.service = service;
        this.sessionProvider = sessionProvider;
    }

    private String session(String auth) throws Exception {
        return sessionProvider.currentSession();
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



