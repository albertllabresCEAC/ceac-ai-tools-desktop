package tools.ceac.ai.mcp.qbid.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Centraliza todas las URLs de qBID.
 * Si cambia alguna URL, solo se toca aquí.
 */
@Component
public class QbidUrls {

    @Value("${qbid.base-url:https://www.empresaiformacio.org/sBid}")
    private String base;

    public String agenda() {
        return base + "/modules/Agenda?moduleaction=changeSistema&initial=yes";
    }

    public String cambiarSistemaModGen() {
        return base + "/modules/Agenda?moduleaction=changeSistema&clearBackStack=true&sys_programa=&sys_negoci=7";
    }

    public String cambiarSistemaBid() {
        return base + "/modules/Agenda?moduleaction=changeSistema&clearBackStack=true&sys_programa=&sys_negoci=1";
    }

    public String listadoModGen() {
        return base + "/modules/GestioFuncioConveni?moduleaction=consultaConveni"
                + "&query_page=&quadernId=&codi_conveni=&codi_conveni_provisional=&newSystem="
                + "&socCentre=Y&curs=72025"
                + "&specialWhereAlumne=1913%3B7%3B+%3D+72025&cod_alumne=&cod_alumne513177373="
                + "&cod_document_fk=&specialWhereEmpresa=1913%3B+%3D+72025&codiEmpresa="
                + "&codiEmpresa513207222=&propietari=-1&helped_restriction=1913%3B+%3D+72025"
                + "&cod_estudi_fk=&cod_estudi_fk513271753=";
    }

    public String listadoBid() {
        return base + "/modules/GestioFuncioConveni?moduleaction=consultaConveni"
                + "&query_page=&quadernId=&codi_conveni=&codi_conveni_provisional=&newSystem="
                + "&socCentre=Y&curs=2025"
                + "&specialWhereAlumne=1913%3B1%3B+%3D+2025&cod_alumne=&cod_alumne816084569="
                + "&cod_document_fk=&specialWhereEmpresa=1913%3B+%3D+2025&codiEmpresa="
                + "&codiEmpresa816110603=&propietari=-1&helped_restriction=1913%3B1%3B+%3D+2025"
                + "&cod_estudi_fk=&cod_estudi_fk816170985=";
    }

    public String detalleConvenio(String codConvenio, String codTemporal, String newSystem) {
        return base + "/modules/GestioConveniDirecte?moduleaction=showConveni"
                + "&codi_conveni=" + codConvenio
                + "&codi_conveni_provisional=" + codTemporal
                + "&newSystem=" + newSystem;  // valor real
    }

    public String cuaderno(String codCuaderno) {
        return base + "/modules/QuadernManagement?moduleaction=showQuadern"
                + "&quadernId=" + codCuaderno + "&newSystem=";
    }

    public String seguimiento(String codConvenio, String codTemporal, String newSystem) {
        return base + "/modules/Fct?moduleaction=show"
                + "&codi_conveni=" + codConvenio
                + "&codi_conveni_provisional=" + codTemporal
                + "&newSystem=" + newSystem;
    }

    public String agendaAlumno(String codConvenio, String codTemporal) {
        return base + "/modules/Fct?moduleaction=showActivitat"
                + "&codi_conveni=" + codConvenio
                + "&codi_conveni_provisional=" + codTemporal;
    }

    public String hashActividad(String codAlumno, String fecha) {
        return base + "/modules/Agenda?moduleaction=tasquesDiaAjaxAlumne"
                + "&entityAlumne=" + codAlumno
                + "&dia=" + fecha;
    }

    /** URL base del formulario de informe periódico (para POST de guardar/firmar). */
    public String guardarInformePeriodico() {
        return base + "/modules/ActivitatValoracioFct";
    }

    public String informePeriodico(String codConvenio, String codTemporal,
                                   String conveniValoracioPk, String hashCode) {
        return base + "/modules/ActivitatValoracioFct?moduleaction=show"
                + "&conveni_valoracio_pk=" + conveniValoracioPk
                + "&codi_conveni=" + codConvenio
                + "&codi_conveni_provisional=" + codTemporal
                + "&hash_code=" + hashCode;
    }

    // ── Referencias (documentos PDF) ──────────────────────────────────────

    public String ref07(String conveniId, String conveniProvId, String language) {
        return base + "/modules/Fct?moduleaction=document"
                + "&codi_conveni=" + conveniId
                + "&codi_conveni_provisional=" + conveniProvId
                + "&cod_visita=&docType=SEGUIMENT"
                + "&language=" + language;
    }

    public String ref10(String conveniId, String conveniProvId,
                        String estudiId, String language) {
        return base + "/modules/Fct?moduleaction=document"
                + "&codi_conveni=" + conveniId
                + "&codi_conveni_provisional=" + conveniProvId
                + "&estudiId=" + estudiId
                + "&docType=QUEST_EMPRESA"
                + "&language=" + language;
    }

    public String ref11(String conveniId, String conveniProvId,
                        String estudiId, String language) {
        return base + "/modules/Fct?moduleaction=document"
                + "&codi_conveni=" + conveniId
                + "&codi_conveni_provisional=" + conveniProvId
                + "&estudiId=" + estudiId
                + "&docType=HOMOLOGACIO"
                + "&language=" + language;
    }

