package tools.ceac.ai.modules.qbid.domain.model;

import java.util.List;

public class ValoracionDTO {

    private String qualificacio;       // "Positiva", "Negativa"â€¦
    private String subQualificacio;    // "Bien", "Notable"â€¦
    private String fecha;              // dataQualy â€” "25/02/2026"
    private String observaciones;
    private String fechaContacto;      // data_realitzada
    private String tipoContacto;       // "Correo", "TelÃ©fono"â€¦
    private String signatarioEmpresa;
    private String signatarioCentro;
    private List<Criterio> criterios;  // quest_0 â€¦ quest_20

    private ValoracionDTO() {}

    public String getQualificacio()       { return qualificacio; }
    public String getSubQualificacio()    { return subQualificacio; }
    public String getFecha()             { return fecha; }
    public String getObservaciones()     { return observaciones; }
    public String getFechaContacto()     { return fechaContacto; }
    public String getTipoContacto()      { return tipoContacto; }
    public String getSignatarioEmpresa() { return signatarioEmpresa; }
    public String getSignatarioCentro()  { return signatarioCentro; }
    public List<Criterio> getCriterios() { return criterios; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final ValoracionDTO o = new ValoracionDTO();
        public Builder qualificacio(String v)       { o.qualificacio = v;       return this; }
        public Builder subQualificacio(String v)    { o.subQualificacio = v;    return this; }
        public Builder fecha(String v)             { o.fecha = v;             return this; }
        public Builder observaciones(String v)     { o.observaciones = v;     return this; }
        public Builder fechaContacto(String v)     { o.fechaContacto = v;     return this; }
        public Builder tipoContacto(String v)      { o.tipoContacto = v;      return this; }
        public Builder signatarioEmpresa(String v) { o.signatarioEmpresa = v; return this; }
        public Builder signatarioCentro(String v)  { o.signatarioCentro = v;  return this; }
        public Builder criterios(List<Criterio> v) { o.criterios = v;         return this; }
        public ValoracionDTO build()               { return o; }
    }

    // â”€â”€ Criterio â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    public static class Criterio {
        private String texto;
        private String valoracion;   // "BUENA/BIEN", "MUY BUENA/MUY BIEN"â€¦

        private Criterio() {}

        public String getTexto()     { return texto; }
        public String getValoracion(){ return valoracion; }

        public static Builder builder() { return new Builder(); }
        public static class Builder {
            private final Criterio o = new Criterio();
            public Builder texto(String v)     { o.texto = v;     return this; }
            public Builder valoracion(String v){ o.valoracion = v; return this; }
            public Criterio build()            { return o; }
        }
    }
}


