package tools.ceac.ai.mcp.qbid.parser;

import tools.ceac.ai.mcp.qbid.model.dto.ActividadDTO;
import tools.ceac.ai.mcp.qbid.model.dto.ActividadDTO.Actividad;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ActividadParser {

    private static final String BASE_URL  = "https://www.empresaiformacio.org/sBid";
    private static final Pattern P_HASH   = Pattern.compile("hash_code=([a-zA-Z0-9]+)");
    private static final Pattern P_PK     = Pattern.compile("conveni_activitat_pk=(\\d+)");
    private static final Pattern P_ACT_ID = Pattern.compile("activitat(\\d+)");
    private static final Pattern P_AID    = Pattern.compile("aid=(\\d+)");
    private static final Pattern P_PERIOD = Pattern.compile("de\\s*(\\d{2}/\\d{2}/\\d{4})\\s*a\\s*(\\d{2}/\\d{2}/\\d{4})");

    // ── Paso 1: parsear respuesta AJAX del hash ───────────────────────────────

    public String[] parseHashResponse(String rawText) {
        if (rawText == null || rawText.isBlank()) return null;
        if (rawText.contains("tascasDayCount = 0")
                || rawText.contains("\"0\"")
                || rawText.contains("tascasDayCount = \"0\"")) return null;

        Matcher mHash = P_HASH.matcher(rawText);
        Matcher mPk   = P_PK.matcher(rawText);

        String hash = mHash.find() ? mHash.group(1) : null;
        String pk   = mPk.find()   ? mPk.group(1)   : "";

        if (hash == null) return null;
        return new String[]{ hash, pk };
    }

    // ── Paso 2: parsear HTML de la actividad diaria ───────────────────────────

    public ActividadDTO parseActividad(String html, String fecha) {
        Document doc = Jsoup.parse(html);

        // ── Cabecera alumno ───────────────────────────────────────────────────
        String codAlumno    = "";
        String nombreAlumno = "";
        Element alumnoLink  = doc.selectFirst("a[href*='aid=']");
        if (alumnoLink != null) {
            Matcher m = P_AID.matcher(alumnoLink.attr("href"));
            if (m.find()) codAlumno = m.group(1);
            nombreAlumno = alumnoLink.attr("title").trim();
        }

        String empresa       = extractLinkTitle(doc, "Empresa:");
        String centraTreball = extractLinkTitle(doc, "Centro de Trabajo:");
        String profesorTutor = extractLinkTitle(doc, "Profesor/Tutor/a:");
        String estudio       = extractLinkTitle(doc, "Estudio:");

        String modalidadEnsenanza  = extractLabelSpan(doc, "Modalidad enseñanza:");
        String movilidad           = extractLabelSpan(doc, "Mobilidad:");
        String modalidadPresencial = extractLabelSpan(doc, "Modalidad presencial:");

        String periodoInicio = "";
        String periodoFin    = "";
        Element periodoSpan  = doc.selectFirst("span.label.label-info");
        if (periodoSpan != null) {
            Matcher m = P_PERIOD.matcher(periodoSpan.text());
            if (m.find()) { periodoInicio = m.group(1); periodoFin = m.group(2); }
        }

        // ── Fecha en texto ────────────────────────────────────────────────────
        String fechaTexto = "";
        for (Element label : doc.select("div.panel-heading label.control-label")) {
            String txt = label.ownText().trim();
            if (txt.matches(".*\\d{4}.*") && txt.contains("de")) {
                fechaTexto = txt;
                break;
            }
        }

        // ── Horas máximas ─────────────────────────────────────────────────────
        String horasMaximas = extractLabelSpan(doc, "Horas máximas:");

        // ── URLs anterior / siguiente ─────────────────────────────────────────
        String urlAnterior  = "";
        String urlSiguiente = "";
        Element back = doc.selectFirst("a:has(img[src*='backAlumne'])");
        Element next = doc.selectFirst("a:has(img[src*='nextAlumne'])");
        if (back != null) urlAnterior  = BASE_URL + back.attr("href");
        if (next != null) urlSiguiente = BASE_URL + next.attr("href");

        // ── Actividades ───────────────────────────────────────────────────────
        List<Actividad> actividades = new ArrayList<>();
        for (Element div : doc.select("div[id^='activitat']")) {
            Element select = div.selectFirst("select[id^='inp_']");
            if (select == null) continue;

            Matcher mId = P_ACT_ID.matcher(div.id());
            String id   = mId.find() ? mId.group(1) : div.id();

            Element labelDesc  = div.selectFirst("label.col-sm-9");
            Element labelHoras = div.selectFirst("label.col-sm-1");
            Element selected   = select.selectFirst("option[selected]");

            actividades.add(Actividad.builder()
                    .id(id)
                    .descripcion(labelDesc  != null ? labelDesc.text().trim()  : "")
                    .horasAsignadas(labelHoras != null ? labelHoras.text().trim() : "0h")
                    .horasIntroducidas(selected != null ? selected.val() : "0")
                    .build());
        }

        // ── Horas introducidas — suma de selects ──────────────────────────────
        double totalHoras = actividades.stream()
                .mapToDouble(a -> {
                    try { return Double.parseDouble(a.getHorasIntroducidas()); }
                    catch (NumberFormatException e) { return 0.0; }
                })
                .sum();
        String horasIntroducidas = formatHoras(totalHoras);

        // ── Ausencia parcial ──────────────────────────────────────────────────
        String ausenciaParcial = "";
        Element selectAus = doc.getElementById("motiuAbsenciaParcial");
        if (selectAus != null) {
            Element selOpt = selectAus.selectFirst("option[selected]");
            if (selOpt != null) ausenciaParcial = selOpt.text().trim();
        }

        // ── Observaciones ─────────────────────────────────────────────────────
        Element textarea = doc.selectFirst("textarea[name='observacionsInutilsAlumneMaiOmplira']");
        String observaciones = textarea != null ? textarea.text().trim() : "";

        // ── Relleno ───────────────────────────────────────────────────────────
        boolean relleno = totalHoras > 0 || !ausenciaParcial.isBlank();

        return ActividadDTO.builder()
                .codAlumno(codAlumno)
                .nombreAlumno(nombreAlumno)
                .empresa(empresa)
                .centroDeTrabajo(centraTreball)
                .profesorTutor(profesorTutor)
                .estudio(estudio)
                .modalidadEnsenanza(modalidadEnsenanza)
                .movilidad(movilidad)
                .modalidadPresencial(modalidadPresencial)
                .periodoInicio(periodoInicio)
                .periodoFin(periodoFin)
                .fecha(fecha)
                .fechaTexto(fechaTexto)
                .horasMaximas(horasMaximas)
                .horasIntroducidas(horasIntroducidas)
                .relleno(relleno)
                .actividades(actividades)
                .ausenciaParcial(ausenciaParcial)
                .observaciones(observaciones)
                .urlAnterior(urlAnterior)
                .urlSiguiente(urlSiguiente)
                .build();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String formatHoras(double hm) {
        if (hm == 0) return "0H";
        int horas    = (int) hm;
        double resto = Math.round((hm - horas) * 100.0) / 100.0;
        String base  = horas > 0 ? horas + "H" : "";
        if (resto == 0.25)     return base + (horas > 0 ? " " : "") + "15MIN";
        else if (resto == 0.5) return base + (horas > 0 ? " " : "") + "30MIN";
        else if (resto == 0.75)return base + (horas > 0 ? " " : "") + "45MIN";
        return base.isBlank() ? "0H" : base;
    }

    private String extractLinkTitle(Document doc, String labelText) {
        for (Element label : doc.select("label.control-label")) {
            if (label.text().trim().equals(labelText)) {
                Element sibling = label.nextElementSibling();
                if (sibling != null) {
                    Element a = sibling.selectFirst("a");
                    return a != null ? a.attr("title").trim() : sibling.text().trim();
                }
            }
        }
        return "";
    }

    private String extractLabelSpan(Document doc, String labelText) {
        for (Element label : doc.select("label.control-label")) {
            if (label.text().trim().equals(labelText)) {
                Element sibling = label.nextElementSibling();
                if (sibling != null) {
                    Element span = sibling.selectFirst("span");
                    return span != null ? span.text().trim() : sibling.text().trim();
                }
            }
        }
        return "";
    }
}