    public String ref18(String conveniId, String conveniProvId,
                        String codVisita, String language) {
        return base + "/modules/Fct?moduleaction=document"
                + "&codi_conveni=" + conveniId
                + "&codi_conveni_provisional=" + conveniProvId
                + "&cod_visita=" + codVisita
                + "&docType=QUALI_EXPEDIENT"
                + "&language=" + language;
    }

    public String ref05(String conveniId, String conveniProvId,
                        String hashCode, String cursSeleccio, String language) {
        return base + "/modules/DocumentsConveni?moduleaction=document"
                + "&conveni_id=" + conveniId
                + "&conveni_prov_id=" + conveniProvId
                + "&hash_code=" + hashCode
                + "&tipus_document=CONVEN"
                + "&curs_seleccio=" + cursSeleccio
                + "&language=" + language;
    }

    public String ref05Baja(String conveniId, String conveniProvId,
                            String hashCode, String cursSeleccio, String language) {
        return base + "/modules/DocumentsConveni?moduleaction=document"
                + "&conveni_id=" + conveniId
                + "&conveni_prov_id=" + conveniProvId
                + "&hash_code=" + hashCode
                + "&tipus_document=CONVENBAIXA"
                + "&curs_seleccio=" + cursSeleccio
                + "&language=" + language;
    }

    public String ref06(String conveniId, String conveniProvId,
                        String estudiId, String language) {
        return base + "/modules/ConveniPla?moduleaction=documentPDF"
                + "&codi_conveni=" + conveniId
                + "&codi_conveni_provisional=" + conveniProvId
                + "&estudiId=" + estudiId
                + "&language=" + language;
    }

    public String ref19(String quadernId, String language) {
        return base + "/modules/QuadernManagement?moduleaction=documentValoracio"
                + "&quadernId=" + quadernId
                + "&language=" + language;
    }

    public String ref20(String quadernId, String language) {
        return base + "/modules/QuadernManagement?moduleaction=document"
                + "&quadernId=" + quadernId
                + "&language=" + language;
    }

    public String ref22(String quadernId, String language) {
        return base + "/modules/QuadernManagement?moduleaction=documentExpedient"
                + "&quadernId=" + quadernId
                + "&language=" + language;
    }

    public String ref15(String conveniId, String conveniProvId,
                        String codVisita, String language) {
        return base + "/modules/Fct?moduleaction=document"
                + "&codi_conveni=" + conveniId
                + "&codi_conveni_provisional=" + conveniProvId
                + "&cod_visita=" + codVisita
                + "&docType=VALOR_AVALUACIO"
                + "&language=" + language;
    }

    public String valoracion(String conveniId, String conveniProvId,
                             String codVisitaValoracion, String codVisita) {
        return base + "/modules/Fct?moduleaction=showValoracioAvaluacio"
                + "&cod_visita_valoracio=" + codVisitaValoracion
                + "&cod_visita=" + codVisita
                + "&codi_conveni=" + conveniId
                + "&codi_conveni_provisional=" + conveniProvId;
    }

    public String fichaAlumno(String codAlumno) {
        return base + "/modules/Alumne?moduleaction=detallAlumne&cod_alumne_pk=" + codAlumno;
    }

    public String empresa(String codEmpresa) {
        return base + "/modules/PropostaEntitat?moduleaction=doDetallEmpresa&cod_empresa_pk=" + codEmpresa;
    }

    public String centroTrabajo(String codEmpresa, String codCentro) {
        return base + "/modules/PropostaEntitat?moduleaction=doDetall"
                + "&cod_empresa_pk=" + codEmpresa
                + "&cod_centre_treball_pk=" + codCentro;
    }

    /** Tab de responsables de acuerdo — carga AJAX, responde HTML parcial */
    public String tabResponsablesCT(String codCentro, String versioCT) {
        return base + "/modules/PropostaEntitat?moduleaction=tabRespConveniAjax"
                + "&centreTreballPk=" + codCentro
                + "&centreTreballVersio=" + versioCT;
    }

    /** URL base del formulario de plan de actividades (para POST de guardar/firmar). */
    public String guardarPlanActividades() {
        return base + "/modules/ConveniPla";
    }

    public String planActividades(String codConvenio, String codTemporal, String newSystem) {
        return base + "/modules/ConveniPla?moduleaction=show"
                + "&codi_conveni=" + codConvenio
                + "&codi_conveni_provisional=" + codTemporal
                + "&newSystem=" + newSystem
                + "&preomplir=NO";
    }

    public String actividadDiaria(String codConvenio, String codTemporal,
                                  String fecha, String hashCode, String conveniActivitatPk) {
        String url = base + "/modules/ActivitatFct?moduleaction=show";
        if (conveniActivitatPk != null && !conveniActivitatPk.isBlank()) {
            url += "&conveni_activitat_pk=" + conveniActivitatPk;
        }
        url += "&codi_conveni=" + codConvenio
                + "&codi_conveni_provisional=" + codTemporal
                + "&data=" + fecha
                + "&hash_code=" + hashCode;
        return url;
    }
}

