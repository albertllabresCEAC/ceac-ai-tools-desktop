package tools.ceac.ai.mcp.campus.application.service;

import tools.ceac.ai.mcp.campus.application.port.out.CampusGateway;
import tools.ceac.ai.mcp.campus.domain.exception.AuthenticationRequiredException;
import tools.ceac.ai.mcp.campus.domain.model.UserProfile;
import tools.ceac.ai.mcp.campus.infrastructure.campus.MoodleUserProfileParser;
import tools.ceac.ai.mcp.campus.infrastructure.config.CampusProperties;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Retrieves the public profile of a Moodle user by scraping /user/profile.php?id={userId}.
 */
@Service
public class GetUserProfileUseCase {

    private final CampusGateway campusGateway;
    private final CampusSessionService sessionService;
    private final MoodleUserProfileParser userProfileParser;
    private final CampusProperties properties;

    public GetUserProfileUseCase(
            CampusGateway campusGateway,
            CampusSessionService sessionService,
            MoodleUserProfileParser userProfileParser,
            CampusProperties properties
    ) {
        this.campusGateway = campusGateway;
        this.sessionService = sessionService;
        this.userProfileParser = userProfileParser;
        this.properties = properties;
    }

    public UserProfile execute(String userId) {
        if (!sessionService.isAuthenticated()) {
            throw new AuthenticationRequiredException("not_authenticated");
        }
        try {
            String html = campusGateway.getUserProfile(userId).body();
            return userProfileParser.parse(html, properties.baseUrl(), userId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("user_profile_fetch_failed", e);
        } catch (IOException e) {
            throw new IllegalStateException("user_profile_fetch_failed", e);
        }
    }
}