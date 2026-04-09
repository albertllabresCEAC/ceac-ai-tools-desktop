package tools.ceac.ai.mcp.qbid.service;

import tools.ceac.ai.mcp.qbid.model.dto.ValoracionDTO;
import tools.ceac.ai.mcp.qbid.parser.ValoracionParser;
import org.springframework.stereotype.Service;

@Service
public class ValoracionService {

    private final QbidHttpService http;
    private final QbidUrls urls;
    private final ValoracionParser parser;

    public ValoracionService(QbidHttpService http, QbidUrls urls, ValoracionParser parser) {
        this.http   = http;
        this.urls   = urls;
        this.parser = parser;
    }

    public ValoracionDTO getValoracion(String jsessionid,
                                       String codConvenio,
                                       String codTemporal,
                                       String codVisitaValoracion,
                                       String codVisita) throws Exception {
        String html = http.get(
                urls.valoracion(codConvenio, codTemporal, codVisitaValoracion, codVisita),
                jsessionid);
        return parser.parseValoracion(html);
    }

    public String getValoracionHtml(String jsessionid,
                                    String codConvenio,
                                    String codTemporal,
                                    String codVisitaValoracion,
                                    String codVisita) throws Exception {
        return http.get(
                urls.valoracion(codConvenio, codTemporal, codVisitaValoracion, codVisita),
                jsessionid);
    }
}
