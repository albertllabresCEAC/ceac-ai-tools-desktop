package tools.ceac.ai.modules.campus.application.service;

import tools.ceac.ai.modules.campus.application.port.out.CampusGateway;
import tools.ceac.ai.modules.campus.domain.exception.AuthenticationRequiredException;
import tools.ceac.ai.modules.campus.domain.model.QuizUserOverride;
import tools.ceac.ai.modules.campus.infrastructure.campus.MoodleQuizOverridesParser;
import tools.ceac.ai.modules.campus.infrastructure.config.CampusProperties;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

/**
 * Lists all user overrides configured for a given quiz module,
 * scraping the {@code /mod/quiz/overrides.php?cmid=...&mode=user} page.
 */
@Service
public class GetQuizUserOverridesUseCase {

    private final CampusGateway campusGateway;
    private final CampusSessionService sessionService;
    private final MoodleQuizOverridesParser parser;
    private final CampusProperties properties;

    public GetQuizUserOverridesUseCase(
            CampusGateway campusGateway,
            CampusSessionService sessionService,
            MoodleQuizOverridesParser parser,
            CampusProperties properties
    ) {
        this.campusGateway = campusGateway;
        this.sessionService = sessionService;
        this.parser = parser;
        this.properties = properties;
    }

    public List<QuizUserOverride> execute(String cmid) {
        if (!sessionService.isAuthenticated()) {
            throw new AuthenticationRequiredException("not_authenticated");
        }
        try {
            String html = campusGateway.getQuizUserOverrides(cmid).body();
            return parser.parse(html, properties.baseUrl());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("quiz_overrides_fetch_failed", e);
        } catch (IOException e) {
            throw new IllegalStateException("quiz_overrides_fetch_failed", e);
        }
    }
}

