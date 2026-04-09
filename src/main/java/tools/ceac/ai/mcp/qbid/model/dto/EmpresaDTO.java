package tools.ceac.ai.mcp.qbid.model.dto;

import java.util.List;

public class EmpresaDTO {

    // â”€â”€ IdentificaciÃ³n â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    private String codEmpresa;       // hidden empresaPk
    private String tipoEntidad;      // "Entidad EspaÃ±ola", "Entidad Extranjera"â€¦
    private String codigo;           // "ME 433101"
    private String estado;           // "VALIDADA"
    private String cifNif;           // "B26811612"
    private String tipo;             // "MICROEMPRESA"
    private String sector;           // "PRIVADO"
    private String numTrabajadores;  // "1-9"
    private String nombre;           // "CORNER ESTUDIOS TECNOLOGIA SL"

    // â”€â”€ Datos actividad â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    private String ubicacion;
    private String pais;
    private String codigoPostal;
    private String municipio;
    private String via;
    private String numero;
    private String escaleraPisoPuerta;
    private String restoDireccion;
    private String poligono;
    private String territorio;
    private String camara;
    private String telefono;
    private String fax;

    // â”€â”€ Centros de trabajo (resumen) â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    private List<CentroResumen> centros;

    private EmpresaDTO() {}

    public String getCodEmpresa()         { return codEmpresa; }
    public String getTipoEntidad()        { return tipoEntidad; }
    public String getCodigo()             { return codigo; }
    public String getEstado()             { return estado; }
    public String getCifNif()             { return cifNif; }
    public String getTipo()               { return tipo; }
    public String getSector()             { return sector; }
    public String getNumTrabajadores()    { return numTrabajadores; }
    public String getNombre()             { return nombre; }
    public String getUbicacion()          { return ubicacion; }
    public String getPais()               { return pais; }
    public String getCodigoPostal()       { return codigoPostal; }
    public String getMunicipio()          { return municipio; }
    public String getVia()                { return via; }
    public String getNumero()             { return numero; }
    public String getEscaleraPisoPuerta() { return escaleraPisoPuerta; }
    public String getRestoDireccion()     { return restoDireccion; }
    public String getPoligono()           { return poligono; }
    public String getTerritorio()         { return territorio; }
    public String getCamara()             { return camara; }
    public String getTelefono()           { return telefono; }
    public String getFax()                { return fax; }
    public List<CentroResumen> getCentros(){ return centros; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final EmpresaDTO o = new EmpresaDTO();
        public Builder codEmpresa(String v)         { o.codEmpresa = v;         return this; }
        public Builder tipoEntidad(String v)        { o.tipoEntidad = v;        return this; }
        public Builder codigo(String v)             { o.codigo = v;             return this; }
        public Builder estado(String v)             { o.estado = v;             return this; }
        public Builder cifNif(String v)             { o.cifNif = v;             return this; }
        public Builder tipo(String v)               { o.tipo = v;               return this; }
        public Builder sector(String v)             { o.sector = v;             return this; }
        public Builder numTrabajadores(String v)    { o.numTrabajadores = v;    return this; }
        public Builder nombre(String v)             { o.nombre = v;             return this; }
        public Builder ubicacion(String v)          { o.ubicacion = v;          return this; }
        public Builder pais(String v)               { o.pais = v;               return this; }
        public Builder codigoPostal(String v)       { o.codigoPostal = v;       return this; }
        public Builder municipio(String v)          { o.municipio = v;          return this; }
        public Builder via(String v)                { o.via = v;                return this; }
        public Builder numero(String v)             { o.numero = v;             return this; }
        public Builder escaleraPisoPuerta(String v) { o.escaleraPisoPuerta = v; return this; }
        public Builder restoDireccion(String v)     { o.restoDireccion = v;     return this; }
        public Builder poligono(String v)           { o.poligono = v;           return this; }
        public Builder territorio(String v)         { o.territorio = v;         return this; }
        public Builder camara(String v)             { o.camara = v;             return this; }
        public Builder telefono(String v)           { o.telefono = v;           return this; }
        public Builder fax(String v)                { o.fax = v;                return this; }
        public Builder centros(List<CentroResumen> v){ o.centros = v;           return this; }
        public EmpresaDTO build()                   { return o; }
    }

    // â”€â”€ CentroResumen â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    public static class CentroResumen {
        private String codCentro;   // arg de doDetall('303630')
        private String nombre;      // "ME433101-MEM01-00 - SEU CENTRAL"

        private CentroResumen() {}

        public String getCodCentro() { return codCentro; }
        public String getNombre()    { return nombre; }

        public static Builder builder() { return new Builder(); }
        public static class Builder {
            private final CentroResumen o = new CentroResumen();
            public Builder codCentro(String v) { o.codCentro = v; return this; }
            public Builder nombre(String v)    { o.nombre = v;    return this; }
            public CentroResumen build()       { return o; }
        }
    }
}

