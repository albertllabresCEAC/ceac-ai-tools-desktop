package tools.ceac.ai.modules.outlook.interfaces.mcp;

import tools.ceac.ai.modules.outlook.domain.model.AttachmentContent;
import tools.ceac.ai.modules.outlook.domain.model.AttachmentContentToolRequest;
import tools.ceac.ai.modules.outlook.domain.model.AttachmentInfo;
import tools.ceac.ai.modules.outlook.domain.model.AttachmentListToolRequest;
import tools.ceac.ai.modules.outlook.domain.model.AttachmentPayload;
import tools.ceac.ai.modules.outlook.domain.model.ComposeMode;
import tools.ceac.ai.modules.outlook.domain.model.CreateDraftRequest;
import tools.ceac.ai.modules.outlook.domain.model.ComposeMailToolRequest;
import tools.ceac.ai.modules.outlook.domain.model.DraftAttachmentToolRequest;
import tools.ceac.ai.modules.outlook.domain.model.DraftDispatchToolRequest;
import tools.ceac.ai.modules.outlook.domain.model.DraftDiscardToolRequest;
import tools.ceac.ai.modules.outlook.domain.model.FolderType;
import tools.ceac.ai.modules.outlook.domain.model.MailDraftResponse;
import tools.ceac.ai.modules.outlook.domain.model.MailMessage;
import tools.ceac.ai.modules.outlook.domain.model.MessageGetToolRequest;
import tools.ceac.ai.modules.outlook.domain.model.MessageListToolRequest;
import tools.ceac.ai.modules.outlook.domain.model.MessageQuery;
import tools.ceac.ai.modules.outlook.domain.model.MessageSearchRequest;
import tools.ceac.ai.modules.outlook.domain.model.MessageSearchResult;
import tools.ceac.ai.modules.outlook.domain.model.SendMailRequest;
import tools.ceac.ai.modules.outlook.domain.model.StatusResponse;
import tools.ceac.ai.modules.outlook.domain.model.UpdateDraftRequest;
import tools.ceac.ai.modules.outlook.domain.model.UpdateDraftToolRequest;
import tools.ceac.ai.modules.outlook.application.service.OutlookMailService;
import tools.ceac.ai.modules.outlook.domain.exception.OutlookComException;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;

@Component
public class OutlookMcpTools {

    private final OutlookMailService outlookService;

    public OutlookMcpTools(OutlookMailService outlookService) {
        this.outlookService = outlookService;
    }

    @Tool(description = """
            List Outlook Desktop messages from a folder, ordered by received date.

            REQUEST (all fields optional):
              folder      : Folder to list. Allowed values: INBOX, DRAFTS, SENT, OUTBOX, DELETED. Default: INBOX.
              limit       : Maximum number of messages to return (1-200). Default: 20.
              unreadOnly  : When true, returns only unread messages. Default: false.
              since       : Lower bound for ReceivedTime. ISO-8601 formats accepted: \
            "2026-04-10T00:00:00+02:00", "2026-04-10T00:00:00Z", "2026-04-10T00:00:00". \
            If omitted, defaults to 7 days ago.
              sortOrder   : Sort direction by received date. Allowed values: desc, asc. Default: desc.

            RESPONSE (array of message objects):
              entryId     : Opaque Outlook identifier. Use it as input for fetch, getMessage, listAttachments, getAttachment.
              subject     : Message subject.
              senderName  : Display name of the sender.
              senderEmail : SMTP address of the sender.
              to          : Primary recipients string.
              cc          : CC recipients string.
              bcc         : Always null in list (not available without full item load).
              body        : Full plain-text body.
              htmlBody    : Always null in list. Use fetch/getMessage to retrieve HTML.
              unread      : true if the message has not been read.
              receivedAt  : Received timestamp (ISO-8601 with offset).
              sentAt      : Always null in list.
              attachments : Array of { index (1-based), fileName, size (bytes) }. Empty array if none.
            """)
    public List<MailMessage> listMessages(MessageListToolRequest request) {
        MessageQuery query = new MessageQuery();
        if (request != null && request.getFolder() != null) {
            query.setFolder(parseFolder(request.getFolder()));
        }
        query.setLimit(request != null ? request.getLimit() : null);
        query.setUnreadOnly(request != null && Boolean.TRUE.equals(request.getUnreadOnly()));
        query.setSince(parseSince(request != null ? request.getSince() : null));
        query.setSortOrder(normalizeSortOrder(request != null ? request.getSortOrder() : null));
        return outlookService.listMessages(query);
    }

