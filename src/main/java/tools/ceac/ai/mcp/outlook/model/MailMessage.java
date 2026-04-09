package tools.ceac.ai.mcp.outlook.model;

import java.time.OffsetDateTime;
import java.util.List;

public record MailMessage(
        String entryId,
        String subject,
        String senderName,
        String senderEmail,
        String to,
        String cc,
        String bcc,
        String body,
        String htmlBody,
        boolean unread,
        OffsetDateTime receivedAt,
        OffsetDateTime sentAt,
        List<AttachmentInfo> attachments
) {
}
