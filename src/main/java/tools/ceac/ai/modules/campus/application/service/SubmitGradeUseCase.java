package tools.ceac.ai.modules.campus.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import tools.ceac.ai.modules.campus.application.port.out.CampusGateway;
import tools.ceac.ai.modules.campus.domain.exception.AuthenticationRequiredException;
import tools.ceac.ai.modules.campus.domain.model.GraderInfo;
import tools.ceac.ai.modules.campus.infrastructure.campus.MoodleGraderPageParser;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Submits a grade and optional feedback for a student's assignment submission.
 * <p>
 * Workflow: GET grader page â†’ extract assignmentId â†’ GET unused draft itemid
 * â†’ build JSON form data â†’ POST to {@code mod_assign_submit_grading_form}.
 * </p>
 */
@Service
public class SubmitGradeUseCase {

    private final CampusGateway campusGateway;
    private final CampusSessionService sessionService;
    private final MoodleGraderPageParser graderPageParser;
    private final ObjectMapper objectMapper;

    public SubmitGradeUseCase(
            CampusGateway campusGateway,
            CampusSessionService sessionService,
            MoodleGraderPageParser graderPageParser,
            ObjectMapper objectMapper
    ) {
        this.campusGateway = campusGateway;
        this.sessionService = sessionService;
        this.graderPageParser = graderPageParser;
        this.objectMapper = objectMapper;
    }

    public void execute(String moduleId, String studentUserId, int grade, String feedback, boolean sendNotification) {
        if (!sessionService.isAuthenticated()) {
            throw new AuthenticationRequiredException("not_authenticated");
        }
        String sesskey = sessionService.getSesskey();
        if (sesskey.isBlank()) {
            throw new IllegalStateException("sesskey_not_available");
        }
        try {
            GraderInfo info = graderPageParser.parse(
                    campusGateway.getGraderPage(moduleId, studentUserId).body());

            String itemId = extractItemId(
                    campusGateway.getUnusedDraftItemId(sesskey).body());

            String jsonFormData = buildJsonFormData(moduleId, studentUserId, grade,
                    feedback != null ? feedback : "", sendNotification, itemId, sesskey);

            campusGateway.submitGrade(info.assignmentId(), studentUserId, jsonFormData, sesskey);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("grade_submit_failed", e);
        } catch (IOException e) {
            throw new IllegalStateException("grade_submit_failed", e);
        }
    }

    private String extractItemId(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode data = root.get(0).path("data");
            if (data.has("itemid")) return data.path("itemid").asText();
            return data.asText();
        } catch (Exception e) {
            throw new IllegalStateException("draft_itemid_parse_failed", e);
        }
    }

    private String buildJsonFormData(String moduleId, String studentUserId, int grade,
                                     String feedback, boolean sendNotification,
                                     String itemId, String sesskey) {
        String encodedFeedback = URLEncoder.encode(feedback, StandardCharsets.UTF_8);
        String urlEncoded = "editpdf_source_userid=" + studentUserId
                + "&id=" + moduleId
                + "&rownum=0"
                + "&useridlistid="
                + "&attemptnumber=-1"
                + "&ajax=0"
                + "&userid=0"
                + "&sendstudentnotifications=" + sendNotification
                + "&action=submitgrade"
                + "&sesskey=" + sesskey
                + "&_qf__mod_assign_grade_form_" + studentUserId + "=1"
                + "&grade=" + grade
                + "&assignfeedbackcomments_editor%5Btext%5D=" + encodedFeedback
                + "&assignfeedbackcomments_editor%5Bformat%5D=1"
                + "&assignfeedbackcomments_editor%5Bitemid%5D=" + itemId
                + "&assignfeedback_editpdf_haschanges=false";
        // Moodle expects jsonformdata as a URL-encoded string wrapped in literal quotes
        return "\"" + urlEncoded + "\"";
    }
}


