package tools.ceac.ai.modules.qbid.application.service;

import tools.ceac.ai.modules.qbid.application.port.out.QbidEndpointFactory;
import tools.ceac.ai.modules.qbid.application.port.out.QbidTransport;
import tools.ceac.ai.modules.qbid.domain.model.ActividadDTO;
import tools.ceac.ai.modules.qbid.infrastructure.parser.ActividadParser;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ActividadService {

    private final QbidTransport http;
    private final QbidEndpointFactory urls;
    private final ActividadParser parser;

    public ActividadService(QbidTransport http, QbidEndpointFactory urls, ActividadParser parser) {
        this.http = http;
        this.urls = urls;
        this.parser = parser;
    }

    public ActividadDTO getActividad(String jsessionid,
                                     String codAlumno,
                                     String codConvenio,
                                     String codTemporal,
                                     String fecha) throws Exception {

        String hashRaw = http.get(urls.hashActividad(codAlumno, fecha), jsessionid);
        String[] hashResult = parser.parseHashResponse(hashRaw);

        if (hashResult == null) {
            return ActividadDTO.builder()
                    .fecha(fecha)
                    .horasIntroducidas("N/A")
                    .relleno(false)
                    .actividades(List.of())
                    .build();
        }

        String hashCode          = hashResult[0];
        String conveniActivitatPk = hashResult[1];

        String html = http.get(
                urls.actividadDiaria(codConvenio, codTemporal, fecha, hashCode, conveniActivitatPk),
                jsessionid);

        return parser.parseActividad(html, fecha);
    }

    public String getActividadHtml(String jsessionid,
                                   String codAlumno,
                                   String codConvenio,
                                   String codTemporal,
                                   String fecha) throws Exception {

        String hashRaw = http.get(urls.hashActividad(codAlumno, fecha), jsessionid);
        String[] hashResult = parser.parseHashResponse(hashRaw);

        if (hashResult == null) {
            return "";
        }

        String hashCode           = hashResult[0];
        String conveniActivitatPk = hashResult[1];

        return http.get(
                urls.actividadDiaria(codConvenio, codTemporal, fecha, hashCode, conveniActivitatPk),
                jsessionid);
    }
}



