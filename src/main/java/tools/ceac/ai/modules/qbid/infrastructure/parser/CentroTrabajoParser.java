package tools.ceac.ai.modules.qbid.infrastructure.parser;

import tools.ceac.ai.modules.qbid.domain.model.CentroTrabajoDTO;
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
                // IdentificaciÃ³n
                .nomenclatura(byLabel(ctPanel,  "Nomenclatura:"))
                .estado(byLabel(ctPanel,        "Estado:"))
                .categoria(byLabel(ctPanel,     "CategorÃ­a:"))
                .nombre(byLabel(ctPanel,        "Nombre:"))
                .nombreOpcional(byLabel(ctPanel,"Nombre opcional:"))
                // UbicaciÃ³n
                .ubicacion(byLabel(ubicTab,          "UbicaciÃ³n:"))
                .pais(byLabel(ubicTab,               "PaÃ­s:"))
                .codigoPostal(byLabel(ubicTab,       "CÃ³digo Postal:"))
                .municipio(byLabel(ubicTab,          "Municipio/Localidad:"))
                .via(byLabel(ubicTab,                "VÃ­a:"))
                .numero(byLabel(ubicTab,             "NÃºmero:"))
                .escaleraPisoPuerta(byLabel(ubicTab, "Escalera/Piso/Puerta:"))
                .restoDireccion(byLabel(ubicTab,     "Resto direcciÃ³n:"))
                .poligono(byLabel(ubicTab,           "PolÃ­gono:"))
                .territorio(byLabel(ubicTab,         "Territorio:"))
                // Contacto
                .telefono(byLabel(contTab, "TelÃ©fono:"))
                .fax(byLabel(contTab,      "Fax:"))
                .email(byLabel(contTab,    "Correo electrÃ³nico:"))
                // Actividad
                .ccae(byLabel(activTab, "CCAE:"))
                .build();
    }

    // â”€â”€ Helpers â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    private String formValue(Document doc, String name) {
        Element el = doc.selectFirst("input[name=" + name + "]");
        return el != null ? el.attr("value").trim() : "";
    }

    /**
     * Busca label.control-label con ese texto dentro del scope dado,
     * y devuelve el texto del siguiente elemento hermano.
     * Acepta scope null (bÃºsqueda global en el doc) para uso defensivo.
     */
    private String byLabel(Element scope, String labelText) {
        if (scope == null) return "";
        for (Element label : scope.select("label.control-label")) {
            if (label.text().trim().equals(labelText)) {
                Element sibling = label.nextElementSibling();
                if (sibling != null) {
                    // Los valores vacÃ­os a veces vienen como "-"
                    String text = sibling.text().trim();
                    return "-".equals(text) ? "" : text;
                }
            }
        }
        return "";
    }
}


