package com.alber.outlookdesktop.model;

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
