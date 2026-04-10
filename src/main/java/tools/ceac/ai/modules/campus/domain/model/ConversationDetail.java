package tools.ceac.ai.modules.campus.domain.model;

import java.util.List;

/** Full conversation with all its members and messages. */
public record ConversationDetail(
        long id,
        List<ConversationMember> members,
        List<ConversationMessage> messages
) {}

