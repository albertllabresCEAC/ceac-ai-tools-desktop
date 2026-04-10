package tools.ceac.ai.mcp.qbid.parser;

import tools.ceac.ai.mcp.qbid.model.dto.CentroTrabajoDTO;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

@Component
public class CentroTrabajoParser {

    public CentroTrabajoDTO parseCentroTrabajo(String html) {
        Document doc = Jsoup.parse(html);

        Element ctPanel  = doc.selectFirst("#collapseCT");
        Element ubicTab  = doc.selectFirst("#dadesUbicacio");
        Element contTab  = doc.selectFirst("#dadesContacte");
        Element activTab = doc.selectFirst("#dadesActivitat");

        return CentroTrabajoDTO.builder()
                .codCentro(formValue(doc, "cod_centre_treball_pk"))
                .codEmpresa(formValue(doc, "cod_empresa_pk"))
                // Identificación
                .nomenclatura(byLabel(ctPanel,  "Nomenclatura:"))
                .estado(byLabel(ctPanel,        "Estado:"))
                .categoria(byLabel(ctPanel,     "Categoría:"))
                .nombre(byLabel(ctPanel,        "Nombre:"))
                .nombreOpcional(byLabel(ctPanel,"Nombre opcional:"))
                // Ubicación
                .ubicacion(byLabel(ubicTab,          "Ubicación:"))
                .pais(byLabel(ubicTab,               "País:"))
                .codigoPostal(byLabel(ubicTab,       "Código Postal:"))
                .municipio(byLabel(ubicTab,          "Municipio/Localidad:"))
                .via(byLabel(ubicTab,                "Vía:"))
                .numero(byLabel(ubicTab,             "Número:"))
                .escaleraPisoPuerta(byLabel(ubicTab, "Escalera/Piso/Puerta:"))
                .restoDireccion(byLabel(ubicTab,     "Resto dirección:"))
                .poligono(byLabel(ubicTab,           "Polígono:"))
                .territorio(byLabel(ubicTab,         "Territorio:"))
                // Contacto
                .telefono(byLabel(contTab, "Teléfono:"))
                .fax(byLabel(contTab,      "Fax:"))
                .email(byLabel(contTab,    "Correo electrónico:"))
                // Actividad
                .ccae(byLabel(activTab, "CCAE:"))
                .build();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String formValue(Document doc, String name) {
        Element el = doc.selectFirst("input[name=" + name + "]");
        return el != null ? el.attr("value").trim() : "";
    }

    /**
     * Busca label.control-label con ese texto dentro del scope dado,
     * y devuelve el texto del siguiente elemento hermano.
     * Acepta scope null (búsqueda global en el doc) para uso defensivo.
     */
    private String byLabel(Element scope, String labelText) {
        if (scope == null) return "";
        for (Element label : scope.select("label.control-label")) {
            if (label.text().trim().equals(labelText)) {
                Element sibling = label.nextElementSibling();
                if (sibling != null) {
                    // Los valores vacíos a veces vienen como "-"
                    String text = sibling.text().trim();
                    return "-".equals(text) ? "" : text;
                }
            }
        }
        return "";
    }
}
