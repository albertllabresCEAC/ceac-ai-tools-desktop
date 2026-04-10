package tools.ceac.ai.mcp.qbid.service;

import tools.ceac.ai.mcp.qbid.model.dto.GuardarPlanRequest;
import tools.ceac.ai.mcp.qbid.model.dto.ItemPlanDTO;
import tools.ceac.ai.mcp.qbid.model.dto.PlanActividadesDTO;
import tools.ceac.ai.mcp.qbid.parser.PlanActividadesParser;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class PlanActividadesService {

    private final QbidHttpService http;
    private final QbidUrls urls;
    private final PlanActividadesParser parser;

    public PlanActividadesService(QbidHttpService http, QbidUrls urls, PlanActividadesParser parser) {
        this.http   = http;
        this.urls   = urls;
        this.parser = parser;
    }

    public List<PlanActividadesDTO> getPlan(String jsessionid,
                                            String codConvenio,
                                            String codTemporal,
                                            String newSystem) throws Exception {
        String html = http.get(urls.planActividades(codConvenio, codTemporal, newSystem), jsessionid);
        return parser.parsePlanes(html);
    }

    public List<PlanActividadesDTO> guardarPlan(String jsessionid,
                                                GuardarPlanRequest req) throws Exception {
        return postPlan(jsessionid, req, false);
    }

    public List<PlanActividadesDTO> validarPlan(String jsessionid,
                                                GuardarPlanRequest req) throws Exception {
        return postPlan(jsessionid, req, true);
    }

    private List<PlanActividadesDTO> postPlan(String jsessionid,
                                              GuardarPlanRequest req,
                                              boolean validate) throws Exception {
        String ns = req.getNewSystem() != null ? req.getNewSystem() : "";

        // GET previo: extraer estado actual del formulario (ajutConveni, actividades, responsable_centre...)
        List<PlanActividadesDTO> planes = getPlan(jsessionid, req.getCodConvenio(), req.getCodTemporal(), ns);

        PlanActividadesDTO plan = planes.stream()
                .filter(p -> p.getConveniPlaPk().equals(req.getConveniPlaPk()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Plan no encontrado: conveniPlaPk=" + req.getConveniPlaPk()));

        int pestanaIndex = IntStream.range(0, planes.size())
                .filter(i -> planes.get(i).getConveniPlaPk().equals(req.getConveniPlaPk()))
                .findFirst().orElse(0);

        // Set para lookup O(1)
        Set<String> seleccionadasSet = req.getActividadesSeleccionadas() != null
                ? new HashSet<>(req.getActividadesSeleccionadas())
                : Collections.emptySet();

        // activitatsSelected = todos los IDs del plan excepto los editables que el usuario desmarca
        String activitatsSelected = plan.getActividades().stream()
                .filter(a -> !a.isEditable() || seleccionadasSet.contains(a.getId()))
                .map(ItemPlanDTO::getId)
                .collect(Collectors.joining(","));

        // Construir el body con soporte multi-valor para activitatFormativa[]
        List<String> parts = new ArrayList<>();
        parts.add(p("moduleaction",             "save"));
        parts.add(p("codi_conveni",             req.getCodConvenio()));
        parts.add(p("codi_conveni_provisional", req.getCodTemporal()));
        parts.add(p("preomplir",                "NO"));
        parts.add(p("conveni_pla_pk",           plan.getConveniPlaPk()));
        parts.add(p("activitatsSelected",       activitatsSelected));
        parts.add(p("validate",                 String.valueOf(validate)));
        parts.add(p("codActivitatUF",           ""));
        parts.add(p("codActivitatOpcUF",        ""));
        parts.add(p("ajutConveni",              plan.getAjutConveni() != null ? plan.getAjutConveni() : ""));
        parts.add(p("pestana_plan",             String.valueOf(pestanaIndex)));
        parts.add(p("responsable_empresa_" + plan.getConveniPlaPk(),
                req.getResponsableEmpresa() != null ? req.getResponsableEmpresa() : plan.getTutorEmpresa()));
        parts.add(p("responsable_centre_" + plan.getConveniPlaPk(),
                plan.getResponsableCentreValue() != null ? plan.getResponsableCentreValue() : ""));
        parts.add(p("cod_pla_origen",           ""));
        parts.add(p(plan.getCodPlaOrigenParamName(), ""));
        parts.add(p("activitats_opc_size",      "0"));
        parts.add(p("activitats_size",          ""));

        // activitatFormativa[] — solo actividades editables que el usuario mantiene seleccionadas
        for (ItemPlanDTO act : plan.getActividades()) {
            if (act.isEditable() && seleccionadasSet.contains(act.getId())) {
                parts.add(p("activitatFormativa[]", act.getId()));
            }
        }

        parts.add(p("recursosActivitat",
                req.getRecursosActivitat() != null ? req.getRecursosActivitat()
                                                   : (plan.getInstalaciones() != null ? plan.getInstalaciones() : "")));

        String body = String.join("&", parts);
        http.postRaw(urls.guardarPlanActividades(), body, jsessionid);

        // GET final: devolver el plan actualizado
        return getPlan(jsessionid, req.getCodConvenio(), req.getCodTemporal(), ns);
    }

    private String p(String key, String value) {
        return URLEncoder.encode(key, StandardCharsets.UTF_8)
                + "=" + URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
