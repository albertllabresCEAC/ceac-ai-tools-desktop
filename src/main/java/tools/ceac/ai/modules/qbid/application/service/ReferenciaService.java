package tools.ceac.ai.modules.qbid.application.service;

import tools.ceac.ai.modules.qbid.application.port.out.QbidTransport;
import tools.ceac.ai.modules.qbid.domain.model.ReferenciaDTO;
import org.springframework.stereotype.Service;

import java.util.Base64;

/**
 * Servicio genÃ©rico para descarga de referencias (REF XX) en formato base64.
 * La URL y el nombre de fichero los construye el caller (controller o MCP tool)
 * usando el endpoint factory de qBid; este servicio no sabe quÃ© tipo de REF estÃ¡ descargando.
 * Para aÃ±adir una nueva referencia basta con aÃ±adir su URL en la infraestructura qBid y
 * un endpoint en ReferenciaController; este servicio no cambia.
 */
@Service
public class ReferenciaService {

    private final QbidTransport http;

    public ReferenciaService(QbidTransport http) {
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


