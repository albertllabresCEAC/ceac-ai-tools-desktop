package tools.ceac.ai.modules.qbid.infrastructure.parser;

import tools.ceac.ai.modules.qbid.domain.model.ConvenioDTO;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parsea el HTML del listado de convenios de qBID.
 * Solo transforma HTML â†’ DTOs. Sin lÃ³gica de negocio.
 */
@Component
public class ConveniosParser {

    // Patrones para extraer cÃ³digos de los onclick de la columna acciones
    private static final Pattern P_CONVENIO  = Pattern.compile("doVeureConveni\\('(\\w+)',\\s*'(\\w+)',\\s*'(\\w+)'");
    private static final Pattern P_CUADERNO  = Pattern.compile("doVeureQuadern\\('(\\w+)'");

    public List<ConvenioDTO> parseListado(String html, String plan) {
        Document doc = Jsoup.parse(html);
        List<ConvenioDTO> result = new ArrayList<>();

        for (Element tr : doc.select("tr")) {
            Elements tds = tr.select("td");
            if (tds.isEmpty()) continue;

            String alumno    = "";
            String estudio   = "";
            String estado    = "";
            String accionesHtml = "";

            for (Element td : tds) {
                String title = td.attr("data-title").toUpperCase();
                String value = td.text().trim();

                if (title.contains("ALUMNO") || title.contains("ALUMNE")) alumno  = value;
                if (title.contains("ESTUDI"))                              estudio = value;
                if (title.contains("ESTAT")  || title.contains("ESTADO")) estado  = value;
                if (title.contains("ACCIO")  || title.contains("ACCION")) accionesHtml = td.html();
            }

            if (alumno.isBlank()) continue;

            // Extraer cÃ³digos de los onclick
            Matcher mConvenio = P_CONVENIO.matcher(accionesHtml);
            Matcher mCuaderno = P_CUADERNO.matcher(accionesHtml);

            boolean tieneConvenio = mConvenio.find();  // una llamada
            boolean tieneCuaderno = mCuaderno.find();

            result.add(ConvenioDTO.builder()
                    .alumno(alumno)
                    .estudio(estudio)
                    .estado(estado)
                    .codConvenio(tieneConvenio ? mConvenio.group(1) : "")
                    .codTemporal(tieneConvenio ? mConvenio.group(2) : "")
                    .newSystem(tieneConvenio   ? mConvenio.group(3) : "1")
                    .codCuaderno(tieneCuaderno ? mCuaderno.group(1) : "")
                    .plan(plan)
                    .build());
        }

        return result;
    }
}



