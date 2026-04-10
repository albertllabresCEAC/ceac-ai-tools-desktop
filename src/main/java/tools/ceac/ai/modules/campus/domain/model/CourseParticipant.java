package tools.ceac.ai.modules.campus.domain.model;

/** A participant (student or teacher) enrolled in a Moodle course. */
public record CourseParticipant(
        String userId,
        String fullName,
        String email,
        String roles,
        String groups,
        String lastAccess
) {}

