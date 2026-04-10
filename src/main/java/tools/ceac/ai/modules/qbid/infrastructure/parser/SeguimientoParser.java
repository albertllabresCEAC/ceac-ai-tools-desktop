package tools.ceac.ai.modules.qbid.infrastructure.parser;

import tools.ceac.ai.modules.qbid.domain.model.SeguimientoDTO;
import tools.ceac.ai.modules.qbid.domain.model.SeguimientoDTO.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class SeguimientoParser {

    private static final Pattern P_AID              = Pattern.compile("aid=(\\d+)");
    private static final Pattern P_FECHAS          = Pattern.compile("de\\s+(\\d{2}/\\d{2}/\\d{4})\\s*[a&nbsp;]+\\s*(\\d{2}/\\d{2}/\\d{4})");
    private static final Pattern P_DOC             = Pattern.compile("moduleaction=document.*?docType=(\\w+).*?language=SP.*?'\\s*,\\s*'PDF'");
    private static final Pattern P_COD_VISITA      = Pattern.compile("cod_visita=(\\d+)");
    private static final Pattern P_EDITAR_VALORACIO = Pattern.compile("editValoracioAvaluacio\\((\\d+),\\s*(\\d+)\\)");

    public SeguimientoDTO parseSeguimiento(String html) {
        Document doc = Jsoup.parse(html);

        return SeguimientoDTO.builder()
                .cabecera(parseCabecera(doc))
                .seguimientoAlumno(parseSeguimientoAlumno(doc))
                .valoracion(parseValoracion(doc))
                .homologacion(parseHomologacion(doc))
                .documentos(parseDocumentos(doc))
                .build();
    }

    // â”€â”€ Cabecera â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    private Cabecera parseCabecera(Document doc) {
        // codAlumno y nombreAlumno â€” del enlace aid=
        String codAlumno    = "";
        String nombreAlumno = "";
        Element alumnoLink  = doc.selectFirst("a[href*='aid=']");
        if (alumnoLink != null) {
            Matcher m = P_AID.matcher(alumnoLink.attr("href"));
            if (m.find()) codAlumno = m.group(1);
            nombreAlumno = alumnoLink.attr("title").trim();
        }

        // Modalidades â€” spans label-info de la primera fila
        List<Element> spans = doc.select(".label.label-info");
        String modalidadEnsenanza = spans.size() > 0 ? spans.get(0).text().trim() : "";
        String movilidad          = spans.size() > 1 ? spans.get(1).text().trim() : "";
        String modalidadPresencial= spans.size() > 2 ? spans.get(2).text().trim() : "";

        return Cabecera.builder()
                .codAlumno(codAlumno)
                .nombreAlumno(nombreAlumno)
                .empresa(extractByLabel(doc, "Empresa:"))
                .centroDeTrabajo(extractByLabel(doc, "Centro de Trabajo:"))
                .profesorTutor(extractLinkByLabel(doc, "Profesor/Tutor/a:"))
                .estudio(extractLinkByLabel(doc, "Estudio:"))
                .modalidadEnsenanza(modalidadEnsenanza)
                .movilidad(movilidad)
                .modalidadPresencial(modalidadPresencial)
                .build();
    }

    // â”€â”€ SeguimientoAlumno â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    private SeguimientoAlumno parseSeguimientoAlumno(Document doc) {
        // Periodo â€” "de 16/03/2026 a 01/07/2026"
        String periodoInicio = "";
        String periodoFin    = "";
        String periodoRaw    = extractByLabel(doc, "Periodo del acuerdo:");
        Matcher m = P_FECHAS.matcher(periodoRaw);
        if (m.find()) {
            periodoInicio = m.group(1);
            periodoFin    = m.group(2);
        }

        return SeguimientoAlumno.builder()
                .periodoInicio(periodoInicio)
                .periodoFin(periodoFin)
                .contactoInicial(extractByLabel(doc, "Contacto inicial:"))
                .contactoSeguimiento(extractByLabel(doc, "Contacto seguimiento:"))
                .contactoValoracion(extractByLabel(doc, "Contacto valoraciÃ³n:"))
                .build();
    }

    // â”€â”€ Valoracion â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    private Valoracion parseValoracion(Document doc) {
        // cod_visita de REF18 â€” del onclick del botÃ³n QUALI_EXPEDIENT
        String codVisitaRef18 = "";
        Element ref18btn = doc.selectFirst("button[onclick*='docType=QUALI_EXPEDIENT']");
        if (ref18btn != null) {
            Matcher m = P_COD_VISITA.matcher(ref18btn.attr("onclick"));
            if (m.find()) codVisitaRef18 = m.group(1);
        }

        // cod_visita_valoracion â€” del onclick editValoracioAvaluacio(codVisitaValoracion, codVisita)
        String codVisitaValoracion = "";
        for (Element el : doc.select("[onclick*='editValoracioAvaluacio']")) {
            Matcher m = P_EDITAR_VALORACIO.matcher(el.attr("onclick"));
            if (m.find()) {
                codVisitaValoracion = m.group(1);
                break;
            }
        }

        return Valoracion.builder()
                .valoracionDossier(extractByLabel(doc, "ValoraciÃ³n dossier:"))
                .cuestionarioCentroTrabajo(extractByLabel(doc, "REF10. Cuestionario al Centro de Trabajo:"))
                .codVisitaRef18(codVisitaRef18)
                .codVisitaValoracion(codVisitaValoracion)
                .build();
    }

    // â”€â”€ Homologacion â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    private Homologacion parseHomologacion(Document doc) {
        // Los tres campos estÃ¡n dentro del panel "HomologaciÃ³n"
        Element panel = findPanelByTitle(doc, "HomologaciÃ³n");
        if (panel == null) {
            return Homologacion.builder().build();
        }

        return Homologacion.builder()
                .profesorTutor(extractByLabelInScope(panel, "Profesor/Tutor/a:"))
                .coordPracticas(extractByLabelInScope(panel, "Coord. PrÃ¡cticas:"))
                .coordTerritorial(extractByLabelInScope(panel, "Coord. Territorial:"))
                .build();
    }

    // â”€â”€ Documentos â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    private List<Documento> parseDocumentos(Document doc) {
        List<Documento> result = new ArrayList<>();
        String base = "https://www.empresaiformacio.org/sBid";

        // Busca todos los botones con onclick que abren un PDF
        for (Element btn : doc.select("button[onclick*='moduleaction=document']")) {
            String onclick = btn.attr("onclick");

            // Extraer el texto del botÃ³n como ref (REF06, REF07...)
            String ref = btn.text().trim();
            if (ref.isBlank()) continue;

            // Construir URL en castellano buscando el onclick de la opciÃ³n "Castellano"
            // o usando la URL directa del botÃ³n principal
            String url = extractDocUrl(onclick, base);
            if (url.isBlank()) continue;

            result.add(Documento.builder()
                    .ref(ref)
                    .url(url)
                    .build());
        }

        return result;
    }

    // â”€â”€ Helpers â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    private String extractByLabel(Document doc, String labelText) {
        for (Element label : doc.select("label.control-label")) {
            if (label.text().trim().equals(labelText)) {
                Element sibling = label.nextElementSibling();
                if (sibling != null) return sibling.text().trim();
            }
        }
        return "";
    }

    private String extractByLabelInScope(Element scope, String labelText) {
        for (Element label : scope.select("label.control-label")) {
            if (label.text().trim().equals(labelText)) {
                Element sibling = label.nextElementSibling();
                if (sibling != null) return sibling.text().trim();
            }
        }
        return "";
    }

    /** Extrae el texto del enlace dentro del div siguiente al label */
    private String extractLinkByLabel(Document doc, String labelText) {
        for (Element label : doc.select("label.control-label")) {
            if (label.text().trim().equals(labelText)) {
                Element sibling = label.nextElementSibling();
                if (sibling != null) {
                    Element link = sibling.selectFirst("a");
                    return link != null ? link.attr("title").trim() : sibling.text().trim();
                }
            }
        }
        return "";
    }

    /** Busca un panel por el texto de su panel-heading */
    private Element findPanelByTitle(Document doc, String title) {
        for (Element heading : doc.select(".panel-heading")) {
            if (heading.text().trim().equals(title)) {
                return heading.parent();
            }
        }
        return null;
    }

    /** Extrae la URL del PDF en castellano del onclick del botÃ³n */
    private String extractDocUrl(String onclick, String base) {
        // El onclick tiene: window.open('/sBid/modules/Fct?...language=SP', 'PDF')
        Pattern p = Pattern.compile("window\\.open\\('(/sBid/[^']+language=SP)'");
        Matcher m = p.matcher(onclick);
        if (m.find()) return base + m.group(1).replace("&amp;", "&");
        return "";
    }
}



