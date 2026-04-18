package tools.ceac.ai.modules.outlook.domain.model;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Result entry returned by the Outlook message search operation.
 *
 * <p>Each instance represents a single message that matched the search criteria.
 * Unlike {@link MailMessage} (used by the listing endpoint), this record includes the
 * {@code folder} the message was found in, necessary because search can span multiple
 * folders simultaneously, but omits {@code htmlBody}, which is not populated during
 * search.</p>
 *
 * <ul>
 *   <li>{@code body} contains the full plain-text body of the message.</li>
 *   <li>{@code attachments} contains metadata (index, fileName, size) for each attachment;
 *       empty list when there are none. Use {@code getAttachment} to download content.</li>
 *   <li>Date fields ({@code receivedAt}, {@code sentAt}) are ISO-8601 strings with UTC offset.</li>
 * </ul>
 */
public record MessageSearchResult(
        String entryId,
        String folder,
        String subject,
        String senderName,
        String senderEmail,
        String to,
        String cc,
        String bcc,
        String body,
        boolean unread,
        OffsetDateTime receivedAt,
        OffsetDateTime sentAt,
        List<AttachmentInfo> attachments
) {
}
