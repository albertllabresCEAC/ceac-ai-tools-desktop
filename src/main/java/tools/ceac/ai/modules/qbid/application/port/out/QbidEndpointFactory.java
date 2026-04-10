package tools.ceac.ai.modules.qbid.application.port.out;

/**
 * Builds qBid endpoint URLs for the application layer.
 */
public interface QbidEndpointFactory {

    String agenda();
    String cambiarSistemaModGen();
    String cambiarSistemaBid();
    String listadoModGen();
    String listadoBid();
    String detalleConvenio(String codConvenio, String codTemporal, String newSystem);
    String cuaderno(String codCuaderno);
    String seguimiento(String codConvenio, String codTemporal, String newSystem);
    String agendaAlumno(String codConvenio, String codTemporal);
    String hashActividad(String codAlumno, String fecha);
    String guardarInformePeriodico();
    String informePeriodico(String codConvenio, String codTemporal, String conveniValoracioPk, String hashCode);
    String ref07(String conveniId, String conveniProvId, String language);
    String ref10(String conveniId, String conveniProvId, String estudiId, String language);
    String ref11(String conveniId, String conveniProvId, String estudiId, String language);
    String ref18(String conveniId, String conveniProvId, String codVisita, String language);
    String ref05(String conveniId, String conveniProvId, String hashCode, String cursSeleccio, String language);
    String ref05Baja(String conveniId, String conveniProvId, String hashCode, String cursSeleccio, String language);
    String ref06(String conveniId, String conveniProvId, String estudiId, String language);
    String ref19(String quadernId, String language);
    String ref20(String quadernId, String language);
    String ref22(String quadernId, String language);
    String ref15(String conveniId, String conveniProvId, String codVisita, String language);
    String valoracion(String conveniId, String conveniProvId, String codVisitaValoracion, String codVisita);
    String fichaAlumno(String codAlumno);
    String empresa(String codEmpresa);
    String centroTrabajo(String codEmpresa, String codCentro);
    String tabResponsablesCT(String codCentro, String versioCT);
    String guardarPlanActividades();
    String planActividades(String codConvenio, String codTemporal, String newSystem);
    String actividadDiaria(String codConvenio, String codTemporal, String fecha, String hashCode, String conveniActivitatPk);
}

