package tools.ceac.ai.mcp.campus.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import tools.ceac.ai.mcp.campus.application.port.out.CampusGateway;
import tools.ceac.ai.mcp.campus.domain.exception.AuthenticationRequiredException;
import tools.ceac.ai.mcp.campus.domain.model.GradeInfo;
import tools.ceac.ai.mcp.campus.infrastructure.campus.MoodleGraderPageParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Retrieves the current grade and written feedback for a student's assignment submission.
 * Uses the {@code core_get_fragment} AJAX call to obtain the grading panel HTML,
 * then extracts the grade input and feedback textarea.
 */
@Service
public class GetGradeUseCase {

    private final CampusGateway campusGateway;
    private final CampusSessionService sessionService;
    private final MoodleGraderPageParser graderPageParser;
    private final ObjectMapper objectMapper;

    public GetGradeUseCase(
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

    public GradeInfo execute(String moduleId, String studentUserId) {
        if (!sessionService.isAuthenticated()) {
            throw new AuthenticationRequiredException("not_authenticated");
        }
        String sesskey = sessionService.getSesskey();
        if (sesskey.isBlank()) {
            throw new IllegalStateException("sesskey_not_available");
        }
        try {
            String graderHtml = campusGateway.getGraderPage(moduleId, studentUserId).body();
            String contextId = graderPageParser.parse(graderHtml).contextId();

            String json = campusGateway.getCoreFragment(contextId, studentUserId, sesskey).body();
            return parseGradeInfo(json);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("grade_fetch_failed", e);
        } catch (IOException e) {
            throw new IllegalStateException("grade_fetch_failed", e);
        }
    }

    private GradeInfo parseGradeInfo(String json) throws IOException {
        JsonNode root = objectMapper.readTree(json).get(0);
        String html = root.path("data").path("html").asText("");

        Document doc = Jsoup.parse(html);

        Element gradeInput = doc.selectFirst("input[name=grade]");
        String grade = gradeInput != null ? gradeInput.attr("value") : "";

        Element feedbackArea = doc.selectFirst("textarea[name^=assignfeedbackcomments_editor]");
        String feedback = feedbackArea != null ? feedbackArea.text() : "";

        return new GradeInfo(grade, feedback);
    }
}
