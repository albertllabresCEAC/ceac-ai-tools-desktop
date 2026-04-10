package tools.ceac.ai.modules.qbid.application.service;

import tools.ceac.ai.modules.qbid.application.port.out.QbidEndpointFactory;
import tools.ceac.ai.modules.qbid.application.port.out.QbidTransport;
import tools.ceac.ai.modules.qbid.domain.model.FichaAlumnoDTO;
import tools.ceac.ai.modules.qbid.infrastructure.parser.FichaAlumnoParser;
import org.springframework.stereotype.Service;

@Service
public class AlumnoService {

    private final QbidTransport http;
    private final QbidEndpointFactory urls;
    private final FichaAlumnoParser parser;

    public AlumnoService(QbidTransport http, QbidEndpointFactory urls, FichaAlumnoParser parser) {
        this.http   = http;
        this.urls   = urls;
        this.parser = parser;
    }

    public FichaAlumnoDTO getFicha(String jsessionid, String codAlumno) throws Exception {
        String html = http.get(urls.fichaAlumno(codAlumno), jsessionid);
        return parser.parseFicha(html);
    }
}


