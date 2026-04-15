package tools.ceac.ai.modules.campus.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import tools.ceac.ai.modules.campus.application.port.out.CampusGateway;
import tools.ceac.ai.modules.campus.domain.model.ActivitySummary;
import tools.ceac.ai.modules.campus.domain.model.CourseDetail;
import tools.ceac.ai.modules.campus.domain.model.SectionSummary;
import tools.ceac.ai.modules.campus.infrastructure.campus.MoodleModEditFormFieldExtractor;
import tools.ceac.ai.modules.campus.infrastructure.config.CampusProperties;
import tools.ceac.ai.modules.campus.interfaces.api.dto.CreateCourseAssignmentRequest;
import tools.ceac.ai.modules.campus.interfaces.api.dto.CreateCourseAssignmentResponse;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.net.ssl.SSLSession;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CreateCourseAssignmentUseCaseTest {

    private CampusGateway campusGateway;
    private StubCampusSessionService sessionService;
    private StubGetCourseUseCase getCourseUseCase;

    private CreateCourseAssignmentUseCase useCase;

    @BeforeEach
    void setUp() {
        campusGateway = mock(CampusGateway.class);
        sessionService = new StubCampusSessionService();
        getCourseUseCase = new StubGetCourseUseCase();
        CampusProperties properties = new CampusProperties(
                "https://campus.ceacfp.es",
                "https://campus.ceacfp.es/login/index.php",
                "/my/",
                30,
                Path.of("target/test-jcef-install"),
                Path.of("target/test-jcef-cache"),
                new CampusProperties.Ui(false, true, true, true, false)
        );
        useCase = new CreateCourseAssignmentUseCase(
                campusGateway,
                sessionService,
                new MoodleModEditFormFieldExtractor(),
                properties,
                getCourseUseCase
        );
    }

    @Test
    void createsAssignmentAndResolvesCreatedActivityFromCourseState() throws Exception {
        String courseId = "8202";
        String formHtml = """
                <html>
                  <body>
                    <form class="mform" action="https://campus.ceacfp.es/course/modedit.php">
                      <input type="hidden" name="sesskey" value="abc123">
                      <input type="hidden" name="coursemodule" value="">
                      <input type="hidden" name="module" value="1">
                      <input type="hidden" name="modulename" value="assign">
                      <input type="hidden" name="instance" value="">
                      <input type="hidden" name="add" value="assign">
                      <input type="hidden" name="_qf__mod_assign_mod_form" value="1">
                      <input type="hidden" name="course" value="8202">
                      <input type="hidden" name="section" value="3">
                      <input type="hidden" name="update" value="0">
                      <input type="hidden" name="return" value="0">
                      <input type="hidden" name="sr" value="0">
                      <input type="hidden" name="beforemod" value="0">
                      <input type="hidden" name="introeditor[format]" value="1">
                      <input type="hidden" name="introeditor[itemid]" value="111">
                      <input type="hidden" name="activityeditor[format]" value="1">
                      <input type="hidden" name="activityeditor[itemid]" value="222">
                      <input type="hidden" name="tags" value="_qf__force_multiselect_submission">
                      <input type="hidden" name="availabilityconditionsjson" value="{&quot;op&quot;:&quot;&amp;&quot;,&quot;c&quot;:[],&quot;showc&quot;:[]}">
                      <input type="hidden" name="completion" value="0">
                      <input type="text" name="name" value="Valor inicial">
                      <textarea name="introeditor[text]"></textarea>
                      <textarea name="activityeditor[text]"></textarea>
                      <select name="tags[]" multiple></select>
                      <select name="visible">
                        <option value="0">No</option>
                        <option value="1" selected>Si</option>
                      </select>
                      <select name="sendnotifications">
                        <option value="0" selected>No</option>
                        <option value="1">Si</option>
                      </select>
                      <select name="sendlatenotifications">
                        <option value="0" selected>No</option>
                        <option value="1">Si</option>
                      </select>
                      <select name="sendstudentnotifications">
                        <option value="0">No</option>
                        <option value="1" selected>Si</option>
                      </select>
                    </form>
                  </body>
                </html>
                """;

        HttpResponse<String> formResponse = httpResponse(
                formHtml,
                "https://campus.ceacfp.es/course/mod.php?id=8202&add=assign&section=3"
        );
        HttpResponse<String> createResponse = httpResponse(
                "<html><body>ok</body></html>",
                "https://campus.ceacfp.es/course/view.php?id=8202"
        );

        sessionService.setAuthenticated(true);
        sessionService.setSesskey("");
        when(campusGateway.getCourseModEditForm(courseId, "3", "assign")).thenReturn(formResponse);
        when(campusGateway.looksAuthenticated(formResponse)).thenReturn(true);
        when(campusGateway.postCourseModEdit(org.mockito.ArgumentMatchers.anyMap())).thenReturn(createResponse);
        when(campusGateway.looksAuthenticated(createResponse)).thenReturn(true);
        getCourseUseCase.setCourseDetail(new CourseDetail(
                courseId,
                5,
                List.of(new SectionSummary(
                        "42",
                        "Tema 3",
                        3,
                        true,
                        "https://campus.ceacfp.es/course/view.php?id=8202&section=3",
                        List.of(new ActivitySummary(
                                "316309",
                                "Actividad final del modulo",
                                "assign",
                                "https://campus.ceacfp.es/mod/assign/view.php?id=316309",
                                true
                        )),
                        List.of()
                ))
        ));

        CreateCourseAssignmentResponse response = useCase.execute(courseId, new CreateCourseAssignmentRequest(
                3,
                "Actividad final del modulo",
                "<p>Lee las instrucciones y entrega la actividad final.</p>",
                "<p>Entrega un PDF con el desarrollo del caso practico.</p>",
                "2026-04-20T08:00",
                "2026-04-30T23:59",
                "2026-05-02T23:59",
                "2026-05-07T23:59",
                true,
                false,
                false,
                true,
                false,
                true
        ));

        ArgumentCaptor<Map<String, String>> paramsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(campusGateway).postCourseModEdit(paramsCaptor.capture());
        Map<String, String> params = paramsCaptor.getValue();

        assertThat(params)
                .containsEntry("coursemodule", "0")
                .containsEntry("instance", "0")
                .containsEntry("name", "Actividad final del modulo")
                .containsEntry("introeditor[text]", "<p>Lee las instrucciones y entrega la actividad final.</p>")
                .containsEntry("activityeditor[text]", "<p>Entrega un PDF con el desarrollo del caso practico.</p>")
                .containsEntry("mform_isexpanded_id_submissiontypes", "1")
                .containsEntry("mform_isexpanded_id_feedbacktypes", "0")
                .containsEntry("mform_isexpanded_id_modstandardgrade", "0")
                .containsEntry("visible", "1")
                .containsEntry("sendnotifications", "1")
                .containsEntry("sendlatenotifications", "0")
                .containsEntry("sendstudentnotifications", "1")
                .containsEntry("tags", "_qf__force_multiselect_submission")
                .containsEntry("duedate[enabled]", "1")
                .containsEntry("duedate[day]", "30")
                .containsEntry("duedate[month]", "4")
                .containsEntry("duedate[year]", "2026")
                .containsEntry("duedate[hour]", "23")
                .containsEntry("duedate[minute]", "59")
                .containsEntry("submitbutton2", "Guardar cambios y regresar al curso");
        assertThat(params).doesNotContainKey("tags[]");

        assertThat(sessionService.getSesskey()).isEqualTo("abc123");
        assertThat(response.assignmentId()).isEqualTo("316309");
        assertThat(response.assignmentUrl()).isEqualTo("https://campus.ceacfp.es/mod/assign/view.php?id=316309");
        assertThat(response.courseUrl()).isEqualTo("https://campus.ceacfp.es/course/view.php?id=8202");
    }

    @Test
    void rejectsInvalidDueDateFormat() throws Exception {
        sessionService.setAuthenticated(true);
        HttpResponse<String> formResponse = httpResponse(
                """
                <html><body><form class="mform" action="https://campus.ceacfp.es/course/modedit.php">
                  <input type="hidden" name="sesskey" value="abc123">
                </form></body></html>
                """,
                "https://campus.ceacfp.es/course/mod.php?id=8202&add=assign&section=3"
        );
        when(campusGateway.getCourseModEditForm("8202", "3", "assign")).thenReturn(formResponse);
        when(campusGateway.looksAuthenticated(formResponse)).thenReturn(true);
        sessionService.setSesskey("abc123");

        assertThatThrownBy(() -> useCase.execute("8202", new CreateCourseAssignmentRequest(
                3,
                "Actividad final del modulo",
                null,
                null,
                null,
                "30/04/2026 23:59",
                null,
                null,
                true,
                false,
                false,
                true,
                false,
                true
        )))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("duedate_invalid_datetime");
    }

    private HttpResponse<String> httpResponse(String body, String url) {
        return new StubHttpResponse(200, body, URI.create(url));
    }

    private static final class StubCampusSessionService extends CampusSessionService {
        private boolean authenticated;
        private String sesskey = "";

        private StubCampusSessionService() {
            super(null);
        }

        private void setAuthenticated(boolean authenticated) {
            this.authenticated = authenticated;
        }

        private void setSesskey(String sesskey) {
            this.sesskey = sesskey == null ? "" : sesskey;
        }

        @Override
        public void storeSesskey(String sesskey) {
            setSesskey(sesskey);
        }

        @Override
        public void storeSesskeyFromHtml(String html) {
            if (html != null && html.contains("sesskey")) {
                setSesskey("abc123");
            }
        }

        @Override
        public String getSesskey() {
            return sesskey;
        }

        @Override
        public boolean isAuthenticated() {
            return authenticated;
        }
    }

    private static final class StubGetCourseUseCase extends GetCourseUseCase {
        private CourseDetail courseDetail;

        private StubGetCourseUseCase() {
            super(null, null, null);
        }

        private void setCourseDetail(CourseDetail courseDetail) {
            this.courseDetail = courseDetail;
        }

        @Override
        public CourseDetail execute(String courseId) {
            return courseDetail;
        }
    }

    private record StubHttpResponse(int statusCode, String body, URI uri) implements HttpResponse<String> {

        @Override
        public HttpRequest request() {
            return null;
        }

        @Override
        public Optional<HttpResponse<String>> previousResponse() {
            return Optional.empty();
        }

        @Override
        public HttpHeaders headers() {
            return HttpHeaders.of(Map.of(), (name, value) -> true);
        }

        @Override
        public Optional<SSLSession> sslSession() {
            return Optional.empty();
        }

        @Override
        public HttpClient.Version version() {
            return HttpClient.Version.HTTP_1_1;
        }
    }
}
