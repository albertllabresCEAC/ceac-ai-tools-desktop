package tools.ceac.ai.mcp.qbid.service;

import tools.ceac.ai.mcp.qbid.model.dto.ReferenciaDTO;
import org.springframework.stereotype.Service;

import java.util.Base64;

/**
 * Servicio genérico para descarga de referencias (REF XX) en formato base64.
 * La URL y el nombre de fichero los construye el caller (controller o MCP tool)
 * usando QbidUrls — este servicio no sabe qué tipo de REF está descargando.
 * Para añadir una nueva referencia basta con añadir su URL en QbidUrls y
 * un endpoint en ReferenciaController; este servicio no cambia.
 */
@Service
public class ReferenciaService {

    private final QbidHttpService http;

    public ReferenciaService(QbidHttpService http) {
        this.http = http;
    }

    public ReferenciaDTO getDocumento(String jsessionid,
                                      String url,
                                      String filename) throws Exception {
        byte[] bytes = http.getBytes(url, jsessionid);
        return ReferenciaDTO.builder()
                .filename(filename)
                .contentType("application/pdf")
                .encoding("base64")
                .data(Base64.getEncoder().encodeToString(bytes))
                .build();
    }
}