    @Tool(description = """
            Retrieve the full content of an Outlook Desktop message by its entryId.

            REQUEST:
              entryId : (required) Outlook entryId obtained from listMessages or search.

            RESPONSE:
              entryId     : Outlook identifier of the message.
              subject     : Message subject.
              senderName  : Display name of the sender.
              senderEmail : SMTP address of the sender.
              to          : Primary recipients string.
              cc          : CC recipients string.
              bcc         : BCC recipients string (populated when available).
              body        : Full plain-text body.
              htmlBody    : Full HTML body (may be empty if the message has no HTML part).
              unread      : true if the message has not been read.
              receivedAt  : Received timestamp (ISO-8601 with offset).
              sentAt      : Sent timestamp (ISO-8601 with offset).
              attachments : Array of { index (1-based), fileName, size (bytes) }. Empty array if none.
            """)
    public MailMessage getMessage(MessageGetToolRequest request) {
        return outlookService.getMessage(request.getEntryId());
    }

    @Tool(description = """
            Search Outlook Desktop messages by free text across one or more folders.
            Matching is performed against subject, sender name, sender email, recipients (To, CC, BCC) and body.

            REQUEST (all fields optional):
              query      : Free-text string to search for. If omitted, returns all messages matching the other filters.
              folder     : Folder to search. Allowed values: INBOX, DRAFTS, SENT, OUTBOX, DELETED, ALL. \
            Default: ALL (searches INBOX, SENT and DRAFTS simultaneously).
              limit      : Maximum number of results to return (1-200). Default: 20.
              unreadOnly : When true, restricts results to unread messages. Default: false.
              since      : Lower bound for ReceivedTime. ISO-8601 formats accepted: \
            "2026-04-10T00:00:00+02:00", "2026-04-10T00:00:00Z", "2026-04-10T00:00:00". \
            If omitted, no lower bound is applied.
              until      : Upper bound for ReceivedTime, inclusive. ISO-8601 formats accepted: \
            "2026-04-18T23:59:59+02:00", "2026-04-18T21:59:59Z", "2026-04-18T23:59:59". \
            If omitted, no upper bound is applied.

            RESPONSE (array of search result objects):
              entryId     : Outlook identifier. Use it as input for fetch, getMessage, listAttachments or getAttachment.
              folder      : Name of the folder where the message was found (INBOX, SENT, DRAFTS, etc.).
              subject     : Message subject.
              senderName  : Display name of the sender.
              senderEmail : SMTP address of the sender.
              to          : Primary recipients string as exposed by Outlook.
              cc          : CC recipients string as exposed by Outlook.
              bcc         : BCC recipients string as exposed by Outlook. Often empty on received messages.
              body        : Full plain-text body of the message.
              unread      : true if the message has not been read.
              receivedAt  : Received timestamp (ISO-8601 with offset).
              sentAt      : Sent timestamp (ISO-8601 with offset).
              attachments : Array of { index (1-based), fileName, size (bytes) }. Empty array if none.
            """)
    public List<MessageSearchResult> search(MessageSearchRequest request) {
        return outlookService.searchMessages(request != null ? request : new MessageSearchRequest());
    }

    @Tool(description = """
            Retrieve the full content of an Outlook Desktop message by its entryId.
            Equivalent to getMessage. Prefer this tool after a search call to read the full message.

            REQUEST:
              entryId : (required) Outlook entryId obtained from listMessages or search.

            RESPONSE: identical to getMessage —
              entryId, subject, senderName, senderEmail, to, cc, bcc, body (plain text), \
            htmlBody (HTML), unread, receivedAt (ISO-8601), sentAt (ISO-8601), \
            attachments [ { index (1-based), fileName, size (bytes) } ].
            """)
    public MailMessage fetch(MessageGetToolRequest request) {
        return getMessage(request);
    }

