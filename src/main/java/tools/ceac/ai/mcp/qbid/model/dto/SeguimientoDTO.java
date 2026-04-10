package tools.ceac.ai.mcp.qbid.model.dto;

import java.util.List;

public class SeguimientoDTO {

    private Cabecera cabecera;
    private SeguimientoAlumno seguimientoAlumno;
    private Valoracion valoracion;
    private Homologacion homologacion;
    private List<Documento> documentos;

    private SeguimientoDTO() {}

    public Cabecera getCabecera()                   { return cabecera; }
    public SeguimientoAlumno getSeguimientoAlumno() { return seguimientoAlumno; }
    public Valoracion getValoracion()               { return valoracion; }
    public Homologacion getHomologacion()           { return homologacion; }
    public List<Documento> getDocumentos()          { return documentos; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final SeguimientoDTO o = new SeguimientoDTO();
        public Builder cabecera(Cabecera v)                   { o.cabecera = v; return this; }
        public Builder seguimientoAlumno(SeguimientoAlumno v) { o.seguimientoAlumno = v; return this; }
        public Builder valoracion(Valoracion v)               { o.valoracion = v; return this; }
        public Builder homologacion(Homologacion v)           { o.homologacion = v; return this; }
        public Builder documentos(List<Documento> v)          { o.documentos = v; return this; }
        public SeguimientoDTO build()                         { return o; }
    }

    // ── Cabecera ─────────────────────────────────────────────────────────────

    public static class Cabecera {
        private String codAlumno;
        private String nombreAlumno;
        private String empresa;
        private String centroDeTrabajo;
        private String profesorTutor;
        private String estudio;
        private String modalidadEnsenanza;
        private String movilidad;
        private String modalidadPresencial;

        private Cabecera() {}

        public String getCodAlumno()          { return codAlumno; }
        public String getNombreAlumno()       { return nombreAlumno; }
        public String getEmpresa()            { return empresa; }
        public String getCentroDeTrabajo()    { return centroDeTrabajo; }
        public String getProfesorTutor()      { return profesorTutor; }
        public String getEstudio()            { return estudio; }
        public String getModalidadEnsenanza() { return modalidadEnsenanza; }
        public String getMovilidad()          { return movilidad; }
        public String getModalidadPresencial(){ return modalidadPresencial; }

        public static Builder builder() { return new Builder(); }
        public static class Builder {
            private final Cabecera o = new Cabecera();
            public Builder codAlumno(String v)          { o.codAlumno = v; return this; }
            public Builder nombreAlumno(String v)       { o.nombreAlumno = v; return this; }
            public Builder empresa(String v)            { o.empresa = v; return this; }
            public Builder centroDeTrabajo(String v)    { o.centroDeTrabajo = v; return this; }
            public Builder profesorTutor(String v)      { o.profesorTutor = v; return this; }
            public Builder estudio(String v)            { o.estudio = v; return this; }
            public Builder modalidadEnsenanza(String v) { o.modalidadEnsenanza = v; return this; }
            public Builder movilidad(String v)          { o.movilidad = v; return this; }
            public Builder modalidadPresencial(String v){ o.modalidadPresencial = v; return this; }
            public Cabecera build()                     { return o; }
        }
    }

    // ── SeguimientoAlumno ─────────────────────────────────────────────────────

    public static class SeguimientoAlumno {
        private String periodoInicio;
        private String periodoFin;
        private String contactoInicial;
        private String contactoSeguimiento;
        private String contactoValoracion;

        private SeguimientoAlumno() {}

        public String getPeriodoInicio()        { return periodoInicio; }
        public String getPeriodoFin()           { return periodoFin; }
        public String getContactoInicial()      { return contactoInicial; }
        public String getContactoSeguimiento()  { return contactoSeguimiento; }
        public String getContactoValoracion()   { return contactoValoracion; }

        public static Builder builder() { return new Builder(); }
        public static class Builder {
            private final SeguimientoAlumno o = new SeguimientoAlumno();
            public Builder periodoInicio(String v)       { o.periodoInicio = v; return this; }
            public Builder periodoFin(String v)          { o.periodoFin = v; return this; }
            public Builder contactoInicial(String v)     { o.contactoInicial = v; return this; }
            public Builder contactoSeguimiento(String v) { o.contactoSeguimiento = v; return this; }
            public Builder contactoValoracion(String v)  { o.contactoValoracion = v; return this; }
            public SeguimientoAlumno build()             { return o; }
        }
    }

    // ── Valoracion ────────────────────────────────────────────────────────────

    public static class Valoracion {
        private String valoracionDossier;
        private String cuestionarioCentroTrabajo;
        private String codVisitaRef18;
        private String codVisitaValoracion;

        private Valoracion() {}

        public String getValoracionDossier()          { return valoracionDossier; }
        public String getCuestionarioCentroTrabajo()  { return cuestionarioCentroTrabajo; }
        public String getCodVisitaRef18()             { return codVisitaRef18; }
        public String getCodVisitaValoracion()        { return codVisitaValoracion; }

        public static Builder builder() { return new Builder(); }
        public static class Builder {
            private final Valoracion o = new Valoracion();
            public Builder valoracionDossier(String v)         { o.valoracionDossier = v; return this; }
            public Builder cuestionarioCentroTrabajo(String v) { o.cuestionarioCentroTrabajo = v; return this; }
            public Builder codVisitaRef18(String v)            { o.codVisitaRef18 = v; return this; }
            public Builder codVisitaValoracion(String v)       { o.codVisitaValoracion = v; return this; }
            public Valoracion build()                          { return o; }
        }
    }

    // ── Homologacion ──────────────────────────────────────────────────────────

    public static class Homologacion {
        private String profesorTutor;
        private String coordPracticas;
        private String coordTerritorial;

        private Homologacion() {}

        public String getProfesorTutor()    { return profesorTutor; }
        public String getCoordPracticas()   { return coordPracticas; }
        public String getCoordTerritorial() { return coordTerritorial; }

        public static Builder builder() { return new Builder(); }
        public static class Builder {
            private final Homologacion o = new Homologacion();
            public Builder profesorTutor(String v)    { o.profesorTutor = v; return this; }
            public Builder coordPracticas(String v)   { o.coordPracticas = v; return this; }
            public Builder coordTerritorial(String v) { o.coordTerritorial = v; return this; }
            public Homologacion build()               { return o; }
        }
    }

    // ── Documento ─────────────────────────────────────────────────────────────

    public static class Documento {
        private String ref;   // "REF06", "REF07", "REF18"...
        private String url;   // URL del PDF en castellano

        private Documento() {}

        public String getRef() { return ref; }
        public String getUrl() { return url; }

        public static Builder builder() { return new Builder(); }
        public static class Builder {
            private final Documento o = new Documento();
            public Builder ref(String v) { o.ref = v; return this; }
            public Builder url(String v) { o.url = v; return this; }
            public Documento build()     { return o; }
        }
    }
}

