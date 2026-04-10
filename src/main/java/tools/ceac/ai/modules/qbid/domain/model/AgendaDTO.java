package tools.ceac.ai.modules.qbid.domain.model;

import java.util.List;

public class AgendaDTO {

    private String codAlumno;
    private String nombreAlumno;
    private String empresa;
    private String centroDeTrabajo;
    private String profesorTutor;
    private String estudio;
    private String periodoInicio;
    private String periodoFin;
    private String horasInformadas;
    private String horasConsignadas;
    private String horasValidadas;
    private List<DiaCalendario> calendario;
    private List<Ausencia> ausencias;
    private List<InformePeriodico> informes;
    private DiaActual diaActual;

    private AgendaDTO() {}

    public String getCodAlumno() { return codAlumno; }
    public String getNombreAlumno() { return nombreAlumno; }
    public String getEmpresa() { return empresa; }
    public String getCentroDeTrabajo() { return centroDeTrabajo; }
    public String getProfesorTutor() { return profesorTutor; }
    public String getEstudio() { return estudio; }
    public String getPeriodoInicio() { return periodoInicio; }
    public String getPeriodoFin() { return periodoFin; }
    public String getHorasInformadas() { return horasInformadas; }
    public String getHorasConsignadas() { return horasConsignadas; }
    public String getHorasValidadas() { return horasValidadas; }
    public List<DiaCalendario> getCalendario() { return calendario; }
    public List<Ausencia> getAusencias() { return ausencias; }
    public List<InformePeriodico> getInformes() { return informes; }
    public DiaActual getDiaActual() { return diaActual; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final AgendaDTO value = new AgendaDTO();

        public Builder codAlumno(String input) {
            value.codAlumno = input;
            return this;
        }

        public Builder nombreAlumno(String input) {
            value.nombreAlumno = input;
            return this;
        }

        public Builder empresa(String input) {
            value.empresa = input;
            return this;
        }

        public Builder centroDeTrabajo(String input) {
            value.centroDeTrabajo = input;
            return this;
        }

        public Builder profesorTutor(String input) {
            value.profesorTutor = input;
            return this;
        }

        public Builder estudio(String input) {
            value.estudio = input;
            return this;
        }

        public Builder periodoInicio(String input) {
            value.periodoInicio = input;
            return this;
        }

        public Builder periodoFin(String input) {
            value.periodoFin = input;
            return this;
        }

        public Builder horasInformadas(String input) {
            value.horasInformadas = input;
            return this;
        }

        public Builder horasConsignadas(String input) {
            value.horasConsignadas = input;
            return this;
        }

        public Builder horasValidadas(String input) {
            value.horasValidadas = input;
            return this;
        }

        public Builder calendario(List<DiaCalendario> input) {
            value.calendario = input;
            return this;
        }

        public Builder ausencias(List<Ausencia> input) {
            value.ausencias = input;
            return this;
        }

        public Builder informes(List<InformePeriodico> input) {
            value.informes = input;
            return this;
        }

        public Builder diaActual(DiaActual input) {
            value.diaActual = input;
            return this;
        }

        public AgendaDTO build() {
            return value;
        }
    }

    public static class DiaCalendario {
        private int dia;
        private String estado;

        private DiaCalendario() {}

        public int getDia() { return dia; }
        public String getEstado() { return estado; }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private final DiaCalendario value = new DiaCalendario();

            public Builder dia(int input) {
                value.dia = input;
                return this;
            }

            public Builder estado(String input) {
                value.estado = input;
                return this;
            }

            public DiaCalendario build() {
                return value;
            }
        }
    }

    public static class Ausencia {
        private String fechaDesde;
        private String fechaHasta;
        private String motivo;

        private Ausencia() {}

        public String getFechaDesde() { return fechaDesde; }
        public String getFechaHasta() { return fechaHasta; }
        public String getMotivo() { return motivo; }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private final Ausencia value = new Ausencia();

            public Builder fechaDesde(String input) {
                value.fechaDesde = input;
                return this;
            }

            public Builder fechaHasta(String input) {
                value.fechaHasta = input;
                return this;
            }

            public Builder motivo(String input) {
                value.motivo = input;
                return this;
            }

            public Ausencia build() {
                return value;
            }
        }
    }

    public static class InformePeriodico {
        private String fechaDesde;
        private String fechaHasta;
        private String estado;
        private String conveniValoracioPk;
        private String hashCode;
        private String url;

        private InformePeriodico() {}

        public String getFechaDesde() { return fechaDesde; }
        public String getFechaHasta() { return fechaHasta; }
        public String getEstado() { return estado; }
        public String getConveniValoracioPk() { return conveniValoracioPk; }
        public String getHashCode() { return hashCode; }
        public String getUrl() { return url; }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private final InformePeriodico value = new InformePeriodico();

            public Builder fechaDesde(String input) {
                value.fechaDesde = input;
                return this;
            }

            public Builder fechaHasta(String input) {
                value.fechaHasta = input;
                return this;
            }

            public Builder estado(String input) {
                value.estado = input;
                return this;
            }

            public Builder conveniValoracioPk(String input) {
                value.conveniValoracioPk = input;
                return this;
            }

            public Builder hashCode(String input) {
                value.hashCode = input;
                return this;
            }

            public Builder url(String input) {
                value.url = input;
                return this;
            }

            public InformePeriodico build() {
                return value;
            }
        }
    }

    public static class DiaActual {
        private String fecha;
        private String descripcion;
        private String hashCode;
        private String url;

        private DiaActual() {}

        public String getFecha() { return fecha; }
        public String getDescripcion() { return descripcion; }
        public String getHashCode() { return hashCode; }
        public String getUrl() { return url; }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private final DiaActual value = new DiaActual();

            public Builder fecha(String input) {
                value.fecha = input;
                return this;
            }

            public Builder descripcion(String input) {
                value.descripcion = input;
                return this;
            }

            public Builder hashCode(String input) {
                value.hashCode = input;
                return this;
            }

            public Builder url(String input) {
                value.url = input;
                return this;
            }

            public DiaActual build() {
                return value;
            }
        }
    }
}


