package tools.ceac.ai.modules.campus.application.service;

import tools.ceac.ai.modules.campus.application.port.out.CampusGateway;
import tools.ceac.ai.modules.campus.domain.exception.AuthenticationRequiredException;
import tools.ceac.ai.modules.campus.domain.model.AssignDetail;
import tools.ceac.ai.modules.campus.infrastructure.campus.MoodleAssignModEditParser;
import tools.ceac.ai.modules.campus.infrastructure.config.CampusProperties;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Recupera los detalles de configuración de una tarea Moodle scrapeando
 * {@code /course/modedit.php?update={cmid}&return=1}.
 */
@Service
public class GetAssignDetailUseCase {

    private final CampusGateway campusGateway;
    private final CampusSessionService sessionService;
    private final MoodleAssignModEditParser parser;
    private final CampusProperties properties;

    public GetAssignDetailUseCase(CampusGateway campusGateway,
                                  CampusSessionService sessionService,
                                  MoodleAssignModEditParser parser,
                                  CampusProperties properties) {
        this.campusGateway = campusGateway;
        this.sessionService = sessionService;
        this.parser = parser;
        this.properties = properties;
    }

    public AssignDetail execute(String cmid) {
        if (!sessionService.isAuthenticated()) {
            throw new AuthenticationRequiredException("not_authenticated");
        }
        try {
            String html = campusGateway.getAssignModEdit(cmid).body();
            return parser.parse(html, properties.baseUrl());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("assign_detail_fetch_failed", e);
        } catch (IOException e) {
            throw new IllegalStateException("assign_detail_fetch_failed", e);
        }
    }
}
