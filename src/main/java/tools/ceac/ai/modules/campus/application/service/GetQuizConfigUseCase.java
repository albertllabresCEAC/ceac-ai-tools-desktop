package tools.ceac.ai.modules.campus.application.service;

import tools.ceac.ai.modules.campus.application.port.out.CampusGateway;
import tools.ceac.ai.modules.campus.domain.exception.AuthenticationRequiredException;
import tools.ceac.ai.modules.campus.domain.model.QuizConfig;
import tools.ceac.ai.modules.campus.infrastructure.campus.MoodleQuizModEditParser;
import tools.ceac.ai.modules.campus.infrastructure.config.CampusProperties;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Retrieves the configuration of a Moodle quiz module by scraping
 * {@code /course/modedit.php?update={cmid}&return=1}.
 */
@Service
public class GetQuizConfigUseCase {

    private final CampusGateway campusGateway;
    private final CampusSessionService sessionService;
    private final MoodleQuizModEditParser parser;
    private final CampusProperties properties;

    public GetQuizConfigUseCase(CampusGateway campusGateway,
                                CampusSessionService sessionService,
                                MoodleQuizModEditParser parser,
                                CampusProperties properties) {
        this.campusGateway = campusGateway;
        this.sessionService = sessionService;
        this.parser = parser;
        this.properties = properties;
    }

    public QuizConfig execute(String cmid) {
        if (!sessionService.isAuthenticated()) {
            throw new AuthenticationRequiredException("not_authenticated");
        }
        try {
            String html = campusGateway.getQuizModEdit(cmid).body();
            return parser.parse(html, properties.baseUrl());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("quiz_config_fetch_failed", e);
        } catch (IOException e) {
            throw new IllegalStateException("quiz_config_fetch_failed", e);
        }
    }
}

