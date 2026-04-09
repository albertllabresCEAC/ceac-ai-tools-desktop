package tools.ceac.ai.mcp.qbid.service;

import tools.ceac.ai.mcp.qbid.model.dto.CentroTrabajoDTO;
import tools.ceac.ai.mcp.qbid.model.dto.EmpresaDTO;
import tools.ceac.ai.mcp.qbid.parser.CentroTrabajoParser;
import tools.ceac.ai.mcp.qbid.parser.EmpresaParser;
import org.springframework.stereotype.Service;

@Service
public class EmpresaService {

    private final QbidHttpService http;
    private final QbidUrls urls;
    private final EmpresaParser parser;
    private final CentroTrabajoParser ctParser;

    public EmpresaService(QbidHttpService http, QbidUrls urls,
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
