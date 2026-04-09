package tools.ceac.ai.mcp.qbid.service;

import tools.ceac.ai.mcp.qbid.model.dto.FichaAlumnoDTO;
import tools.ceac.ai.mcp.qbid.parser.FichaAlumnoParser;
import org.springframework.stereotype.Service;

@Service
public class AlumnoService {

    private final QbidHttpService http;
    private final QbidUrls urls;
    private final FichaAlumnoParser parser;

    public AlumnoService(QbidHttpService http, QbidUrls urls, FichaAlumnoParser parser) {
        this.http   = http;
        this.urls   = urls;
        this.parser = parser;
    }

    public FichaAlumnoDTO getFicha(String jsessionid, String codAlumno) throws Exception {
        String html = http.get(urls.fichaAlumno(codAlumno), jsessionid);
        return parser.parseFicha(html);
    }
}
