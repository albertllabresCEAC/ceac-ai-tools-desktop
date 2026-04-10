package tools.ceac.ai.mcp.campus.domain.model;

/** A reference to a Moodle course (id + name), as seen from a user's profile. */
public record CourseRef(
        String courseId,
        String courseName
) {}