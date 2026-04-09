package tools.ceac.ai.mcp.qbid.model.dto;

public class ConvenioDTO {
    private String alumno;
    private String codAlumno;
    private String estudio;
    private String estado;
    private String codConvenio;
    private String codTemporal;
    private String newSystem;
    private String codCuaderno;
    private String plan;

    private ConvenioDTO() {}

    public String getAlumno()      { return alumno; }
    public String getCodAlumno()   { return codAlumno; }
    public String getEstudio()     { return estudio; }
    public String getEstado()      { return estado; }
    public String getCodConvenio() { return codConvenio; }
    public String getCodTemporal() { return codTemporal; }
    public String getNewSystem()   { return newSystem; }
    public String getCodCuaderno() { return codCuaderno; }
    public String getPlan()        { return plan; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final ConvenioDTO o = new ConvenioDTO();
        public Builder alumno(String v)      { o.alumno = v; return this; }
        public Builder codAlumno(String v)   { o.codAlumno = v; return this; }
        public Builder estudio(String v)     { o.estudio = v; return this; }
        public Builder estado(String v)      { o.estado = v; return this; }
        public Builder codConvenio(String v) { o.codConvenio = v; return this; }
        public Builder codTemporal(String v) { o.codTemporal = v; return this; }
        public Builder newSystem(String v)   { o.newSystem = v; return this; }
        public Builder codCuaderno(String v) { o.codCuaderno = v; return this; }
        public Builder plan(String v)        { o.plan = v; return this; }
        public ConvenioDTO build()           { return o; }
    }
}

