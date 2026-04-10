package tools.ceac.ai.mcp.qbid.model.dto;

import java.util.List;

public class PlanActividadesDTO {

    // ── Campos a nivel de convenio (fuera de las tabs) ────────────────────────
    private String modalidadEnsenanza;
    private String movilidad;
    private String modalidadPresencial;
    private String centro;
    private String tutorDocente;
    private String alumno;
    private String estudio;
    private String empresa;
    private String centroDeTrabajo;

    // ── Campos a nivel de plan (dentro de la tab) ─────────────────────────────
    private String conveniPlaPk;
    private String ajutConveni;          // campo oculto del form (e.g. "1913;10267"); necesario para el POST de guardar
    private String periodo;
    private String tutorEmpresa;
    private String tutorResponsable;
    private String responsableCentreValue; // value del option seleccionado en responsable_centre_{pk}; necesario para el POST
    private String codPlaOrigenParamName;  // nombre completo del input autocomplete (e.g. "cod_pla_origen811947214"); necesario para el POST
    private String instalaciones;

    // ── Actividades ───────────────────────────────────────────────────────────
    private List<ItemPlanDTO> actividades;

    private PlanActividadesDTO() {}

    public String getModalidadEnsenanza()      { return modalidadEnsenanza; }
    public String getMovilidad()               { return movilidad; }
    public String getModalidadPresencial()     { return modalidadPresencial; }
    public String getCentro()                  { return centro; }
    public String getTutorDocente()            { return tutorDocente; }
    public String getAlumno()                  { return alumno; }
    public String getEstudio()                 { return estudio; }
    public String getEmpresa()                 { return empresa; }
    public String getCentroDeTrabajo()         { return centroDeTrabajo; }
    public String getConveniPlaPk()              { return conveniPlaPk; }
    public String getAjutConveni()             { return ajutConveni; }
    public String getPeriodo()                 { return periodo; }
    public String getTutorEmpresa()            { return tutorEmpresa; }
    public String getTutorResponsable()        { return tutorResponsable; }
    public String getResponsableCentreValue()  { return responsableCentreValue; }
    public String getCodPlaOrigenParamName()   { return codPlaOrigenParamName; }
    public String getInstalaciones()           { return instalaciones; }
    public List<ItemPlanDTO> getActividades()  { return actividades; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final PlanActividadesDTO o = new PlanActividadesDTO();
        public Builder modalidadEnsenanza(String v)      { o.modalidadEnsenanza = v; return this; }
        public Builder movilidad(String v)               { o.movilidad = v; return this; }
        public Builder modalidadPresencial(String v)     { o.modalidadPresencial = v; return this; }
        public Builder centro(String v)                  { o.centro = v; return this; }
        public Builder tutorDocente(String v)            { o.tutorDocente = v; return this; }
        public Builder alumno(String v)                  { o.alumno = v; return this; }
        public Builder estudio(String v)                 { o.estudio = v; return this; }
        public Builder empresa(String v)                 { o.empresa = v; return this; }
        public Builder centroDeTrabajo(String v)         { o.centroDeTrabajo = v; return this; }
        public Builder conveniPlaPk(String v)              { o.conveniPlaPk = v; return this; }
        public Builder ajutConveni(String v)             { o.ajutConveni = v; return this; }
        public Builder periodo(String v)                 { o.periodo = v; return this; }
        public Builder tutorEmpresa(String v)            { o.tutorEmpresa = v; return this; }
        public Builder tutorResponsable(String v)        { o.tutorResponsable = v; return this; }
        public Builder responsableCentreValue(String v)  { o.responsableCentreValue = v; return this; }
        public Builder codPlaOrigenParamName(String v)   { o.codPlaOrigenParamName = v; return this; }
        public Builder instalaciones(String v)           { o.instalaciones = v; return this; }
        public Builder actividades(List<ItemPlanDTO> v)  { o.actividades = v; return this; }
        public PlanActividadesDTO build()                { return o; }
    }
}
