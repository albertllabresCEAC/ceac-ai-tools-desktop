package tools.ceac.ai.mcp.qbid.model.dto;

public class CentroTrabajoDTO {

    private String codCentro;      // hidden cod_centre_treball_pk
    private String codEmpresa;     // hidden cod_empresa_pk

    // ── Identificación ────────────────────────────────────────────────────────
    private String nomenclatura;    // "ME433101-MEM01-00"
    private String estado;          // "VALIDADO"
    private String categoria;       // "MICROEMPRESA"
    private String nombre;          // "SEU CENTRAL"
    private String nombreOpcional;

    // ── Datos ubicación (tab #dadesUbicacio) ──────────────────────────────────
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

    // ── Datos de contacto (tab #dadesContacte) ────────────────────────────────
    private String telefono;
    private String fax;
    private String email;

    // ── Datos actividad (tab #dadesActivitat) ─────────────────────────────────
    private String ccae;

    private CentroTrabajoDTO() {}

    public String getCodCentro()          { return codCentro; }
    public String getCodEmpresa()         { return codEmpresa; }
    public String getNomenclatura()       { return nomenclatura; }
    public String getEstado()             { return estado; }
    public String getCategoria()          { return categoria; }
    public String getNombre()             { return nombre; }
    public String getNombreOpcional()     { return nombreOpcional; }
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
    public String getTelefono()           { return telefono; }
    public String getFax()                { return fax; }
    public String getEmail()              { return email; }
    public String getCcae()               { return ccae; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final CentroTrabajoDTO o = new CentroTrabajoDTO();
        public Builder codCentro(String v)          { o.codCentro = v;          return this; }
        public Builder codEmpresa(String v)         { o.codEmpresa = v;         return this; }
        public Builder nomenclatura(String v)       { o.nomenclatura = v;       return this; }
        public Builder estado(String v)             { o.estado = v;             return this; }
        public Builder categoria(String v)          { o.categoria = v;          return this; }
        public Builder nombre(String v)             { o.nombre = v;             return this; }
        public Builder nombreOpcional(String v)     { o.nombreOpcional = v;     return this; }
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
        public Builder telefono(String v)           { o.telefono = v;           return this; }
        public Builder fax(String v)                { o.fax = v;                return this; }
        public Builder email(String v)              { o.email = v;              return this; }
        public Builder ccae(String v)               { o.ccae = v;               return this; }
        public CentroTrabajoDTO build()             { return o; }
    }
}
