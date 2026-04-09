package tools.ceac.ai.mcp.qbid.parser;

import tools.ceac.ai.mcp.qbid.model.dto.DetalleInformeDTO;
import tools.ceac.ai.mcp.qbid.model.dto.DetalleInformeDTO.ActividadValoracion;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class InformePeriodicoParser {

    private static final Pattern P_PERIODO = Pattern.compile("de\\s*(\\d{2}/\\d{2}/\\d{4})\\s*a\\s*(\\d{2}/\\d{2}/\\d{4})");
    private static final Pattern P_AID     = Pattern.compile("aid=(\\d+)");
    private static final Pattern P_HORAS   = Pattern.compile("(\\d+h)");

    public DetalleInformeDTO parse(String html) {
        Document doc = Jsoup.parse(html);

        // â”€â”€ IDs del formulario â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        String conveniValoracioPk = inputVal(doc, "conveni_valoracio_pk");
        String codConvenio        = inputVal(doc, "codi_conveni");
        String codTemporal        = inputVal(doc, "codi_conveni_provisional");
        String cursSeleccio       = inputVal(doc, "curs_seleccio");

        // â”€â”€ Alumno â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        String codAlumno = "";
        String alumno    = "";
        Element alumnoLink = doc.selectFirst("a[href*='aid=']");
        if (alumnoLink != null) {
            Matcher m = P_AID.matcher(alumnoLink.attr("href"));
            if (m.find()) codAlumno = m.group(1);
            alumno = alumnoLink.attr("title").trim();
        }

        // â”€â”€ Empresa y tutor â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        String empresa       = extractLinkTitle(doc, "Empresa:");
        String profesorTutor = extractLinkTitle(doc, "Profesor/Tutor/a:");

        // â”€â”€ Periodo del acuerdo â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        String periodoAcuerdo = "";
        for (Element label : doc.select("label.control-label")) {
            if (label.text().trim().equals("Periodo del acuerdo:")) {
                Element div = label.nextElementSibling();
                if (div != null) {
                    Element p = div.selectFirst("p.form-control-static");
                    if (p != null) periodoAcuerdo = p.text().trim();
                }
                break;
            }
        }

        // â”€â”€ Periodo del informe (en el panel-heading de "Resumen de horas") â”€â”€â”€
        String periodoInforme = "";
        for (Element heading : doc.select("div.panel-heading")) {
            if (heading.text().contains("Resumen")) {
                Matcher m = P_PERIODO.matcher(heading.text());
                if (m.find()) periodoInforme = "de " + m.group(1) + " a " + m.group(2);
                break;
            }
        }

        // â”€â”€ Horas totales â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        String horasTotales = "";
        Element alertInfo = doc.selectFirst("div.alert.alert-info strong");
        if (alertInfo != null) {
            Matcher m = P_HORAS.matcher(alertInfo.text());
            if (m.find()) horasTotales = m.group(1);
        }

        // â”€â”€ Editable: false si los selects estÃ¡n disabled (informe bloqueado) â”€
        boolean editable = doc.selectFirst("select[disabled]") == null;

        // â”€â”€ Firmado por el tutor: true si NO aparece "(NO FIRMADO)" â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        boolean firmadoTutor = !doc.text().contains("NO FIRMADO");

        // â”€â”€ DÃ­as no gestionados â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        List<String> diasNoGestionados = parseListaAlert(doc, "alert-danger");

        // â”€â”€ Ausencias del informe â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        List<String> ausenciasInforme = parseListaAlert(doc, "alert-warning");

        // â”€â”€ Actividades â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        List<ActividadValoracion> actividades = parseActividades(doc);

        // â”€â”€ Observaciones â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        String obsAlumno  = textareaVal(doc, "obs_alumne");
        String obsEmpresa = textareaVal(doc, "obs_empresa");
        String obsTutor   = textareaVal(doc, "obs_tutor");

        // â”€â”€ Signatario empresa â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        String signatario = "";
        Element sigInput = doc.selectFirst("input[name='signatari']");
        if (sigInput != null) signatario = sigInput.val().trim();

        return DetalleInformeDTO.builder()
                .conveniValoracioPk(conveniValoracioPk)
                .codConvenio(codConvenio)
                .codTemporal(codTemporal)
                .cursSeleccio(cursSeleccio)
                .periodoAcuerdo(periodoAcuerdo)
                .periodoInforme(periodoInforme)
                .alumno(alumno)
                .codAlumno(codAlumno)
                .empresa(empresa)
                .profesorTutor(profesorTutor)
                .horasTotales(horasTotales)
                .editable(editable)
                .firmadoTutor(firmadoTutor)
                .diasNoGestionados(diasNoGestionados)
                .ausenciasInforme(ausenciasInforme)
                .actividades(actividades)
                .observacionesAlumno(obsAlumno)
                .observacionesEmpresa(obsEmpresa)
                .observacionesTutor(obsTutor)
                .signatarioEmpresa(signatario)
                .build();
    }

    // â”€â”€ Actividades â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    private List<ActividadValoracion> parseActividades(Document doc) {
        List<ActividadValoracion> result = new ArrayList<>();

        for (Element row : doc.select("div.row[id^='activitat']")) {
            // Solo filas con valoraciÃ³n (tienen select inp_XXXXX) â€” omite cabeceras de grupo
            Element selectTutor = row.selectFirst("select[name^='inp_']:not([name^='inp_extra_'])");
            if (selectTutor == null) continue;

            String id = row.id().replace("activitat", "");

            Element descLabel = row.selectFirst(".activitatInfo label.control-label");
            String descripcion = descLabel != null ? descLabel.text().trim() : "";

            Element horasSpan = row.selectFirst("span.label.label-info");
            String horas = horasSpan != null ? horasSpan.text().trim() : "0h";

            String adecuacionTutor = selectedOptionText(selectTutor);

            Element selectEmpresa = row.selectFirst("select[name^='inp_extra_']");
            String valoracionEmpresa = selectedOptionText(selectEmpresa);

            result.add(ActividadValoracion.builder()
                    .id(id)
                    .descripcion(descripcion)
                    .horas(horas)
                    .adecuacionTutor(adecuacionTutor)
                    .valoracionEmpresa(valoracionEmpresa)
                    .build());
        }
        return result;
    }

    // â”€â”€ Listas de alertas (dÃ­as no gestionados / ausencias) â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    private List<String> parseListaAlert(Document doc, String alertClass) {
        List<String> result = new ArrayList<>();
        Element alert = doc.selectFirst("div.alert." + alertClass);
        if (alert == null) return result;
        for (Element li : alert.select("li")) {
            String text = li.text().trim();
            if (!text.isEmpty()) result.add(text);
        }
        return result;
    }

    // â”€â”€ Helpers â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    private String selectedOptionText(Element select) {
        if (select == null) return "";
        Element selected = select.selectFirst("option[selected]");
        return selected != null ? selected.text().trim() : "";
    }

    private String inputVal(Document doc, String name) {
        Element el = doc.selectFirst("input[name='" + name + "']");
        return el != null ? el.val() : "";
    }

    private String textareaVal(Document doc, String id) {
        Element el = doc.getElementById(id);
        return el != null ? el.text().trim() : "";
    }

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
}
