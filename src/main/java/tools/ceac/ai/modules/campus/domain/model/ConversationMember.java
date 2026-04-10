package tools.ceac.ai.modules.campus.domain.model;

/** A participant in a Moodle conversation. */
public record ConversationMember(
        long id,
        String fullName,
        String profileImageUrl,
        Boolean isOnline,
        boolean isBlocked,
        boolean isContact
) {}

