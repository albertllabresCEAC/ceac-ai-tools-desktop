package tools.ceac.ai.mcp.qbid.model.dto;

import java.util.List;

/**
 * Request para guardar (borrador) un informe periódico.
 *
 * <p>Los campos {@code conveniValoracioPk}, {@code codConvenio}, {@code codTemporal}
 * y {@code hashCode} se obtienen de {@code listarInformes} o {@code verDetalleInforme}.
 *
 * <p>Las {@code valoraciones} deben incluir una entrada por cada actividad presente
 * en el informe. El campo {@code actividadId} es el sufijo numérico (e.g. {@code "92245"}),
 * obtenido de {@code verDetalleInforme} (actividades[].id).
 *
 * <p>Valores aceptados para {@code adecuacionTutor} (campo {@code inp_XXXXX} en qBID):
 * <ul>
 *   <li>{@code MUY_ADECUADA}  → código 29</li>
 *   <li>{@code ADECUADA}      → código 30</li>
 *   <li>{@code POCO_ADECUADA} → código 31</li>
 *   <li>{@code NADA_ADECUADA} → código 32</li>
 *   <li>{@code SIN_VALORACION}→ código 33 (usar para ítems con 0h)</li>
 * </ul>
 *
 * <p>Valores aceptados para {@code valoracionEmpresa} (campo {@code inp_extra_XXXXX} en qBID):
 * <ul>
 *   <li>{@code MUY_BUENA}     → código 81 (nota 8-10)</li>
 *   <li>{@code BUENA}         → código 82 (nota 6-8)</li>
 *   <li>{@code SUFICIENTE}    → código 83 (nota 5)</li>
 *   <li>{@code PASIVA}        → código 84 (nota 3-4)</li>
 *   <li>{@code NEGATIVA}      → código 85 (nota 0-2)</li>
 *   <li>{@code SIN_VALORACION}→ código 86</li>
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
    private String observacionesAlumno;   // opcional, máx 1000 caracteres
    private String observacionesEmpresa;  // opcional, máx 1000 caracteres
    private String observacionesTutor;    // obligatorio si hay NEGATIVA en valoracionEmpresa; máx 1000 caracteres
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

    // ── ValoracionItem ────────────────────────────────────────────────────────

    /**
     * Valoración de una actividad concreta del informe.
     */
    public static class ValoracionItem {
        /** Sufijo numérico de la actividad, e.g. "92245". Obtenido de actividades[].id */
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

    // ── Enums de valoración ───────────────────────────────────────────────────

    /**
     * Adecuación de las tareas del alumno/a al plan formativo.
     * Se envía a qBID como parámetro {@code inp_<actividadId>}.
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
         * @throws IllegalArgumentException si el nombre no es válido
         */
        public static AdecuacionTutor fromNombre(String nombre) {
            for (AdecuacionTutor v : values()) {
                if (v.name().equalsIgnoreCase(nombre)) return v;
            }
            throw new IllegalArgumentException(
                "Adecuación desconocida: '" + nombre + "'. Valores válidos: "
                + "MUY_ADECUADA, ADECUADA, POCO_ADECUADA, NADA_ADECUADA, SIN_VALORACION");
        }
    }

    /**
     * Valoración de la empresa sobre la actuación del alumno/a.
     * Se envía a qBID como parámetro {@code inp_extra_<actividadId>}.
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
         * @throws IllegalArgumentException si el nombre no es válido
         */
        public static ValoracionEmpresa fromNombre(String nombre) {
            for (ValoracionEmpresa v : values()) {
                if (v.name().equalsIgnoreCase(nombre)) return v;
            }
            throw new IllegalArgumentException(
                "Valoración empresa desconocida: '" + nombre + "'. Valores válidos: "
                + "MUY_BUENA, BUENA, SUFICIENTE, PASIVA, NEGATIVA, SIN_VALORACION");
        }
    }
}
