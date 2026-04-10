package tools.ceac.ai.modules.outlook.domain.model;

import java.time.OffsetDateTime;

public record McpSearchResult(
        String entryId,
        String folder,
        String subject,
        String senderName,
        String senderEmail,
        String snippet,
        boolean unread,
        OffsetDateTime receivedAt,
        OffsetDateTime sentAt
) {
}


