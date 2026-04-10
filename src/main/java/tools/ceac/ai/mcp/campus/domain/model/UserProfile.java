package tools.ceac.ai.mcp.campus.domain.model;

import java.util.List;

/** Public profile of a Moodle user, scraped from /user/profile.php?id={userId}. */
public record UserProfile(
        String userId,
        String fullName,
        String email,
        String country,
        String timezone,
        List<CourseRef> courses,
        String firstAccess,
        String lastAccess
) {}