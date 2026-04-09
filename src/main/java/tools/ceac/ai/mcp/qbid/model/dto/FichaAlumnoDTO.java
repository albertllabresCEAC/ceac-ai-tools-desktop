package tools.ceac.ai.mcp.qbid.model.dto;

import java.util.List;

public class FichaAlumnoDTO {

    private String codAlumno;        // hidden input alumneId
    private String usuario;          // "lca5fbdh"
    private String extranjero;       // "Si" / "No"
    private String nombre;
    private String apellidos;
    private String fechaNacimiento;
    private String sexo;
    private String direccion;
    private String telefono;
    private String pais;
    private String codigoPostal;
    private String municipio;
    private String inss;
    private String nass;
    private String idalu;
    private String email;
    private String necesidadesEspeciales;
    private String observaciones;
    private List<Cuaderno> cuadernos;

    private FichaAlumnoDTO() {}

    public String getCodAlumno()            { return codAlumno; }
    public String getUsuario()              { return usuario; }
    public String getExtranjero()           { return extranjero; }
    public String getNombre()               { return nombre; }
    public String getApellidos()            { return apellidos; }
    public String getFechaNacimiento()      { return fechaNacimiento; }
    public String getSexo()                 { return sexo; }
    public String getDireccion()            { return direccion; }
    public String getTelefono()             { return telefono; }
    public String getPais()                 { return pais; }
    public String getCodigoPostal()         { return codigoPostal; }
    public String getMunicipio()            { return municipio; }
    public String getInss()                 { return inss; }
    public String getNass()                 { return nass; }
    public String getIdalu()                { return idalu; }
    public String getEmail()                { return email; }
    public String getNecesidadesEspeciales(){ return necesidadesEspeciales; }
    public String getObservaciones()        { return observaciones; }
    public List<Cuaderno> getCuadernos()    { return cuadernos; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final FichaAlumnoDTO o = new FichaAlumnoDTO();
        public Builder codAlumno(String v)            { o.codAlumno = v;             return this; }
        public Builder usuario(String v)              { o.usuario = v;               return this; }
        public Builder extranjero(String v)           { o.extranjero = v;            return this; }
        public Builder nombre(String v)               { o.nombre = v;                return this; }
        public Builder apellidos(String v)            { o.apellidos = v;             return this; }
        public Builder fechaNacimiento(String v)      { o.fechaNacimiento = v;       return this; }
        public Builder sexo(String v)                 { o.sexo = v;                  return this; }
        public Builder direccion(String v)            { o.direccion = v;             return this; }
        public Builder telefono(String v)             { o.telefono = v;              return this; }
        public Builder pais(String v)                 { o.pais = v;                  return this; }
        public Builder codigoPostal(String v)         { o.codigoPostal = v;          return this; }
        public Builder municipio(String v)            { o.municipio = v;             return this; }
        public Builder inss(String v)                 { o.inss = v;                  return this; }
        public Builder nass(String v)                 { o.nass = v;                  return this; }
        public Builder idalu(String v)                { o.idalu = v;                 return this; }
        public Builder email(String v)                { o.email = v;                 return this; }
        public Builder necesidadesEspeciales(String v){ o.necesidadesEspeciales = v; return this; }
        public Builder observaciones(String v)        { o.observaciones = v;         return this; }
        public Builder cuadernos(List<Cuaderno> v)    { o.cuadernos = v;             return this; }
        public FichaAlumnoDTO build()                 { return o; }
    }

    // â”€â”€ Cuaderno â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    public static class Cuaderno {
        private String estudio;       // texto completo del label strong
        private String codCuaderno;   // primer arg de doVeureQuadern
        private String codPrograma;   // segundo arg de doVeureQuadern
        private String estado;        // "Pendiente de Cualificar", "Cualificado", "Provisional", "Baja"

        private Cuaderno() {}

        public String getEstudio()     { return estudio; }
        public String getCodCuaderno() { return codCuaderno; }
        public String getCodPrograma() { return codPrograma; }
        public String getEstado()      { return estado; }

        public static Builder builder() { return new Builder(); }
        public static class Builder {
            private final Cuaderno o = new Cuaderno();
            public Builder estudio(String v)     { o.estudio = v;     return this; }
            public Builder codCuaderno(String v) { o.codCuaderno = v; return this; }
            public Builder codPrograma(String v) { o.codPrograma = v; return this; }
            public Builder estado(String v)      { o.estado = v;      return this; }
            public Cuaderno build()              { return o; }
        }
    }
}
