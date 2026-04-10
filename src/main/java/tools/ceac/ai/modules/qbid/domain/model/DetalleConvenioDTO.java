package tools.ceac.ai.modules.qbid.domain.model;

public class DetalleConvenioDTO {
    private String codAlumno;
    private String nombreAlumno;
    private String codEmpresa;
    private String empresa;
    private String tutorEmpresa;
    private String profesorTutor;
    private String fechaInicio;
    private String fechaFin;
    private String horasTotales;
    private String horasPendientes;
    private String horasAcumuladas;
    private String estudio;
    private String curso;
    private String tipoAcuerdo;
    private String hashCodeRef05;
    private String hashCodeRef05Baja;
    private String cursSeleccio;
    private String estudiId;

    private DetalleConvenioDTO() {}

    public String getCodAlumno()       { return codAlumno; }
    public String getNombreAlumno()    { return nombreAlumno; }
    public String getCodEmpresa()      { return codEmpresa; }
    public String getEmpresa()         { return empresa; }
    public String getTutorEmpresa()    { return tutorEmpresa; }
    public String getProfesorTutor()   { return profesorTutor; }
    public String getFechaInicio()     { return fechaInicio; }
    public String getFechaFin()        { return fechaFin; }
    public String getHorasTotales()    { return horasTotales; }
    public String getHorasPendientes() { return horasPendientes; }
    public String getHorasAcumuladas() { return horasAcumuladas; }
    public String getEstudio()         { return estudio; }
    public String getCurso()           { return curso; }
    public String getTipoAcuerdo()        { return tipoAcuerdo; }
    public String getHashCodeRef05()      { return hashCodeRef05; }
    public String getHashCodeRef05Baja()  { return hashCodeRef05Baja; }
    public String getCursSeleccio()       { return cursSeleccio; }
    public String getEstudiId()           { return estudiId; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final DetalleConvenioDTO o = new DetalleConvenioDTO();
        public Builder codAlumno(String v)       { o.codAlumno = v; return this; }
        public Builder nombreAlumno(String v)    { o.nombreAlumno = v; return this; }
        public Builder codEmpresa(String v)      { o.codEmpresa = v; return this; }
        public Builder empresa(String v)         { o.empresa = v; return this; }
        public Builder tutorEmpresa(String v)    { o.tutorEmpresa = v; return this; }
        public Builder profesorTutor(String v)   { o.profesorTutor = v; return this; }
        public Builder fechaInicio(String v)     { o.fechaInicio = v; return this; }
        public Builder fechaFin(String v)        { o.fechaFin = v; return this; }
        public Builder horasTotales(String v)    { o.horasTotales = v; return this; }
        public Builder horasPendientes(String v) { o.horasPendientes = v; return this; }
        public Builder horasAcumuladas(String v) { o.horasAcumuladas = v; return this; }
        public Builder estudio(String v)         { o.estudio = v; return this; }
        public Builder curso(String v)           { o.curso = v; return this; }
        public Builder tipoAcuerdo(String v)         { o.tipoAcuerdo = v; return this; }
        public Builder hashCodeRef05(String v)       { o.hashCodeRef05 = v; return this; }
        public Builder hashCodeRef05Baja(String v)   { o.hashCodeRef05Baja = v; return this; }
        public Builder cursSeleccio(String v)        { o.cursSeleccio = v; return this; }
        public Builder estudiId(String v)            { o.estudiId = v; return this; }
        public DetalleConvenioDTO build()            { return o; }
    }
}



