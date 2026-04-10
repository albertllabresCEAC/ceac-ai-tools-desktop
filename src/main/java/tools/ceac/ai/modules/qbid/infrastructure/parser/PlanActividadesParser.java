package tools.ceac.ai.modules.qbid.infrastructure.parser;

import tools.ceac.ai.modules.qbid.domain.model.ItemPlanDTO;
import tools.ceac.ai.modules.qbid.domain.model.PlanActividadesDTO;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class PlanActividadesParser {

    private static final Pattern P_PADDING = Pattern.compile("padding-left:\\s*(\\d+)px");

    public List<PlanActividadesDTO> parsePlanes(String html) {
        Document doc = Jsoup.parse(html);
        List<PlanActividadesDTO> result = new ArrayList<>();

        // â”€â”€ Campos a nivel de convenio (fuera de las tabs, comunes a todos los planes) â”€â”€

        // Modalidad enseÃ±anza, movilidad y presencial â€” span.label-info en cada fila
        String modalidadEnsenanza  = extractLabelInfo(doc, "Modalidad enseÃ±anza:");
        String movilidad           = extractLabelInfo(doc, "Mobilidad:");
        String modalidadPresencial = extractLabelInfo(doc, "Modalidad presencial:");

        // Centro, tutor, alumno, estudio, empresa, centro de trabajo â€” link dentro del siguiente div
        String centro         = extractLinkLabel(doc, "Centro:");
        String tutorDocente   = extractLinkLabel(doc, "Profesor/Tutor/a:");
        String alumno         = extractLinkLabel(doc, "Alumno/a:");
        String estudio        = extractLinkLabel(doc, "Estudio:");
        String empresa        = extractLinkLabel(doc, "Empresa:");
        String centroDeTrabajo = extractLinkLabel(doc, "Centro de Trabajo:");

        // â”€â”€ ajutConveni â€” campo oculto global del formulario â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        String ajutConveni = "";
        Element ajutInput = doc.selectFirst("input[name='ajutConveni']");
        if (ajutInput != null) ajutConveni = ajutInput.val();

        // â”€â”€ Un plan por cada tab #pap_{pk} â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

        for (Element tabPanel : doc.select("div[id^='pap_']")) {
            String pk = tabPanel.id().replaceFirst("pap_", "");

            // Periodo â€” del anchor del tab correspondiente
            String periodo = "";
            Element tabLink = doc.selectFirst("a[href='#pap_" + pk + "']");
            if (tabLink != null) periodo = tabLink.text().trim();

            // Tutor empresa â€” input responsable_empresa_{pk}
            String tutorEmpresa = "";
            Element tutorInput = tabPanel.selectFirst("input[name^='responsable_empresa_']");
            if (tutorInput != null) tutorEmpresa = tutorInput.val().trim();

            // Tutor/a responsable â€” opciÃ³n seleccionada del select responsable_centre_{pk}
            // El texto tiene el prefijo "Professorat: " que se elimina
            String tutorResponsable = "";
            String responsableCentreValue = "";
            Element responsableSelect = tabPanel.selectFirst("select[name^='responsable_centre_']");
            if (responsableSelect != null) {
                Element selected = responsableSelect.selectFirst("option[selected]");
                if (selected != null) {
                    tutorResponsable = selected.text().trim()
                            .replaceFirst("^Professorat:\\s*", "");
                    responsableCentreValue = selected.attr("value");
                }
            }

            // Nombre del input autocomplete cod_pla_origen{helperId} â€” necesario para el POST
            String codPlaOrigenParamName = "cod_pla_origen";
            for (Element inp : tabPanel.select("input[name^='cod_pla_origen']")) {
                String n = inp.attr("name");
                if (n.length() > "cod_pla_origen".length()) {
                    codPlaOrigenParamName = n;
                    break;
                }
            }

            // Instalaciones y equipamientos â€” textarea recursosActivitat
            String instalaciones = "";
            Element textarea = tabPanel.selectFirst("textarea[name='recursosActivitat']");
            if (textarea != null) instalaciones = textarea.text().trim();

            // â”€â”€ Actividades â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

            List<ItemPlanDTO> actividades = new ArrayList<>();
            for (Element actDiv : tabPanel.select("div.activitatClass")) {
                String rawId = actDiv.id().replaceFirst("activitat", "");

                // Nivel jerÃ¡rquico desde padding-left (0px=raÃ­z, 100px=nivel1, 200px=nivel2...)
                int nivel = 0;
                Matcher m = P_PADDING.matcher(actDiv.attr("style"));
                if (m.find()) nivel = Integer.parseInt(m.group(1)) / 100;

                // parentId desde el hidden input dentro del div
                String parentId = "";
                Element parentInput = actDiv.selectFirst("input[id^='activitat_parent_']");
                if (parentInput != null) parentId = parentInput.val().trim();

                // Estado del checkbox
                Element chk = actDiv.selectFirst("input[type=checkbox]");
                boolean seleccionada = chk != null && chk.hasAttr("checked");
                boolean editable     = chk != null && !chk.hasAttr("disabled");

                // Texto descriptivo del label
                String texto = "";
                Element label = actDiv.selectFirst("label");
                if (label != null) texto = label.text().trim();

                actividades.add(ItemPlanDTO.builder()
                        .id(rawId)
                        .parentId(parentId)
                        .nivel(nivel)
                        .texto(texto)
                        .seleccionada(seleccionada)
                        .editable(editable)
                        .build());
            }

            result.add(PlanActividadesDTO.builder()
                    .modalidadEnsenanza(modalidadEnsenanza)
                    .movilidad(movilidad)
                    .modalidadPresencial(modalidadPresencial)
                    .centro(centro)
                    .tutorDocente(tutorDocente)
                    .alumno(alumno)
                    .estudio(estudio)
                    .empresa(empresa)
                    .centroDeTrabajo(centroDeTrabajo)
                    .conveniPlaPk(pk)
                    .ajutConveni(ajutConveni)
                    .periodo(periodo)
                    .tutorEmpresa(tutorEmpresa)
                    .tutorResponsable(tutorResponsable)
                    .responsableCentreValue(responsableCentreValue)
                    .codPlaOrigenParamName(codPlaOrigenParamName)
                    .instalaciones(instalaciones)
                    .actividades(actividades)
                    .build());
        }
        return result;
    }

    // â”€â”€ Helpers â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    /**
     * Busca un label.control-label con ese texto y devuelve el texto del
     * span.label-info en el siguiente sibling (usado para modalidad, movilidad...).
     */
    private String extractLabelInfo(Document doc, String labelText) {
        for (Element label : doc.select("label.control-label")) {
            if (label.text().trim().equals(labelText)) {
                Element sibling = label.nextElementSibling();
                if (sibling != null) {
                    Element span = sibling.selectFirst("span.label-info");
                    if (span != null) return span.text().trim();
                }
            }
        }
        return "";
    }

    /**
     * Busca un label.control-label con ese texto y devuelve el texto del
     * primer enlace en el siguiente sibling (usado para centro, alumno, empresa...).
     */
    private String extractLinkLabel(Document doc, String labelText) {
        for (Element label : doc.select("label.control-label")) {
            if (label.text().trim().equals(labelText)) {
                Element sibling = label.nextElementSibling();
                if (sibling != null) {
                    Element a = sibling.selectFirst("a");
                    if (a != null) return a.text().trim();
                }
            }
        }
        return "";
    }
}


