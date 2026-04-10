package tools.ceac.ai.modules.campus.application.service;

import tools.ceac.ai.modules.campus.application.port.out.CampusGateway;
import tools.ceac.ai.modules.campus.domain.exception.AuthenticationRequiredException;
import tools.ceac.ai.modules.campus.domain.model.CourseParticipant;
import tools.ceac.ai.modules.campus.infrastructure.campus.MoodleCourseParticipantsParser;
import tools.ceac.ai.modules.campus.infrastructure.config.CampusProperties;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

/**
 * Retrieves the list of participants enrolled in a Moodle course
 * by scraping /user/index.php?id={courseId}.
 */
@Service
public class GetCourseParticipantsUseCase {

    private final CampusGateway campusGateway;
    private final CampusSessionService sessionService;
    private final MoodleCourseParticipantsParser participantsParser;
    private final CampusProperties properties;

    public GetCourseParticipantsUseCase(
            CampusGateway campusGateway,
            CampusSessionService sessionService,
            MoodleCourseParticipantsParser participantsParser,
            CampusProperties properties
    ) {
        this.campusGateway = campusGateway;
        this.sessionService = sessionService;
        this.participantsParser = participantsParser;
        this.properties = properties;
    }

    public List<CourseParticipant> execute(String courseId) {
        if (!sessionService.isAuthenticated()) {
            throw new AuthenticationRequiredException("not_authenticated");
        }
        try {
            String html = campusGateway.getCourseParticipants(courseId).body();
            return participantsParser.parse(html, properties.baseUrl());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("participants_fetch_failed", e);
        } catch (IOException e) {
            throw new IllegalStateException("participants_fetch_failed", e);
        }
    }
}

