package tools.ceac.ai.mcp.qbid.model.dto;

import java.util.List;

/**
 * Request para guardar (borrador) un informe periÃ³dico.
 *
 * <p>Los campos {@code conveniValoracioPk}, {@code codConvenio}, {@code codTemporal}
 * y {@code hashCode} se obtienen de {@code listarInformes} o {@code verDetalleInforme}.
 *
 * <p>Las {@code valoraciones} deben incluir una entrada por cada actividad presente
 * en el informe. El campo {@code actividadId} es el sufijo numÃ©rico (e.g. {@code "92245"}),
 * obtenido de {@code verDetalleInforme} (actividades[].id).
 *
 * <p>Valores aceptados para {@code adecuacionTutor} (campo {@code inp_XXXXX} en qBID):
 * <ul>
 *   <li>{@code MUY_ADECUADA}  â†’ cÃ³digo 29</li>
 *   <li>{@code ADECUADA}      â†’ cÃ³digo 30</li>
 *   <li>{@code POCO_ADECUADA} â†’ cÃ³digo 31</li>
 *   <li>{@code NADA_ADECUADA} â†’ cÃ³digo 32</li>
 *   <li>{@code SIN_VALORACION}â†’ cÃ³digo 33 (usar para Ã­tems con 0h)</li>
 * </ul>
 *
 * <p>Valores aceptados para {@code valoracionEmpresa} (campo {@code inp_extra_XXXXX} en qBID):
 * <ul>
 *   <li>{@code MUY_BUENA}     â†’ cÃ³digo 81 (nota 8-10)</li>
 *   <li>{@code BUENA}         â†’ cÃ³digo 82 (nota 6-8)</li>
 *   <li>{@code SUFICIENTE}    â†’ cÃ³digo 83 (nota 5)</li>
 *   <li>{@code PASIVA}        â†’ cÃ³digo 84 (nota 3-4)</li>
 *   <li>{@code NEGATIVA}      â†’ cÃ³digo 85 (nota 0-2)</li>
 *   <li>{@code SIN_VALORACION}â†’ cÃ³digo 86</li>
 * </ul>
 *
 * <p>Regla: si alguna {@code valoracionEmpresa} es {@code NEGATIVA},
 * {@code observacionesTutor} es obligatorio antes de guardar.
 */
public class GuardarInformeRequest {

    private String conveniValoracioPk;
    private String codConvenio;
    private String codTemporal;
    private String hashCode;
    private List<ValoracionItem> valoraciones;
    private String observacionesAlumno;   // opcional, mÃ¡x 1000 caracteres
    private String observacionesEmpresa;  // opcional, mÃ¡x 1000 caracteres
    private String observacionesTutor;    // obligatorio si hay NEGATIVA en valoracionEmpresa; mÃ¡x 1000 caracteres
    private String signatarioEmpresa;     // nombre completo del tutor/a empresa

    public String getConveniValoracioPk()              { return conveniValoracioPk; }
    public void   setConveniValoracioPk(String v)      { this.conveniValoracioPk = v; }
    public String getCodConvenio()                     { return codConvenio; }
    public void   setCodConvenio(String v)             { this.codConvenio = v; }
    public String getCodTemporal()                     { return codTemporal; }
    public void   setCodTemporal(String v)             { this.codTemporal = v; }
    public String getHashCode()                        { return hashCode; }
    public void   setHashCode(String v)                { this.hashCode = v; }
    public List<ValoracionItem> getValoraciones()      { return valoraciones; }
    public void   setValoraciones(List<ValoracionItem> v) { this.valoraciones = v; }
    public String getObservacionesAlumno()             { return observacionesAlumno; }
    public void   setObservacionesAlumno(String v)     { this.observacionesAlumno = v; }
    public String getObservacionesEmpresa()            { return observacionesEmpresa; }
    public void   setObservacionesEmpresa(String v)    { this.observacionesEmpresa = v; }
    public String getObservacionesTutor()              { return observacionesTutor; }
    public void   setObservacionesTutor(String v)      { this.observacionesTutor = v; }
    public String getSignatarioEmpresa()               { return signatarioEmpresa; }
    public void   setSignatarioEmpresa(String v)       { this.signatarioEmpresa = v; }

