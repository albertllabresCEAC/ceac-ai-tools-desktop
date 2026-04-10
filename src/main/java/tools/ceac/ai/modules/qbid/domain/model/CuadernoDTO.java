package tools.ceac.ai.modules.qbid.domain.model;

import java.util.List;

public class CuadernoDTO {
    private String codCuaderno;
    private String codAlumno;
    private String nombreAlumno;
    private String estudio;
    private String estadoCuaderno;          // "Pendiente", "Activo", "Cualificado"...
    private String horasInformadas;
    private String horasValidadas;
    private String horasRestantesInformar;
    private String horasRestantesValidar;
    private String horasCurriculares;
    private String exencion;
    private String hashCode;
    private String urlRef19;               // ValoraciÃ³n global (documentValoracio)
    private String urlRef20;               // CalificaciÃ³n final FCT (document)
    private String urlRef22;               // Expediente (documentExpedient)
    private List<AcuerdoCuadernoDTO> acuerdos;

    private CuadernoDTO() {}

    public String getCodCuaderno()              { return codCuaderno; }
    public String getCodAlumno()                { return codAlumno; }
    public String getNombreAlumno()             { return nombreAlumno; }
    public String getEstudio()                  { return estudio; }
    public String getEstadoCuaderno()           { return estadoCuaderno; }
    public String getHorasInformadas()          { return horasInformadas; }
    public String getHorasValidadas()           { return horasValidadas; }
    public String getHorasRestantesInformar()   { return horasRestantesInformar; }
    public String getHorasRestantesValidar()    { return horasRestantesValidar; }
    public String getHorasCurriculares()        { return horasCurriculares; }
    public String getExencion()                 { return exencion; }
    public String getHashCode()                 { return hashCode; }
    public String getUrlRef19()                 { return urlRef19; }
    public String getUrlRef20()                 { return urlRef20; }
    public String getUrlRef22()                 { return urlRef22; }
    public List<AcuerdoCuadernoDTO> getAcuerdos() { return acuerdos; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final CuadernoDTO o = new CuadernoDTO();
        public Builder codCuaderno(String v)            { o.codCuaderno = v; return this; }
        public Builder codAlumno(String v)              { o.codAlumno = v; return this; }
        public Builder nombreAlumno(String v)           { o.nombreAlumno = v; return this; }
        public Builder estudio(String v)                { o.estudio = v; return this; }
        public Builder estadoCuaderno(String v)         { o.estadoCuaderno = v; return this; }
        public Builder horasInformadas(String v)        { o.horasInformadas = v; return this; }
        public Builder horasValidadas(String v)         { o.horasValidadas = v; return this; }
        public Builder horasRestantesInformar(String v) { o.horasRestantesInformar = v; return this; }
        public Builder horasRestantesValidar(String v)  { o.horasRestantesValidar = v; return this; }
        public Builder horasCurriculares(String v)      { o.horasCurriculares = v; return this; }
        public Builder exencion(String v)               { o.exencion = v; return this; }
        public Builder hashCode(String v)               { o.hashCode = v; return this; }
        public Builder urlRef19(String v)               { o.urlRef19 = v; return this; }
        public Builder urlRef20(String v)               { o.urlRef20 = v; return this; }
        public Builder urlRef22(String v)               { o.urlRef22 = v; return this; }
        public Builder acuerdos(List<AcuerdoCuadernoDTO> v) { o.acuerdos = v; return this; }
        public CuadernoDTO build()                      { return o; }
    }
}


