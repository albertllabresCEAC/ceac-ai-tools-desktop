package tools.ceac.ai.mcp.qbid.model.dto;

import java.util.List;

/**
 * Request para guardar o validar el plan de actividades de un convenio.
 *
 * <p>Los identificadores {@code codConvenio}, {@code codTemporal} y
 * {@code conveniPlaPk} se obtienen de {@code verPlanActividades}
 * (PlanActividadesDTO.conveniPlaPk).
 *
 * <p>{@code actividadesSeleccionadas}: lista de IDs que deben quedar marcados
 * en el plan. Deben incluirse explicitamente tanto los grupos raiz (nivel 0)
 * como las subactividades (nivel 1) que se quieran conservar seleccionadas.
 * Los IDs se obtienen de PlanActividadesDTO.actividades[].id.
 *
 * <p>{@code responsableEmpresa}: nombre del tutor/a empresa responsable.
 * Si no se indica, se mantiene el valor actual.
 *
 * <p>{@code recursosActivitat}: descripcion de instalaciones y equipamientos
 * disponibles en el centro de trabajo. Si no se indica, se mantiene el valor actual.
 */
public class GuardarPlanRequest {

    private String codConvenio;
    private String codTemporal;
    private String newSystem;            // "" para MODGEN, "547" u otro para BID
    private String conveniPlaPk;         // PK del plan a guardar (PlanActividadesDTO.conveniPlaPk)
    private List<String> actividadesSeleccionadas; // IDs de actividades a mantener seleccionadas
    private String responsableEmpresa;   // nombre del tutor/a empresa (null = mantener actual)
    private String recursosActivitat;    // instalaciones/equipamientos (null = mantener actual)

    public String getCodConvenio()                           { return codConvenio; }
    public void   setCodConvenio(String v)                   { this.codConvenio = v; }
    public String getCodTemporal()                           { return codTemporal; }
    public void   setCodTemporal(String v)                   { this.codTemporal = v; }
    public String getNewSystem()                             { return newSystem; }
    public void   setNewSystem(String v)                     { this.newSystem = v; }
    public String getConveniPlaPk()                          { return conveniPlaPk; }
    public void   setConveniPlaPk(String v)                  { this.conveniPlaPk = v; }
    public List<String> getActividadesSeleccionadas()        { return actividadesSeleccionadas; }
    public void   setActividadesSeleccionadas(List<String> v){ this.actividadesSeleccionadas = v; }
    public String getResponsableEmpresa()                    { return responsableEmpresa; }
    public void   setResponsableEmpresa(String v)            { this.responsableEmpresa = v; }
    public String getRecursosActivitat()                     { return recursosActivitat; }
    public void   setRecursosActivitat(String v)             { this.recursosActivitat = v; }
}

