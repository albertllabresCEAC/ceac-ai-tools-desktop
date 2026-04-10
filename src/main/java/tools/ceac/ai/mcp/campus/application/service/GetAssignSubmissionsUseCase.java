package tools.ceac.ai.mcp.campus.application.service;

import tools.ceac.ai.mcp.campus.application.port.out.CampusGateway;
import tools.ceac.ai.mcp.campus.domain.exception.AuthenticationRequiredException;
import tools.ceac.ai.mcp.campus.domain.model.SubmissionSummary;
import tools.ceac.ai.mcp.campus.infrastructure.campus.AssignGradingPage;
import tools.ceac.ai.mcp.campus.infrastructure.campus.MoodleAssignGradingParser;
import tools.ceac.ai.mcp.campus.infrastructure.config.CampusProperties;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

/**
 * Lists all student submissions for a given assignment module.
 * If the grading page is showing a limited number of rows, it first submits
 * the "show all" option (perpage=-1) before scraping the full list.
 */
@Service
public class GetAssignSubmissionsUseCase {

    private final CampusGateway campusGateway;
    private final CampusSessionService sessionService;
    private final MoodleAssignGradingParser gradingParser;
    private final CampusProperties properties;

    public GetAssignSubmissionsUseCase(
            CampusGateway campusGateway,
            CampusSessionService sessionService,
            MoodleAssignGradingParser gradingParser,
            CampusProperties properties
    ) {
        this.campusGateway = campusGateway;
        this.sessionService = sessionService;
        this.gradingParser = gradingParser;
        this.properties = properties;
    }

    public List<SubmissionSummary> execute(String assignId) {
        if (!sessionService.isAuthenticated()) {
            throw new AuthenticationRequiredException("not_authenticated");
        }
        try {
            String html = campusGateway.getAssignGradingPage(assignId).body();
            AssignGradingPage page = gradingParser.parse(html, properties.baseUrl());

            if (page.currentPerpage() != -1) {
                campusGateway.postAssignSaveOptions(assignId, page.contextId(), page.formUserId(), page.sesskey());
                html = campusGateway.getAssignGradingPage(assignId).body();
                page = gradingParser.parse(html, properties.baseUrl());
            }

            return page.submissions();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("submissions_fetch_failed", e);
        } catch (IOException e) {
            throw new IllegalStateException("submissions_fetch_failed", e);
        }
    }
}
