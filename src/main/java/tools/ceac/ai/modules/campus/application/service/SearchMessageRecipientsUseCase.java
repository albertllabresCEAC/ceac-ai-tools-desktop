package tools.ceac.ai.modules.campus.application.service;

import java.io.IOException;
import java.util.List;
import tools.ceac.ai.modules.campus.application.port.out.CampusGateway;
import tools.ceac.ai.modules.campus.domain.exception.AuthenticationRequiredException;
import tools.ceac.ai.modules.campus.domain.model.DashboardSnapshot;
import tools.ceac.ai.modules.campus.domain.model.MessageRecipient;
import tools.ceac.ai.modules.campus.infrastructure.campus.MoodleSearchUsersParser;
import org.springframework.stereotype.Service;

/**
 * Searches campus users via Moodle's core_message_message_search_users AJAX API.
 */
@Service
public class SearchMessageRecipientsUseCase {

    private final CampusGateway campusGateway;
    private final CampusSessionService sessionService;
    private final GetDashboardUseCase getDashboardUseCase;
    private final MoodleSearchUsersParser parser;

    public SearchMessageRecipientsUseCase(
            CampusGateway campusGateway,
            CampusSessionService sessionService,
            GetDashboardUseCase getDashboardUseCase,
            MoodleSearchUsersParser parser) {
        this.campusGateway = campusGateway;
        this.sessionService = sessionService;
        this.getDashboardUseCase = getDashboardUseCase;
        this.parser = parser;
    }

    public List<MessageRecipient> execute(String query) {
        return execute(query, 51, 0);
    }

    public List<MessageRecipient> execute(String query, int limitNum, int limitFrom) {
        String normalizedQuery = query == null ? "" : query.trim();
        if (normalizedQuery.isBlank()) {
            throw new IllegalStateException("search_query_required");
        }
        if (!sessionService.isAuthenticated()) {
            throw new AuthenticationRequiredException("not_authenticated");
        }
        DashboardSnapshot dashboard = getDashboardUseCase.execute();
        String sesskey = sessionService.getSesskey();
        if (sesskey.isBlank()) {
            sesskey = dashboard.sesskey();
        }
        if (sesskey == null || sesskey.isBlank()) {
            throw new IllegalStateException("sesskey_not_available");
        }
        if (dashboard.userId() == null || dashboard.userId().isBlank()) {
            throw new IllegalStateException("current_user_id_not_available");
        }
        try {
            String json = campusGateway.searchMessageUsers(
                    dashboard.userId(),
                    sesskey,
                    normalizedQuery,
                    Math.max(1, limitNum),
                    Math.max(0, limitFrom)
            ).body();
            return parser.parseMerged(json);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("search_message_users_failed", e);
        } catch (IOException e) {
            throw new IllegalStateException("search_message_users_failed", e);
        }
    }
}
