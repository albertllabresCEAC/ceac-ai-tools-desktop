package tools.ceac.ai.mcp.qbid.parser;

import tools.ceac.ai.mcp.qbid.model.dto.DetalleConvenioDTO;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class DetalleConvenioParser {

    private static final Pattern P_AID    = Pattern.compile("aid=(\\d+)");
    private static final Pattern P_HASH   = Pattern.compile("hash_code=([a-f0-9]+)");
    private static final Pattern P_FECHAS = Pattern.compile("(\\d{2}/\\d{2}/\\d{4})\\s*-\\s*(\\d{2}/\\d{2}/\\d{4})");
    private static final Pattern P_ESTUDI = Pattern.compile("estudiId=(\\d+)");

    public DetalleConvenioDTO parseDetalle(String html) {
        Document doc = Jsoup.parse(html);

        // codAlumno y nombreAlumno â€” del href/title del enlace del alumno
        String codAlumno    = "";
        String nombreAlumno = "";
        Element alumnoLink  = doc.selectFirst("a[href*='aid=']");
        if (alumnoLink != null) {
            Matcher m = P_AID.matcher(alumnoLink.attr("href"));
            if (m.find()) codAlumno = m.group(1);
            nombreAlumno = alumnoLink.attr("title").trim();
        }

        // codEmpresa y nombre limpio
        // Estructura real: <input name="codiEmpresa" value="366143"> en el mismo div
        // El nombre limpio estÃ¡ en title="KARVE INFORMATICA, S.L." del <a>
        String codEmpresa = "";
        String empresa    = "";
        for (Element label : doc.select("label.control-label")) {
            if (label.text().trim().equals("Empresa:")) {
                Element sibling = label.nextElementSibling();
                if (sibling != null) {
                    Element codiInput = sibling.selectFirst("input[name='codiEmpresa']");
                    if (codiInput != null) codEmpresa = codiInput.attr("value").trim();
                    Element link = sibling.selectFirst("a");
                    if (link != null) {
                        empresa = link.attr("title").trim();
                        if (empresa.isBlank()) empresa = link.text().trim();
                    }
                }
                break;
            }
        }

        // Campos estÃ¡ndar por label
        String fechas      = extractByLabel(doc, "Periodo del acuerdo:");
        String horasTot    = extractByLabel(doc, "Horas de la estancia en la empresa:");
        String horasPend   = extractByLabel(doc, "Horas pendientes:");
        String horasAcum   = extractByLabel(doc, "Horas acumuladas:");
        String estudio     = extractByLabel(doc, "Estudio:");
        String curso       = extractByLabel(doc, "Curso:");
        String tipoAcuerdo = extractByLabel(doc, "Tipo acuerdo:");

        // Tutor empresa â€” label dinÃ¡mico con id labelTutorsEmpresa{id}
        String tutorEmpresa = "";
        Element tutorLabel  = doc.selectFirst("label[id^='labelTutorsEmpresa']");
        if (tutorLabel != null) {
            // El label contiene el nombre + un enlace de borrar â€” quedarse solo con el texto directo
            tutorEmpresa = tutorLabel.ownText().trim();
        }

        // Profesor/Tutor â€” option selected del select#professor
        String profesorTutor = "";
        Element profSelect   = doc.getElementById("professor");
        if (profSelect != null) {
            Element selected = profSelect.selectFirst("option[selected]");
            if (selected != null) profesorTutor = selected.text().trim();
        }

        // hash_code REF05 â€” del onclick del botÃ³n REF05 (CONVEN)
        String hashRef05 = "";
        Element ref05btn = doc.selectFirst("button[onclick*='tipus_document=CONVEN']");
        if (ref05btn != null) {
            Matcher m = P_HASH.matcher(ref05btn.attr("onclick"));
            if (m.find()) hashRef05 = m.group(1);
        }

        // hash_code REF05_Baja â€” del onclick del botÃ³n REF05 finalizaciÃ³n anticipada (CONVENBAIXA)
        String hashRef05Baja = "";
        Element ref05BajaBtn = doc.selectFirst("button[onclick*='tipus_document=CONVENBAIXA']");
        if (ref05BajaBtn != null) {
            Matcher m = P_HASH.matcher(ref05BajaBtn.attr("onclick"));
            if (m.find()) hashRef05Baja = m.group(1);
        }

        // curs_seleccio â€” campo oculto del formulario
        String cursSeleccio = "";
        Element cursInput = doc.selectFirst("input[name='curs_seleccio']");
        if (cursInput != null) cursSeleccio = cursInput.attr("value");

        // estudiId â€” del onclick del botÃ³n REF06
        String estudiId = "";
        Element ref06btn = doc.selectFirst("button[onclick*='moduleaction=documentPDF']");
        if (ref06btn != null) {
            Matcher m = P_ESTUDI.matcher(ref06btn.attr("onclick"));
            if (m.find()) estudiId = m.group(1);
        }

        // Separar fechaInicio / fechaFin del string "16/03/2026 - 01/07/2026"
        String fechaInicio = "";
        String fechaFin    = "";
        if (fechas != null) {
            Matcher m = P_FECHAS.matcher(fechas);
            if (m.find()) {
                fechaInicio = m.group(1);
                fechaFin    = m.group(2);
            }
        }

        return DetalleConvenioDTO.builder()
                .codAlumno(codAlumno)
                .nombreAlumno(nombreAlumno)
                .codEmpresa(codEmpresa)
                .empresa(empresa)
                .tutorEmpresa(tutorEmpresa)
                .profesorTutor(profesorTutor)
                .fechaInicio(fechaInicio)
                .fechaFin(fechaFin)
                .horasTotales(limpiarHoras(horasTot))
                .horasPendientes(limpiarHoras(horasPend))
                .horasAcumuladas(limpiarHoras(horasAcum))
                .estudio(estudio)
                .curso(curso)
                .tipoAcuerdo(tipoAcuerdo)
                .hashCodeRef05(hashRef05)
                .hashCodeRef05Baja(hashRef05Baja)
                .cursSeleccio(cursSeleccio)
                .estudiId(estudiId)
                .build();
    }

    // â”€â”€ Helpers â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    /**
     * Busca un label.control-label con ese texto exacto y devuelve
     * el texto del elemento hermano siguiente (el valor).
     */
    private String extractByLabel(Document doc, String labelText) {
        for (Element label : doc.select("label.control-label")) {
            if (label.text().trim().equals(labelText)) {
                Element sibling = label.nextElementSibling();
                if (sibling != null) return sibling.text().trim();
            }
        }
        return "";
    }

    /** Quita todo excepto dÃ­gitos: "515&nbsp;h" â†’ "515" */
    private String limpiarHoras(String raw) {
        if (raw == null || raw.isBlank()) return "0";
        return raw.replaceAll("[^0-9]", "");
    }
}

