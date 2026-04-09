package tools.ceac.ai.mcp.qbid.model.dto;

import java.util.List;

public class ActividadDTO {

    // â”€â”€ Cabecera â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    private String codAlumno;
    private String nombreAlumno;
    private String empresa;
    private String centroDeTrabajo;
    private String profesorTutor;
    private String estudio;
    private String modalidadEnsenanza;
    private String movilidad;
    private String modalidadPresencial;
    private String periodoInicio;
    private String periodoFin;

    // â”€â”€ Actividad â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    private String fecha;
    private String fechaTexto;
    private String horasMaximas;
    private String horasIntroducidas;
    private boolean relleno;
    private List<Actividad> actividades;
    private String ausenciaParcial;
    private String observaciones;
    private String urlAnterior;
    private String urlSiguiente;

    private ActividadDTO() {}

    public String getCodAlumno()           { return codAlumno; }
    public String getNombreAlumno()        { return nombreAlumno; }
    public String getEmpresa()             { return empresa; }
    public String getCentroDeTrabajo()     { return centroDeTrabajo; }
    public String getProfesorTutor()       { return profesorTutor; }
    public String getEstudio()             { return estudio; }
    public String getModalidadEnsenanza()  { return modalidadEnsenanza; }
    public String getMovilidad()           { return movilidad; }
    public String getModalidadPresencial() { return modalidadPresencial; }
    public String getPeriodoInicio()       { return periodoInicio; }
    public String getPeriodoFin()          { return periodoFin; }
    public String getFecha()               { return fecha; }
    public String getFechaTexto()          { return fechaTexto; }
    public String getHorasMaximas()        { return horasMaximas; }
    public String getHorasIntroducidas()   { return horasIntroducidas; }
    public boolean isRelleno()             { return relleno; }
    public List<Actividad> getActividades() { return actividades; }
    public String getAusenciaParcial()     { return ausenciaParcial; }
    public String getObservaciones()       { return observaciones; }
    public String getUrlAnterior()         { return urlAnterior; }
    public String getUrlSiguiente()        { return urlSiguiente; }

    public static Builder builder() { return new Builder(); }
    public static class Builder {
        private final ActividadDTO o = new ActividadDTO();
        public Builder codAlumno(String v)           { o.codAlumno = v; return this; }
        public Builder nombreAlumno(String v)        { o.nombreAlumno = v; return this; }
        public Builder empresa(String v)             { o.empresa = v; return this; }
        public Builder centroDeTrabajo(String v)     { o.centroDeTrabajo = v; return this; }
        public Builder profesorTutor(String v)       { o.profesorTutor = v; return this; }
        public Builder estudio(String v)             { o.estudio = v; return this; }
        public Builder modalidadEnsenanza(String v)  { o.modalidadEnsenanza = v; return this; }
        public Builder movilidad(String v)           { o.movilidad = v; return this; }
        public Builder modalidadPresencial(String v) { o.modalidadPresencial = v; return this; }
        public Builder periodoInicio(String v)       { o.periodoInicio = v; return this; }
        public Builder periodoFin(String v)          { o.periodoFin = v; return this; }
        public Builder fecha(String v)               { o.fecha = v; return this; }
        public Builder fechaTexto(String v)          { o.fechaTexto = v; return this; }
        public Builder horasMaximas(String v)        { o.horasMaximas = v; return this; }
        public Builder horasIntroducidas(String v)   { o.horasIntroducidas = v; return this; }
        public Builder relleno(boolean v)            { o.relleno = v; return this; }
        public Builder actividades(List<Actividad> v){ o.actividades = v; return this; }
        public Builder ausenciaParcial(String v)     { o.ausenciaParcial = v; return this; }
        public Builder observaciones(String v)       { o.observaciones = v; return this; }
        public Builder urlAnterior(String v)         { o.urlAnterior = v; return this; }
        public Builder urlSiguiente(String v)        { o.urlSiguiente = v; return this; }
        public ActividadDTO build()                  { return o; }
    }

    // â”€â”€ Actividad â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    public static class Actividad {
        private String id;
        private String descripcion;
        private String horasAsignadas;
        private String horasIntroducidas;

        private Actividad() {}
        public String getId()                { return id; }
        public String getDescripcion()       { return descripcion; }
        public String getHorasAsignadas()    { return horasAsignadas; }
        public String getHorasIntroducidas() { return horasIntroducidas; }

        public static Builder builder() { return new Builder(); }
        public static class Builder {
            private final Actividad o = new Actividad();
            public Builder id(String v)                { o.id = v; return this; }
            public Builder descripcion(String v)       { o.descripcion = v; return this; }
            public Builder horasAsignadas(String v)    { o.horasAsignadas = v; return this; }
            public Builder horasIntroducidas(String v) { o.horasIntroducidas = v; return this; }
            public Actividad build()                   { return o; }
        }
    }
}

