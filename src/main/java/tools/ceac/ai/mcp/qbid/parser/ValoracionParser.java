package tools.ceac.ai.mcp.qbid.parser;

import tools.ceac.ai.mcp.qbid.model.dto.ValoracionDTO;
import tools.ceac.ai.mcp.qbid.model.dto.ValoracionDTO.Criterio;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ValoracionParser {

    public ValoracionDTO parseValoracion(String html) {
        Document doc = Jsoup.parse(html);

        return ValoracionDTO.builder()
                .qualificacio(selectedText(doc, "qualificacio"))
                .subQualificacio(selectedText(doc, "subqualificacio"))
                .fecha(inputValue(doc, "dataQualy"))
                .observaciones(textareaValue(doc, "observacions"))
                .fechaContacto(inputValue(doc, "data_realitzada"))
                .tipoContacto(resolverTipoContacto(doc))
                .signatarioEmpresa(parseSignatario(doc, "empresa"))
                .signatarioCentro(parseSignatario(doc, "centre"))
                .criterios(parseCriterios(doc))
                .build();
    }

    // â”€â”€ Helpers â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    /** Texto de la opciÃ³n selected en un <select name="..."> */
    private String selectedText(Document doc, String selectName) {
        Element sel = doc.selectFirst("select[name=" + selectName + "]");
        if (sel == null) return "";
        Element opt = sel.selectFirst("option[selected]");
        return opt != null ? opt.text().trim() : "";
    }

    /** Valor de un <input name="..."> */
    private String inputValue(Document doc, String name) {
        Element el = doc.selectFirst("input[name=" + name + "]");
        return el != null ? el.attr("value").trim() : "";
    }

    /** Contenido de un <textarea name="..."> */
    private String textareaValue(Document doc, String name) {
        Element el = doc.selectFirst("textarea[name=" + name + "]");
        return el != null ? el.text().trim() : "";
    }

    /**
     * Tipo de contacto: primero busca un <select name="tipus_visita"> con opciÃ³n
     * seleccionada; si no existe, toma el valor de un <input name="tipus_visita">.
     */
    private String resolverTipoContacto(Document doc) {
        Element sel = doc.selectFirst("select[name=tipus_visita]");
        if (sel != null) {
            Element opt = sel.selectFirst("option[selected]");
            return opt != null ? opt.text().trim() : "";
        }
        return inputValue(doc, "tipus_visita");
    }

    /**
     * Signatario: busca el input oculto o de texto que contenga el nombre.
     * La pÃ¡gina tiene campos como "signatura_empresa" o similar.
     * Fallback: busca en el texto de un label/span cercano a "empresa"/"centre".
     */
    private String parseSignatario(Document doc, String tipo) {
        // Intenta por input name que contenga el tipo
        for (Element el : doc.select("input[name*=" + tipo + "]")) {
            String val = el.attr("value").trim();
            if (!val.isBlank()) return val;
        }
        // Intenta por un span/div dentro de un row que tenga un label con el tipo
        for (Element label : doc.select("label")) {
            if (label.text().toLowerCase().contains(tipo)) {
                Element sibling = label.nextElementSibling();
                if (sibling != null) {
                    String txt = sibling.text().trim();
                    if (!txt.isBlank()) return txt;
                }
            }
        }
        return "";
    }

    /**
     * Criterios de valoraciÃ³n (quest_0 â€¦ quest_20).
     * Cada criterio es un grupo de radios con el mismo name.
     * El texto de la pregunta estÃ¡ en el label asociado al grupo.
     */
    private List<Criterio> parseCriterios(Document doc) {
        List<Criterio> result = new ArrayList<>();
        int i = 0;
        while (true) {
            String name = "quest_" + i;
            // Busca el radio seleccionado (checked)
            Element checked = doc.selectFirst("input[type=radio][name=" + name + "][checked]");
            if (checked == null) {
                // TambiÃ©n puede venir sin atributo checked si estÃ¡ deshabilitado/readonly;
                // en ese caso busca por value en los inputs hidden con el mismo name
                Element hidden = doc.selectFirst("input[type=hidden][name=" + name + "]");
                if (hidden == null) break;   // ya no hay mÃ¡s criterios
                String val = hidden.attr("value");
                String texto = extractCriterioLabel(doc, name, i);
                String valoracion = resolveValoracionText(doc, name, val);
                result.add(Criterio.builder().texto(texto).valoracion(valoracion).build());
            } else {
                String val = checked.attr("value");
                String texto = extractCriterioLabel(doc, name, i);
                String valoracion = resolveValoracionText(doc, name, val);
                result.add(Criterio.builder().texto(texto).valoracion(valoracion).build());
            }
            i++;
        }
        return result;
    }

    /**
     * Intenta obtener el texto de la pregunta buscando el label del grupo de radios.
     * Estrategia: buscar un label cuyo for apunte a quest_N o que sea el primer label
     * dentro del mismo form-group.
     */
    private String extractCriterioLabel(Document doc, String name, int index) {
        // Label con for="quest_N"
        Element lbl = doc.selectFirst("label[for=" + name + "]");
        if (lbl != null) return lbl.text().trim();

        // El primer radio del grupo puede estar dentro de un tr/td â€” subimos al row
        Element radio = doc.selectFirst("input[name=" + name + "]");
        if (radio != null) {
            // Subir hasta un <tr> o .form-group y buscar el primer texto significativo
            Element row = radio.closest("tr");
            if (row != null) {
                // Primer <td> tiene normalmente el texto de la pregunta
                Element td = row.selectFirst("td");
                if (td != null) return td.text().trim();
            }
            Element fg = radio.closest(".form-group");
            if (fg != null) {
                Element fl = fg.selectFirst("label");
                if (fl != null) return fl.text().trim();
            }
        }
        return "Criterio " + (index + 1);
    }

    /**
     * Dada la lista de opciones de un grupo de radios, devuelve el label del valor
     * seleccionado. Si no hay labels junto a los radios, devuelve el valor bruto.
     */
    private String resolveValoracionText(Document doc, String name, String value) {
        for (Element radio : doc.select("input[type=radio][name=" + name + "]")) {
            if (radio.attr("value").equals(value)) {
                // Busca el label inmediatamente siguiente o su texto circundante
                Element next = radio.nextElementSibling();
                if (next != null && next.tagName().equals("label")) {
                    return next.text().trim();
                }
                // Intenta con label[for=id]
                String id = radio.attr("id");
                if (!id.isBlank()) {
                    Element lbl = doc.selectFirst("label[for=" + id + "]");
                    if (lbl != null) return lbl.text().trim();
                }
                // Sube al td y busca texto alrededor
                Element td = radio.closest("td");
                if (td != null) return td.text().trim();
            }
        }
        return value;
    }
}
