package tools.ceac.ai.mcp.campus.interfaces.api.dto;

import java.util.List;

public record ConversationDetailResponse(
        long id,
        List<ConversationMemberResponse> members,
        List<ConversationMessageResponse> messages
) {}