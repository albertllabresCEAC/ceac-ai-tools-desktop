package tools.ceac.ai.mcp.qbid.model.dto;

import java.util.List;

public class DetalleInformeDTO {

    private String conveniValoracioPk;
    private String codConvenio;
    private String codTemporal;
    private String periodoAcuerdo;
    private String periodoInforme;
    private String alumno;
    private String codAlumno;
    private String empresa;
    private String profesorTutor;
    private String horasTotales;
    private String cursSeleccio;          // valor del hidden input curs_seleccio (necesario para el POST de guardar)
    private boolean editable;           // false si los selects estÃ¡n disabled (bloqueado)
    private boolean firmadoTutor;       // false si aparece "(NO FIRMADO)" en el bloque de signatarios
    private List<String> diasNoGestionados;  // "DD/MM/YYYY: descripcion"
    private List<String> ausenciasInforme;   // "DD/MM/YYYY: motivo"
    private List<ActividadValoracion> actividades;
    private String observacionesAlumno;
    private String observacionesEmpresa;
    private String observacionesTutor;
    private String signatarioEmpresa;

    private DetalleInformeDTO() {}

    public String getConveniValoracioPk()              { return conveniValoracioPk; }
    public String getCodConvenio()                     { return codConvenio; }
    public String getCodTemporal()                     { return codTemporal; }
    public String getPeriodoAcuerdo()                  { return periodoAcuerdo; }
    public String getPeriodoInforme()                  { return periodoInforme; }
    public String getAlumno()                          { return alumno; }
    public String getCodAlumno()                       { return codAlumno; }
    public String getEmpresa()                         { return empresa; }
    public String getProfesorTutor()                   { return profesorTutor; }
    public String getHorasTotales()                    { return horasTotales; }
    public String getCursSeleccio()                      { return cursSeleccio; }
    public boolean isEditable()                        { return editable; }
    public boolean isFirmadoTutor()                    { return firmadoTutor; }
    public List<String> getDiasNoGestionados()         { return diasNoGestionados; }
    public List<String> getAusenciasInforme()          { return ausenciasInforme; }
    public List<ActividadValoracion> getActividades()  { return actividades; }
    public String getObservacionesAlumno()             { return observacionesAlumno; }
    public String getObservacionesEmpresa()            { return observacionesEmpresa; }
    public String getObservacionesTutor()              { return observacionesTutor; }
    public String getSignatarioEmpresa()               { return signatarioEmpresa; }

    public static Builder builder() { return new Builder(); }
    public static class Builder {
        private final DetalleInformeDTO o = new DetalleInformeDTO();
        public Builder conveniValoracioPk(String v)           { o.conveniValoracioPk = v; return this; }
        public Builder codConvenio(String v)                  { o.codConvenio = v; return this; }
        public Builder codTemporal(String v)                  { o.codTemporal = v; return this; }
        public Builder periodoAcuerdo(String v)               { o.periodoAcuerdo = v; return this; }
        public Builder periodoInforme(String v)               { o.periodoInforme = v; return this; }
        public Builder alumno(String v)                       { o.alumno = v; return this; }
        public Builder codAlumno(String v)                    { o.codAlumno = v; return this; }
        public Builder empresa(String v)                      { o.empresa = v; return this; }
        public Builder profesorTutor(String v)                { o.profesorTutor = v; return this; }
        public Builder horasTotales(String v)                 { o.horasTotales = v; return this; }
        public Builder cursSeleccio(String v)                 { o.cursSeleccio = v; return this; }
        public Builder editable(boolean v)                    { o.editable = v; return this; }
        public Builder firmadoTutor(boolean v)                { o.firmadoTutor = v; return this; }
        public Builder diasNoGestionados(List<String> v)      { o.diasNoGestionados = v; return this; }
        public Builder ausenciasInforme(List<String> v)       { o.ausenciasInforme = v; return this; }
        public Builder actividades(List<ActividadValoracion> v){ o.actividades = v; return this; }
        public Builder observacionesAlumno(String v)          { o.observacionesAlumno = v; return this; }
        public Builder observacionesEmpresa(String v)         { o.observacionesEmpresa = v; return this; }
        public Builder observacionesTutor(String v)           { o.observacionesTutor = v; return this; }
        public Builder signatarioEmpresa(String v)            { o.signatarioEmpresa = v; return this; }
        public DetalleInformeDTO build()                      { return o; }
    }

    // â”€â”€ ActividadValoracion â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    public static class ActividadValoracion {
        private String id;
        private String descripcion;
        private String horas;
        private String adecuacionTutor;
        private String valoracionEmpresa;

        private ActividadValoracion() {}
        public String getId()                { return id; }
        public String getDescripcion()       { return descripcion; }
        public String getHoras()             { return horas; }
        public String getAdecuacionTutor()   { return adecuacionTutor; }
        public String getValoracionEmpresa() { return valoracionEmpresa; }

        public static Builder builder() { return new Builder(); }
        public static class Builder {
            private final ActividadValoracion o = new ActividadValoracion();
            public Builder id(String v)                { o.id = v; return this; }
            public Builder descripcion(String v)       { o.descripcion = v; return this; }
            public Builder horas(String v)             { o.horas = v; return this; }
            public Builder adecuacionTutor(String v)   { o.adecuacionTutor = v; return this; }
            public Builder valoracionEmpresa(String v) { o.valoracionEmpresa = v; return this; }
            public ActividadValoracion build()         { return o; }
        }
    }
}
