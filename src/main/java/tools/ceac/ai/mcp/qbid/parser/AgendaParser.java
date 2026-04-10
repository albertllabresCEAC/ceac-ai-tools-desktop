package tools.ceac.ai.mcp.qbid.parser;

import tools.ceac.ai.mcp.qbid.model.dto.AgendaDTO;
import tools.ceac.ai.mcp.qbid.model.dto.AgendaDTO.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class AgendaParser {

    private static final String BASE_URL = "https://www.empresaiformacio.org/sBid";

    // Extrae días del JS: daysMonth[i] = '24-AgCellData';
    private static final Pattern P_DAYS     = Pattern.compile("daysMonth\\[\\w+\\]\\s*=\\s*'(\\d+)-(AgCell\\w+)'");
    // Extrae fecha del periodo en span label-info
    private static final Pattern P_PERIODO  = Pattern.compile("de\\s*(\\d{2}/\\d{2}/\\d{4})\\s*a\\s*(\\d{2}/\\d{2}/\\d{4})");
    // Extrae hash_code de URL
    private static final Pattern P_HASH     = Pattern.compile("hash_code=([a-f0-9]+)");
    // Extrae conveni_valoracio_pk
    private static final Pattern P_VPKK     = Pattern.compile("conveni_valoracio_pk=(\\d+)");
    // Extrae fecha de la URL de actividad diaria: data=YYYYMMDD
    private static final Pattern P_DATA     = Pattern.compile("data=(\\d{8})");
    // Extrae fechas de ausencias: "28/01/2026 a 28/01/2026:"
    private static final Pattern P_AUSENCIA = Pattern.compile("(\\d{2}/\\d{2}/\\d{4})\\s*a\\s*(\\d{2}/\\d{2}/\\d{4}):");
    private static final Pattern P_AID      = Pattern.compile("aid=(\\d+)");

    public AgendaDTO parseAgenda(String html) {
        Document doc = Jsoup.parse(html);

        // ── Cabecera ─────────────────────────────────────────────────────────
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

        // ── Periodo ──────────────────────────────────────────────────────────
        String periodoInicio = "";
        String periodoFin    = "";
        Element periodoSpan  = doc.selectFirst("span.label.label-info");
        if (periodoSpan != null) {
            Matcher m = P_PERIODO.matcher(periodoSpan.text());
            if (m.find()) { periodoInicio = m.group(1); periodoFin = m.group(2); }
        }

        // ── Horas ────────────────────────────────────────────────────────────
        String horasInformadas  = extractLabelSpan(doc, "Horas informadas:");
        String horasConsignadas = extractLabelSpan(doc, "Horas consignadas:");
        String horasValidadas   = extractLabelSpan(doc, "Horas validadas:");

        // ── Calendario — extraído del JS ──────────────────────────────────────
        List<DiaCalendario> calendario = parseCalendario(html);

        // ── Ausencias ─────────────────────────────────────────────────────────
        List<Ausencia> ausencias = parseAusencias(doc);

        // ── Informes periódicos ───────────────────────────────────────────────
        List<InformePeriodico> informes = parseInformes(doc);

        // ── Día actual — tarea del día en #tablaTasquesDia ────────────────────
        DiaActual diaActual = parseDiaActual(doc);

        return AgendaDTO.builder()
                .codAlumno(codAlumno)
                .nombreAlumno(nombreAlumno)
                .empresa(empresa)
                .centroDeTrabajo(centraTreball)
                .profesorTutor(profesorTutor)
                .estudio(estudio)
                .periodoInicio(periodoInicio)
                .periodoFin(periodoFin)
                .horasInformadas(horasInformadas)
                .horasConsignadas(horasConsignadas)
                .horasValidadas(horasValidadas)
                .calendario(calendario)
                .ausencias(ausencias)
                .informes(informes)
                .diaActual(diaActual)
                .build();
    }

    // ── Calendario ────────────────────────────────────────────────────────────

    private List<DiaCalendario> parseCalendario(String html) {
        List<DiaCalendario> result = new ArrayList<>();
        Matcher m = P_DAYS.matcher(html);
        while (m.find()) {
            int dia         = Integer.parseInt(m.group(1));
            String cssClass = m.group(2);
            String estado   = cssClassToEstado(cssClass);
            result.add(DiaCalendario.builder().dia(dia).estado(estado).build());
        }
        // Ordenar por día
        result.sort((a, b) -> Integer.compare(a.getDia(), b.getDia()));
        return result;
    }

    private String cssClassToEstado(String cssClass) {
        return switch (cssClass) {
            case "AgCellData"   -> "PENDIENTE";   // día laborable sin actividad
            case "AgCellInfo"   -> "INFORMADO";   // actividad informada
            case "AgCellBaixa"  -> "AUSENCIA";    // ausencia registrada
            case "AgCellFesta"  -> "FESTIVO";
            case "AgCellMedic"  -> "MEDICO";
            default             -> cssClass;
        };
    }

    // ── Ausencias ─────────────────────────────────────────────────────────────

    private List<Ausencia> parseAusencias(Document doc) {
        List<Ausencia> result = new ArrayList<>();
        Element collapse1 = doc.getElementById("collapse1");
        if (collapse1 == null) return result;

        for (Element li : collapse1.select("li")) {
            String texto = li.text().trim();
            Matcher m    = P_AUSENCIA.matcher(texto);
            if (!m.find()) continue;

            // El motivo está en el <strong>
            String motivo = "";
            Element strong = li.selectFirst("strong");
            if (strong != null) motivo = strong.text().trim();

            result.add(Ausencia.builder()
                    .fechaDesde(m.group(1))
                    .fechaHasta(m.group(2))
                    .motivo(motivo)
                    .build());
        }
        return result;
    }

    // ── Informes periódicos ───────────────────────────────────────────────────

    private List<InformePeriodico> parseInformes(Document doc) {
        List<InformePeriodico> result = new ArrayList<>();
        Element collapse2 = doc.getElementById("collapse2");
        if (collapse2 == null) return result;

        for (Element a : collapse2.select("a[href]")) {
            String texto = a.text().trim();
            String href  = a.attr("href");
            String style = a.attr("style");

            Matcher mF = P_AUSENCIA.matcher(texto);
            if (!mF.find()) continue;

            String estado = colorToEstado(style);

            Matcher mVpk  = P_VPKK.matcher(href);
            Matcher mHash = P_HASH.matcher(href);

            result.add(InformePeriodico.builder()
                    .fechaDesde(mF.group(1))
                    .fechaHasta(mF.group(2))
                    .estado(estado)
                    .conveniValoracioPk(mVpk.find()  ? mVpk.group(1)  : "")
                    .hashCode(mHash.find()            ? mHash.group(1) : "")
                    .url(BASE_URL + href)
                    .build());
        }
        return result;
    }

    private String colorToEstado(String style) {
        if (style == null) return "DESCONOCIDO";
        if (style.contains("green"))   return "FIRMADO";
        if (style.contains("#00309C")) return "PENDIENTE_FIRMA";
        if (style.contains("red"))     return "NO_GESTIONADO";
        if (style.contains("orange"))  return "PENDIENTE_VALIDACION";
        return "DESCONOCIDO";
    }

    // ── Día actual ────────────────────────────────────────────────────────────

    private DiaActual parseDiaActual(Document doc) {
        Element tablaTasques = doc.getElementById("tablaTasquesDia");
        if (tablaTasques == null) return null;

        Element a = tablaTasques.selectFirst("a[href]");
        if (a == null) return null;

        String href        = a.attr("href");
        String descripcion = a.text().trim();

        Matcher mData = P_DATA.matcher(href);
        Matcher mHash = P_HASH.matcher(href);

        return DiaActual.builder()
                .fecha(mData.find()       ? mData.group(1) : "")
                .descripcion(descripcion)
                .hashCode(mHash.find()    ? mHash.group(1) : "")
                .url(BASE_URL + href)
                .build();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String extractLinkTitle(Document doc, String labelText) {
        for (Element label : doc.select("label.control-label")) {
            if (label.text().trim().equals(labelText)) {
                Element sibling = label.nextElementSibling();
                if (sibling != null) {
                    Element a = sibling.selectFirst("a");
                    if (a != null) return a.attr("title").trim();
                    return sibling.text().trim();
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

