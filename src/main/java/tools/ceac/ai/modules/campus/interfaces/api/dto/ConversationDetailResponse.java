package tools.ceac.ai.modules.campus.interfaces.api.dto;

import java.util.List;

public record ConversationDetailResponse(
        long id,
        List<ConversationMemberResponse> members,
        List<ConversationMessageResponse> messages
) {}