    // â”€â”€ ValoracionItem â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    /**
     * ValoraciÃ³n de una actividad concreta del informe.
     */
    public static class ValoracionItem {
        /** Sufijo numÃ©rico de la actividad, e.g. "92245". Obtenido de actividades[].id */
        private String actividadId;
        /** Nombre del enum {@link AdecuacionTutor}, e.g. "ADECUADA" */
        private String adecuacionTutor;
        /** Nombre del enum {@link ValoracionEmpresa}, e.g. "MUY_BUENA" */
        private String valoracionEmpresa;

        public String getActividadId()              { return actividadId; }
        public void   setActividadId(String v)      { this.actividadId = v; }
        public String getAdecuacionTutor()          { return adecuacionTutor; }
        public void   setAdecuacionTutor(String v)  { this.adecuacionTutor = v; }
        public String getValoracionEmpresa()        { return valoracionEmpresa; }
        public void   setValoracionEmpresa(String v){ this.valoracionEmpresa = v; }
    }

    // â”€â”€ Enums de valoraciÃ³n â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    /**
     * AdecuaciÃ³n de las tareas del alumno/a al plan formativo.
     * Se envÃ­a a qBID como parÃ¡metro {@code inp_<actividadId>}.
     */
    public enum AdecuacionTutor {
        MUY_ADECUADA("29"),
        ADECUADA("30"),
        POCO_ADECUADA("31"),
        NADA_ADECUADA("32"),
        SIN_VALORACION("33");

        private final String codigo;

        AdecuacionTutor(String codigo) { this.codigo = codigo; }

        public String getCodigo() { return codigo; }

        /**
         * Convierte el nombre de texto (case-insensitive) al enum correspondiente.
         * @throws IllegalArgumentException si el nombre no es vÃ¡lido
         */
        public static AdecuacionTutor fromNombre(String nombre) {
            for (AdecuacionTutor v : values()) {
                if (v.name().equalsIgnoreCase(nombre)) return v;
            }
            throw new IllegalArgumentException(
                "AdecuaciÃ³n desconocida: '" + nombre + "'. Valores vÃ¡lidos: "
                + "MUY_ADECUADA, ADECUADA, POCO_ADECUADA, NADA_ADECUADA, SIN_VALORACION");
        }
    }

    /**
     * ValoraciÃ³n de la empresa sobre la actuaciÃ³n del alumno/a.
     * Se envÃ­a a qBID como parÃ¡metro {@code inp_extra_<actividadId>}.
     */
    public enum ValoracionEmpresa {
        MUY_BUENA("81"),    // nota 8-10
        BUENA("82"),        // nota 6-8
        SUFICIENTE("83"),   // nota 5
        PASIVA("84"),       // nota 3-4
        NEGATIVA("85"),     // nota 0-2
        SIN_VALORACION("86");

        private final String codigo;

        ValoracionEmpresa(String codigo) { this.codigo = codigo; }

        public String getCodigo() { return codigo; }

        /**
         * Convierte el nombre de texto (case-insensitive) al enum correspondiente.
         * @throws IllegalArgumentException si el nombre no es vÃ¡lido
         */
        public static ValoracionEmpresa fromNombre(String nombre) {
            for (ValoracionEmpresa v : values()) {
                if (v.name().equalsIgnoreCase(nombre)) return v;
            }
            throw new IllegalArgumentException(
                "ValoraciÃ³n empresa desconocida: '" + nombre + "'. Valores vÃ¡lidos: "
                + "MUY_BUENA, BUENA, SUFICIENTE, PASIVA, NEGATIVA, SIN_VALORACION");
        }
    }
}