    @Tool(description = """
            Create an Outlook Desktop draft email and save it to the Drafts folder.

            REQUEST:
              mode            : (optional) Composition mode. Allowed values: NEW, REPLY, REPLY_ALL. Default: NEW.
              originalEntryId : (required when mode is REPLY or REPLY_ALL) entryId of the message to reply to.
              subject         : (required when mode is NEW) Message subject.
              to              : (required when mode is NEW) Primary recipients. Separate multiple addresses with semicolons.
              cc              : (optional) CC recipients. Separate multiple addresses with semicolons.
              bcc             : (optional) BCC recipients. Separate multiple addresses with semicolons.
              body            : (optional) Plain-text body. Use either body or htmlBody, not both.
              htmlBody        : (optional) HTML body. Takes precedence over body if both are provided.
              attachments     : (optional) Array of { fileName (required), base64Content (required), mediaType (optional) }.

            RESPONSE:
              entryId : Outlook entryId of the created draft. Use it for updateDraft, addAttachmentToDraft, sendDraft or discardDraft.
              subject : Subject of the created draft.
              message : Human-readable confirmation ("Draft created").
            """)
    public MailDraftResponse createDraft(ComposeMailToolRequest request) {
        CreateDraftRequest draftRequest = new CreateDraftRequest();
        draftRequest.setMode(parseComposeMode(request.getMode()));
        draftRequest.setOriginalEntryId(request.getOriginalEntryId());
        draftRequest.setSubject(request.getSubject());
        draftRequest.setTo(request.getTo());
        draftRequest.setCc(request.getCc());
        draftRequest.setBcc(request.getBcc());
        draftRequest.setBody(request.getBody());
        draftRequest.setHtmlBody(request.getHtmlBody());
        draftRequest.setAttachments(request.getAttachments());
        return outlookService.createDraft(draftRequest);
    }

    @Tool(description = """
            Attach a file to an existing Outlook Desktop draft.

            REQUEST:
              entryId    : (required) Outlook entryId of the target draft.
              attachment : (required) Object with:
                             fileName     : File name including extension (e.g. "invoice.pdf").
                             base64Content: File content encoded as a Base64 string.
                             mediaType    : (optional) MIME type (e.g. "application/pdf", "image/png").

            RESPONSE:
              entryId : entryId of the updated draft.
              subject : Subject of the draft.
              message : Human-readable confirmation ("Attachment added").
            """)
    public MailDraftResponse addAttachmentToDraft(DraftAttachmentToolRequest request) {
        return outlookService.addAttachmentToDraft(request.getEntryId(), request.getAttachment());
    }

    @Tool(description = """
            Update fields of an existing Outlook Desktop draft. Only the fields provided are changed; omitted fields are left untouched.
            Attachments in the request are appended — existing attachments on the draft are preserved.

            REQUEST:
              entryId  : (required) Outlook entryId of the draft to update.
              subject  : (optional) New subject.
              to       : (optional) New primary recipients. Separate multiple addresses with semicolons.
              cc       : (optional) New CC recipients.
              bcc      : (optional) New BCC recipients.
              body     : (optional) New plain-text body.
              htmlBody : (optional) New HTML body. Takes precedence over body if both are provided.
              attachments : (optional) Array of new attachments to append: \
            { fileName (required), base64Content (required), mediaType (optional) }.

            RESPONSE:
              entryId : entryId of the updated draft.
              subject : Subject of the draft after update.
              message : Human-readable confirmation ("Draft updated").
            """)
    public MailDraftResponse updateDraft(UpdateDraftToolRequest request) {
        UpdateDraftRequest draftRequest = new UpdateDraftRequest();
        draftRequest.setSubject(request.getSubject());
        draftRequest.setTo(request.getTo());
        draftRequest.setCc(request.getCc());
        draftRequest.setBcc(request.getBcc());
        draftRequest.setBody(request.getBody());
        draftRequest.setHtmlBody(request.getHtmlBody());
        draftRequest.setAttachments(request.getAttachments());
        return outlookService.updateDraft(request.getEntryId(), draftRequest);
    }

    @Tool(description = """
            List the attachments of an Outlook Desktop message. Use this before getAttachment to discover valid index values.

            REQUEST:
              entryId : (required) Outlook entryId of the message.

            RESPONSE (array of attachment metadata objects):
              index    : 1-based attachment index. Pass this value to getAttachment.
              fileName : File name including extension.
              size     : Attachment size in bytes.
            """)
    public List<AttachmentInfo> listAttachments(AttachmentListToolRequest request) {
        return outlookService.listAttachments(request.getEntryId());
    }

    @Tool(description = """
            Download an Outlook Desktop message attachment as a Base64-encoded string.
            Call listAttachments first to discover the valid index values for a given message.

            REQUEST:
              entryId         : (required) Outlook entryId of the message.
              attachmentIndex : (required) 1-based index of the attachment, as returned by listAttachments.

            RESPONSE:
              fileName     : File name including extension.
              mediaType    : MIME type (e.g. "application/pdf"). Falls back to "application/octet-stream" if unknown.
              base64Content: Full attachment content encoded as a Base64 string.
            """)
    public AttachmentContent getAttachment(AttachmentContentToolRequest request) {
        return outlookService.downloadAttachment(request.getEntryId(), request.getAttachmentIndex());
    }

