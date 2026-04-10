package tools.ceac.ai.modules.qbid.application.service;

import tools.ceac.ai.modules.qbid.application.port.out.QbidEndpointFactory;
import tools.ceac.ai.modules.qbid.application.port.out.QbidTransport;
import tools.ceac.ai.modules.qbid.domain.model.ValoracionDTO;
import tools.ceac.ai.modules.qbid.infrastructure.parser.ValoracionParser;
import org.springframework.stereotype.Service;

@Service
public class ValoracionService {

    private final QbidTransport http;
    private final QbidEndpointFactory urls;
    private final ValoracionParser parser;

    public ValoracionService(QbidTransport http, QbidEndpointFactory urls, ValoracionParser parser) {
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


