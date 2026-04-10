package tools.ceac.ai.mcp.qbid.parser;

import tools.ceac.ai.mcp.qbid.model.dto.FichaAlumnoDTO;
import tools.ceac.ai.mcp.qbid.model.dto.FichaAlumnoDTO.Cuaderno;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class FichaAlumnoParser {

    private static final Pattern P_QUAERN =
            Pattern.compile("doVeureQuadern\\('(\\d+)'\\s*,\\s*'(\\d+)'\\)");

    public FichaAlumnoDTO parseFicha(String html) {
        Document doc = Jsoup.parse(html);

        return FichaAlumnoDTO.builder()
                .codAlumno(formValue(doc, "alumneId"))
                .usuario(parseUsuario(doc))
                .extranjero(byLabel(doc, "Extranjero:"))
                .nombre(byLabel(doc, "Nombre:"))
                .apellidos(byLabel(doc, "Apellidos:"))
                .fechaNacimiento(byLabel(doc, "Fecha de nacimiento:"))
                .sexo(byLabel(doc, "Sexo:"))
                .direccion(byLabel(doc, "Dirección:"))
                .telefono(byLabel(doc, "Teléfono:"))
                .pais(byLabel(doc, "País(Dirección):"))
                .codigoPostal(byLabel(doc, "Código Postal:"))
                .municipio(byLabel(doc, "Municipio/Localidad:"))
                .inss(byLabel(doc, "INSS/Mutua:"))
                .nass(byLabel(doc, "NASS:"))
                .idalu(byLabel(doc, "IDALU:"))
                .email(parseEmail(doc))
                .necesidadesEspeciales(byLabel(doc, "Necesidades especiales:"))
                .observaciones(byLabel(doc, "Observaciones:"))
                .cuadernos(parseCuadernos(doc))
                .build();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Valor de un hidden input por name */
    private String formValue(Document doc, String name) {
        Element el = doc.selectFirst("input[name=" + name + "]");
        return el != null ? el.attr("value").trim() : "";
    }

    /**
     * El usuario aparece en: <div class="alert alert-info"><strong>Usuario</strong>: lca5fbdh</div>
     * Extraemos el texto que queda tras el tag <strong>.
     */
    private String parseUsuario(Document doc) {
        Element alert = doc.selectFirst(".alert.alert-info");
        if (alert == null) return "";
        String full = alert.text().trim();          // "Usuario: lca5fbdh"
        int colon = full.indexOf(':');
        return colon >= 0 ? full.substring(colon + 1).trim() : full;
    }

    /**
     * Para la mayoría de campos: el siguiente sibling del label.control-label
     * es un div con un <p class="form-control-static"> o un <a>.
     */
    private String byLabel(Document doc, String labelText) {
        for (Element label : doc.select("label.control-label")) {
            if (label.text().trim().equals(labelText)) {
                Element sibling = label.nextElementSibling();
                if (sibling != null) return sibling.text().trim();
            }
        }
        return "";
    }

    /** El correo está en un <a href="mailto:..."> dentro del div siguiente al label */
    private String parseEmail(Document doc) {
        for (Element label : doc.select("label.control-label")) {
            if (label.text().trim().equals("Correo electrónico:")) {
                Element sibling = label.nextElementSibling();
                if (sibling != null) {
                    Element link = sibling.selectFirst("a[href^=mailto]");
                    if (link != null) return link.text().trim();
                    return sibling.text().trim();
                }
            }
        }
        return "";
    }

    /**
     * Recorre todos los .panel-group que contengan botones doVeureQuadern.
     * El estado se extrae del texto del panel-heading del grupo padre.
     */
    private List<Cuaderno> parseCuadernos(Document doc) {
        List<Cuaderno> result = new ArrayList<>();

        for (Element panelGroup : doc.select(".panel-group")) {
            String estado = "";
            Element heading = panelGroup.selectFirst(".panel-heading");
            if (heading != null) {
                // El heading contiene el texto del estado + un badge con el número
                // Usamos ownText() para evitar coger el número del badge
                estado = heading.select(".panel-title a").first() != null
                        ? cleanHeadingText(heading.select(".panel-title a").first())
                        : heading.text().trim();
            }

            for (Element btn : panelGroup.select("button[onclick*='doVeureQuadern']")) {
                Matcher m = P_QUAERN.matcher(btn.attr("onclick"));
                if (!m.find()) continue;

                String codCuaderno = m.group(1);
                String codPrograma = m.group(2);

                // El nombre del estudio está en el label strong dentro del mismo row
                String estudio = "";
                Element row = btn.closest(".form-group");
                if (row != null) {
                    Element lbl = row.selectFirst("label strong");
                    if (lbl != null) estudio = lbl.text().trim();
                    else {
                        Element lbl2 = row.selectFirst("label");
                        if (lbl2 != null) estudio = lbl2.text().trim();
                    }
                }

                result.add(Cuaderno.builder()
                        .estudio(estudio)
                        .codCuaderno(codCuaderno)
                        .codPrograma(codPrograma)
                        .estado(estado)
                        .build());
            }
        }
        return result;
    }

    /**
     * El <a> del heading tiene el texto del estado + el badge (<span class="badge">N</span>).
     * Queremos sólo el texto sin el número.
     */
    private String cleanHeadingText(Element anchor) {
        // Clona para no mutar el DOM
        Element clone = anchor.clone();
        clone.select(".badge").remove();
        return clone.text().trim();
    }
}