    @Tool(description = """
            Compose and send an Outlook Desktop email immediately, without creating a draft first.

            REQUEST:
              mode            : (optional) Composition mode. Allowed values: NEW, REPLY, REPLY_ALL. Default: NEW.
              originalEntryId : (required when mode is REPLY or REPLY_ALL) entryId of the message to reply to.
              subject         : (required when mode is NEW) Message subject.
              to              : (required when mode is NEW) Primary recipients. Separate multiple addresses with semicolons.
              cc              : (optional) CC recipients.
              bcc             : (optional) BCC recipients.
              body            : (optional) Plain-text body. Use either body or htmlBody, not both.
              htmlBody        : (optional) HTML body. Takes precedence over body if both are provided.
              attachments     : (optional) Array of { fileName (required), base64Content (required), mediaType (optional) }.

            RESPONSE:
              status : "OK" on success.
              detail : Human-readable confirmation ("Email sent").
            """)
    public StatusResponse sendMail(ComposeMailToolRequest request) {
        SendMailRequest sendRequest = new SendMailRequest();
        sendRequest.setMode(parseComposeMode(request.getMode()));
        sendRequest.setOriginalEntryId(request.getOriginalEntryId());
        sendRequest.setSubject(request.getSubject());
        sendRequest.setTo(request.getTo());
        sendRequest.setCc(request.getCc());
        sendRequest.setBcc(request.getBcc());
        sendRequest.setBody(request.getBody());
        sendRequest.setHtmlBody(request.getHtmlBody());
        sendRequest.setAttachments(request.getAttachments());
        return outlookService.sendMail(sendRequest);
    }

    @Tool(description = """
            Send an existing Outlook Desktop draft. The draft is removed from Drafts after sending.

            REQUEST:
              entryId : (required) Outlook entryId of the draft to send.

            RESPONSE:
              status : "OK" on success.
              detail : Human-readable confirmation ("Draft sent").
            """)
    public StatusResponse sendDraft(DraftDispatchToolRequest request) {
        return outlookService.sendExistingDraft(request.getEntryId());
    }

    @Tool(description = """
            Discard an existing Outlook Desktop draft.

            REQUEST:
              entryId   : (required) Outlook entryId of the draft to discard.
              permanent : (optional) When true, permanently deletes the draft. \
            When false or omitted, moves it to Deleted Items. Default: false.

            RESPONSE:
              status : "OK" on success.
              detail : "Draft permanently deleted" or "Draft moved to Deleted Items".
            """)
    public StatusResponse discardDraft(DraftDiscardToolRequest request) {
        return outlookService.discardDraft(request.getEntryId(), Boolean.TRUE.equals(request.getPermanent()));
    }

    private FolderType parseFolder(String folder) {
        try {
            return FolderType.valueOf(folder.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new OutlookComException("Folder invalida. Usa: INBOX, DRAFTS, SENT, OUTBOX, DELETED", ex);
        }
    }

    private ComposeMode parseComposeMode(String mode) {
        if (mode == null || mode.isBlank()) {
            return ComposeMode.NEW;
        }
        try {
            return ComposeMode.valueOf(mode.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new OutlookComException("Compose mode invalido. Usa: NEW, REPLY, REPLY_ALL", ex);
        }
    }

    private OffsetDateTime parseSince(String since) {
        if (!StringUtils.hasText(since)) {
            return null;
        }
        String value = since.trim();
        try {
            return OffsetDateTime.parse(value);
        } catch (DateTimeParseException ignored) {
        }
        try {
            return OffsetDateTime.ofInstant(Instant.parse(value), ZoneId.systemDefault());
        } catch (DateTimeParseException ignored) {
        }
        try {
            return LocalDateTime.parse(value).atZone(ZoneId.systemDefault()).toOffsetDateTime();
        } catch (DateTimeParseException ex) {
            throw new OutlookComException("since debe ser una fecha ISO-8601 valida", ex);
        }
    }

    private String normalizeSortOrder(String sortOrder) {
        if (!StringUtils.hasText(sortOrder)) {
            return "desc";
        }
        String normalized = sortOrder.trim().toLowerCase(Locale.ROOT);
        if (!normalized.equals("desc") && !normalized.equals("asc")) {
            throw new OutlookComException("sortOrder invalido. Usa: desc, asc");
        }
        return normalized;
    }
}



