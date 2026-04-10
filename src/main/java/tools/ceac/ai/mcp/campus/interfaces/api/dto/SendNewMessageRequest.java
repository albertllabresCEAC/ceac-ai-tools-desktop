package tools.ceac.ai.mcp.campus.interfaces.api.dto;

import java.util.List;

public record SendNewMessageRequest(
        List<String> recipientIds,
        String subject,
        String content
) {}