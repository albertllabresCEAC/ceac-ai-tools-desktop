package tools.ceac.ai.modules.qbid.application.service;

import tools.ceac.ai.modules.qbid.application.port.out.QbidEndpointFactory;
import tools.ceac.ai.modules.qbid.application.port.out.QbidTransport;
import tools.ceac.ai.modules.qbid.domain.model.CentroTrabajoDTO;
import tools.ceac.ai.modules.qbid.domain.model.EmpresaDTO;
import tools.ceac.ai.modules.qbid.infrastructure.parser.CentroTrabajoParser;
import tools.ceac.ai.modules.qbid.infrastructure.parser.EmpresaParser;
import org.springframework.stereotype.Service;

@Service
public class EmpresaService {

    private final QbidTransport http;
    private final QbidEndpointFactory urls;
    private final EmpresaParser parser;
    private final CentroTrabajoParser ctParser;

    public EmpresaService(QbidTransport http, QbidEndpointFactory urls,
                          EmpresaParser parser, CentroTrabajoParser ctParser) {
        this.http     = http;
        this.urls     = urls;
        this.parser   = parser;
        this.ctParser = ctParser;
    }

    public EmpresaDTO getEmpresa(String jsessionid, String codEmpresa) throws Exception {
        String html = http.get(urls.empresa(codEmpresa), jsessionid);
        return parser.parseEmpresa(html);
    }

    public CentroTrabajoDTO getCentroTrabajo(String jsessionid,
                                             String codEmpresa,
                                             String codCentro) throws Exception {
        String html = http.get(urls.centroTrabajo(codEmpresa, codCentro), jsessionid);
        return ctParser.parseCentroTrabajo(html);
    }
}


