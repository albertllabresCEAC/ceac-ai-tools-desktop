package tools.ceac.ai.modules.campus.application.service;

import tools.ceac.ai.modules.campus.application.port.out.CampusGateway;
import tools.ceac.ai.modules.campus.domain.exception.AuthenticationRequiredException;
import tools.ceac.ai.modules.campus.domain.model.CourseDetail;
import tools.ceac.ai.modules.campus.infrastructure.campus.MoodleCourseStateParser;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.http.HttpResponse;

/**
 * Retrieves the full structure of a Moodle course (sections and activities)
 * via the {@code core_courseformat_get_state} AJAX API.
 */
@Service
public class GetCourseUseCase {

    private final CampusGateway campusGateway;
    private final CampusSessionService sessionService;
    private final MoodleCourseStateParser courseStateParser;

    public GetCourseUseCase(CampusGateway campusGateway, CampusSessionService sessionService, MoodleCourseStateParser courseStateParser) {
        this.campusGateway = campusGateway;
        this.sessionService = sessionService;
        this.courseStateParser = courseStateParser;
    }

    public CourseDetail execute(String courseId) {
        if (!sessionService.isAuthenticated()) {
            throw new AuthenticationRequiredException("not_authenticated");
        }
        String sesskey = sessionService.getSesskey();
        if (sesskey.isBlank()) {
            throw new IllegalStateException("sesskey_not_available");
        }
        try {
            HttpResponse<String> response = campusGateway.getCourseState(courseId, sesskey);
            return courseStateParser.parse(response.body());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("course_fetch_failed", e);
        } catch (IOException e) {
            throw new IllegalStateException("course_fetch_failed", e);
        }
    }
}


