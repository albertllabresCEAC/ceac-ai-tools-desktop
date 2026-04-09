package tools.ceac.ai.mcp.qbid.parser;

import tools.ceac.ai.mcp.qbid.model.dto.AcuerdoCuadernoDTO;
import tools.ceac.ai.mcp.qbid.model.dto.CuadernoDTO;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class CuadernoParser {

    private static final Pattern P_AID         = Pattern.compile("aid=(\\d+)");
    private static final Pattern P_CUADERNO    = Pattern.compile("quadernId=(\\d+)");
    private static final Pattern P_CONVENIO    = Pattern.compile("doVeureConveni\\('(\\w+)',\\s*'(\\w+)',\\s*'(\\w+)'");
    private static final Pattern P_HASH        = Pattern.compile("hash_code=([a-f0-9]+)");
    private static final Pattern P_HORAS       = Pattern.compile("(\\d+)h/(\\d+)h");
    private static final Pattern P_WINDOW_OPEN = Pattern.compile("window\\.open\\('([^']+)'");

    public CuadernoDTO parseCuaderno(String html) {
        Document doc = Jsoup.parse(html);

        // codCuaderno â€” del quadernId en el JS de la pÃ¡gina
        String codCuaderno = "";
        Matcher mQ = P_CUADERNO.matcher(html);
        if (mQ.find()) codCuaderno = mQ.group(1);

        // codAlumno y nombreAlumno â€” del enlace aid=
        String codAlumno    = "";
        String nombreAlumno = "";
        Element alumnoLink  = doc.selectFirst("a[href*='aid=']");
        if (alumnoLink != null) {
            Matcher m = P_AID.matcher(alumnoLink.attr("href"));
            if (m.find()) codAlumno = m.group(1);
            nombreAlumno = alumnoLink.attr("title").trim();
        }

        // Estado del cuaderno
        String estadoCuaderno = extractByLabel(doc, "Estado del Cuaderno:");

        // Estudio
        String estudio = extractByLabel(doc, "Estudio:");

        // Horas curriculares
        String horasCurriculares = extractByLabel(doc, "Horas curriculares de la estancia en la empresa:");

        // ExenciÃ³n
        String exencion = extractByLabel(doc, "ExenciÃ³n:");

        // hash_code â€” en el input hidden del workingForm
        String hashCode = "";
        Element hashInput = doc.selectFirst("input[name='hash_code']");
        if (hashInput != null) hashCode = hashInput.val();

        // Horas informadas / validadas / restantes â€” dentro del tab de acuerdos
        String horasInformadas         = extractColorSpan(doc, "Horas informadas:");
        String horasValidadas          = extractColorSpan(doc, "Horas validadas:");
        String horasRestantesInformar  = extractColorSpan(doc, "Horas restantes para informar:");
        String horasRestantesValidar   = extractColorSpan(doc, "Horas restantes para validar:");

        // Tabla de acuerdos dentro del tab #conveniTab
        List<AcuerdoCuadernoDTO> acuerdos = parseAcuerdos(doc);

        // URLs de documentos REF â€” presentes solo cuando el cuaderno estÃ¡ cualificado
        String urlRef19 = extractRefUrl(doc, "REF19");
        String urlRef20 = extractRefUrl(doc, "REF20");
        String urlRef22 = extractRefUrl(doc, "REF22");

        return CuadernoDTO.builder()
                .codCuaderno(codCuaderno)
                .codAlumno(codAlumno)
                .nombreAlumno(nombreAlumno)
                .estudio(estudio)
                .estadoCuaderno(estadoCuaderno)
                .horasInformadas(horasInformadas)
                .horasValidadas(horasValidadas)
                .horasRestantesInformar(horasRestantesInformar)
                .horasRestantesValidar(horasRestantesValidar)
                .horasCurriculares(limpiarHoras(horasCurriculares))
                .exencion(exencion)
                .hashCode(hashCode)
                .urlRef19(urlRef19)
                .urlRef20(urlRef20)
                .urlRef22(urlRef22)
                .acuerdos(acuerdos)
                .build();
    }

    private List<AcuerdoCuadernoDTO> parseAcuerdos(Document doc) {
        List<AcuerdoCuadernoDTO> result = new ArrayList<>();

        // Tabla de acuerdos dentro del tab conveniTab
        Element conveniTab = doc.getElementById("conveniTab");
        if (conveniTab == null) return result;

        Elements filas = conveniTab.select("table tbody tr");
        for (Element tr : filas) {
            String documento  = "";
            String fechas     = "";
            String estado     = "";
            String horasReales     = "";
            String horasEstimadas  = "";
            String accionesHtml    = "";

            for (Element td : tr.select("td")) {
                String title = td.attr("data-title").toUpperCase();
                String value = td.text().trim();

                if (title.equals("DOCUMENTO"))        documento   = value;
                if (title.equals("FECHAS"))            fechas      = value;
                if (title.equals("ESTADO"))            estado      = value;
                if (title.equals("REALES/ESTIMADAS")) {
                    Matcher m = P_HORAS.matcher(value);
                    if (m.find()) {
                        // Convenio activo: "0h/518h"
                        horasReales    = m.group(1);
                        horasEstimadas = m.group(2);
                    } else if (!value.isBlank()) {
                        // Convenio cerrado: "386h 15min" â€” sin estimadas
                        horasReales = value;
                    }
                }
                if (title.equals("ACCIONES")) accionesHtml = td.html();
            }

            if (documento.isBlank()) continue;

            Matcher mConv = P_CONVENIO.matcher(accionesHtml);
            boolean tieneConvenio = mConv.find();

            result.add(AcuerdoCuadernoDTO.builder()
                    .documento(documento)
                    .fechas(fechas)
                    .estado(estado)
                    .horasReales(horasReales)
                    .horasEstimadas(horasEstimadas)
                    .codConvenio(tieneConvenio ? mConv.group(1) : "")
                    .codTemporal(tieneConvenio ? mConv.group(2) : "")
                    .newSystem(tieneConvenio   ? mConv.group(3) : "")
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

    /**
     * Extrae el valor del span coloreado que sigue al label con ese texto.
     * Ejemplo: "Horas informadas:" â†’ span con "0h" en rojo
     */
    private String extractColorSpan(Document doc, String labelText) {
        for (Element label : doc.select("label.control-label")) {
            if (label.text().trim().equals(labelText)) {
                Element sibling = label.nextElementSibling();
                if (sibling != null) {
                    Element span = sibling.selectFirst("span");
                    if (span != null) return span.text().trim();
                    return sibling.text().trim();
                }
            }
        }
        return "";
    }

    /**
     * Busca un botÃ³n cuyo texto sea exactamente refName (ej. "REF19") y extrae
     * la URL del window.open del onclick. Devuelve "" si no existe.
     */
    private String extractRefUrl(Document doc, String refName) {
        for (Element btn : doc.select("button")) {
            if (btn.text().trim().equals(refName)) {
                Matcher m = P_WINDOW_OPEN.matcher(btn.attr("onclick"));
                if (m.find()) return m.group(1);
            }
        }
        return "";
    }

    private String limpiarHoras(String raw) {
        if (raw == null || raw.isBlank()) return "0";
        return raw.replaceAll("[^0-9]", "");
    }
}

