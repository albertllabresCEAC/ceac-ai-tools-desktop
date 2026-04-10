package tools.ceac.ai.mcp.qbid.model.dto;

/**
 * Representa un acuerdo/convenio listado dentro del cuaderno.
 */
public class AcuerdoCuadernoDTO {
    private String documento;      // "2025254479 / FP1250327"
    private String fechas;         // "de 16/03/2026 a 01/07/2026"
    private String estado;         // "En curso"
    private String horasReales;    // "0h"
    private String horasEstimadas; // "518h"
    private String codConvenio;    // extraído del onclick
    private String codTemporal;
    private String newSystem;

    private AcuerdoCuadernoDTO() {}

    public String getDocumento()      { return documento; }
    public String getFechas()         { return fechas; }
    public String getEstado()         { return estado; }
    public String getHorasReales()    { return horasReales; }
    public String getHorasEstimadas() { return horasEstimadas; }
    public String getCodConvenio()    { return codConvenio; }
    public String getCodTemporal()    { return codTemporal; }
    public String getNewSystem()      { return newSystem; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final AcuerdoCuadernoDTO o = new AcuerdoCuadernoDTO();
        public Builder documento(String v)      { o.documento = v; return this; }
        public Builder fechas(String v)         { o.fechas = v; return this; }
        public Builder estado(String v)         { o.estado = v; return this; }
        public Builder horasReales(String v)    { o.horasReales = v; return this; }
        public Builder horasEstimadas(String v) { o.horasEstimadas = v; return this; }
        public Builder codConvenio(String v)    { o.codConvenio = v; return this; }
        public Builder codTemporal(String v)    { o.codTemporal = v; return this; }
        public Builder newSystem(String v)      { o.newSystem = v; return this; }
        public AcuerdoCuadernoDTO build()       { return o; }
    }
}

