package tools.ceac.ai.mcp.qbid.parser;

import tools.ceac.ai.mcp.qbid.model.dto.EmpresaDTO;
import tools.ceac.ai.mcp.qbid.model.dto.EmpresaDTO.CentroResumen;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class EmpresaParser {

    private static final Pattern P_DETALL = Pattern.compile("doDetall\\('(\\d+)'\\)");

    public EmpresaDTO parseEmpresa(String html) {
        Document doc = Jsoup.parse(html);

        return EmpresaDTO.builder()
                .codEmpresa(formValue(doc, "empresaPk"))
                .tipoEntidad(parseTipoEntidad(doc))
                .codigo(byLabel(doc, "Código:"))
                .estado(byLabel(doc, "Estado:"))
                .cifNif(byLabel(doc, "CIF/NIF:"))
                .tipo(byLabel(doc, "Tipo:"))
                .sector(byLabel(doc, "Sector:"))
                .numTrabajadores(byLabel(doc, "Número de trabajadores:"))
                .nombre(byLabel(doc, "Nombre:"))
                // Datos actividad
                .ubicacion(byLabel(doc, "Ubicación:"))
                .pais(byLabel(doc, "País:"))
                .codigoPostal(byLabel(doc, "Código Postal:"))
                .municipio(byLabel(doc, "Municipio/Localidad:"))
                .via(byLabel(doc, "Vía:"))
                .numero(byLabel(doc, "Número:"))
                .escaleraPisoPuerta(byLabel(doc, "Escalera/Piso/Puerta:"))
                .restoDireccion(byLabel(doc, "Resto dirección:"))
                .poligono(byLabel(doc, "Polígono:"))
                .territorio(byLabel(doc, "Territorio:"))
                .camara(byLabel(doc, "Càmara:"))
                .telefono(byLabel(doc, "Teléfono:"))
                .fax(byLabel(doc, "Fax:"))
                // Centros
                .centros(parseCentros(doc))
                .build();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String formValue(Document doc, String name) {
        Element el = doc.selectFirst("input[name=" + name + "]");
        return el != null ? el.attr("value").trim() : "";
    }

    /**
     * En este HTML el label/value pueden ser:
     *   <label class="col-sm-2 control-label">X:</label>
     *   <p class="col-sm-4 form-control-static">valor</p>
     * ambos hermanos directos dentro del mismo .row o .form-group.
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

    /**
     * El tipo de entidad aparece como un label sin valor siguiente:
     *   <label class="col-sm-4 control-label">Entidad Española </label>
     * Buscamos el primer label dentro del panel principal que NO tenga ":" al final.
     */
    private String parseTipoEntidad(Document doc) {
        for (Element label : doc.select(".form-group > label.control-label")) {
            String txt = label.text().trim();
            if (!txt.endsWith(":") && !txt.isBlank()) return txt;
        }
        return "";
    }

    /**
     * Tabla de centros de trabajo: cada fila tiene nombre en el primer <td>
     * y el botón con doDetall('id') en el segundo.
     */
    private List<CentroResumen> parseCentros(Document doc) {
        List<CentroResumen> result = new ArrayList<>();
        Element tabla = doc.selectFirst("#centresTreball table");
        if (tabla == null) return result;

        for (Element tr : tabla.select("tbody tr")) {
            List<Element> tds = tr.select("td");
            if (tds.size() < 2) continue;

            String nombre = tds.get(0).text().trim();

            String codCentro = "";
            Element btn = tds.get(1).selectFirst("[onclick*='doDetall']");
            if (btn != null) {
                Matcher m = P_DETALL.matcher(btn.attr("onclick"));
                if (m.find()) codCentro = m.group(1);
            }

            if (!codCentro.isBlank()) {
                result.add(CentroResumen.builder()
                        .codCentro(codCentro)
                        .nombre(nombre)
                        .build());
            }
        }
        return result;
    }
}
