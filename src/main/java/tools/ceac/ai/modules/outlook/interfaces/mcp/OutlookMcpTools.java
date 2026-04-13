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
import tools.ceac.ai.modules.outlook.domain.model.McpSearchResult;
import tools.ceac.ai.modules.outlook.domain.model.MessageGetToolRequest;
import tools.ceac.ai.modules.outlook.domain.model.MessageListToolRequest;
import tools.ceac.ai.modules.outlook.domain.model.MessageQuery;
import tools.ceac.ai.modules.outlook.domain.model.MessageSearchToolRequest;
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
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class OutlookMcpTools {

    private final OutlookMailService outlookService;

    public OutlookMcpTools(OutlookMailService outlookService) {
        this.outlookService = outlookService;
    }

    @Tool(description = "List Outlook Desktop messages ordered by received date descending by default. Call this tool with a JSON object, not a raw string. Input fields: folder, limit, unreadOnly, since, sortOrder. Allowed folder values: INBOX, DRAFTS, SENT, OUTBOX, DELETED. sortOrder accepts desc or asc. If since is omitted, only the last 90 days are listed. If folder is omitted, INBOX is used.")
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

    @Tool(description = "Get a full Outlook Desktop message. Call this tool with a JSON object containing entryId.")
    public MailMessage getMessage(MessageGetToolRequest request) {
        return outlookService.getMessage(request.getEntryId());
    }

    @Tool(description = "Search Outlook Desktop messages by free text. Call this tool with a JSON object containing query, folder, limit and unreadOnly. If folder is omitted or set to ALL, the search runs across INBOX, SENT and DRAFTS. Use fetch with the returned entryId to retrieve the full message.")
    public List<McpSearchResult> search(MessageSearchToolRequest request) {
        int effectiveLimit = sanitizeLimit(request != null ? request.getLimit() : null);
        List<FolderType> folders = resolveSearchFolders(request != null ? request.getFolder() : null);
        Map<String, McpSearchResult> matches = new LinkedHashMap<>();

        for (FolderType folderType : folders) {
            MessageQuery messageQuery = new MessageQuery();
            messageQuery.setFolder(folderType);
            messageQuery.setUnreadOnly(request != null && Boolean.TRUE.equals(request.getUnreadOnly()));
            messageQuery.setLimit(Math.max(effectiveLimit * 4, 25));

            for (MailMessage message : outlookService.listMessages(messageQuery)) {
                if (!matchesQuery(message, request != null ? request.getQuery() : null)) {
                    continue;
                }
                matches.putIfAbsent(message.entryId(), toSearchResult(message, folderType));
            }
        }

        return matches.values().stream()
                .sorted(Comparator.comparing(this::sortTimestamp, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(effectiveLimit)
                .collect(Collectors.toList());
    }

    @Tool(description = "Fetch a full Outlook Desktop message by entryId. Call this tool with a JSON object containing entryId. Use this after search to read the complete content.")
    public MailMessage fetch(MessageGetToolRequest request) {
        return getMessage(request);
    }

    @Tool(description = "Create an Outlook Desktop draft email. Call this tool with a JSON object. Allowed mode values: NEW, REPLY, REPLY_ALL. originalEntryId is required when mode is REPLY or REPLY_ALL. Attachments must be sent as an array of objects with fileName, base64Content and optional mediaType.")
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

    @Tool(description = "Attach a Base64 file payload to an existing Outlook draft. Call this tool with a JSON object containing entryId and attachment. The attachment object must include fileName and base64Content, and may include mediaType.")
    public MailDraftResponse addAttachmentToDraft(DraftAttachmentToolRequest request) {
        return outlookService.addAttachmentToDraft(request.getEntryId(), request.getAttachment());
    }

    @Tool(description = "Update an existing Outlook draft. Call this tool with a JSON object containing entryId plus any fields to change. Attachments are appended; existing attachments are kept.")
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

    @Tool(description = "List attachments available in an Outlook email. Call this tool with a JSON object containing entryId.")
    public List<AttachmentInfo> listAttachments(AttachmentListToolRequest request) {
        return outlookService.listAttachments(request.getEntryId());
    }

    @Tool(description = "Get an Outlook email attachment encoded as Base64. Call this tool with a JSON object containing entryId and attachmentIndex. attachmentIndex is zero-based.")
    public AttachmentContent getAttachment(AttachmentContentToolRequest request) {
        return outlookService.downloadAttachment(request.getEntryId(), request.getAttachmentIndex());
    }

    @Tool(description = "Send an Outlook email immediately. Call this tool with a JSON object. Allowed mode values: NEW, REPLY, REPLY_ALL. originalEntryId is required when mode is REPLY or REPLY_ALL. Attachments must be sent as an array of objects with fileName, base64Content and optional mediaType.")
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

    @Tool(description = "Send an existing Outlook draft. Call this tool with a JSON object containing entryId.")
    public StatusResponse sendDraft(DraftDispatchToolRequest request) {
        return outlookService.sendExistingDraft(request.getEntryId());
    }

    @Tool(description = "Discard an existing Outlook draft. Call this tool with a JSON object containing entryId and optional permanent. If permanent is true, the draft is deleted permanently. Otherwise it is moved to Deleted Items.")
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

    private List<FolderType> resolveSearchFolders(String folder) {
        if (!StringUtils.hasText(folder) || "ALL".equalsIgnoreCase(folder.trim())) {
            return List.of(FolderType.INBOX, FolderType.SENT, FolderType.DRAFTS);
        }
        return List.of(parseFolder(folder));
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

    private int sanitizeLimit(Integer limit) {
        if (limit == null) {
            return 10;
        }
        return Math.max(1, Math.min(limit, 25));
    }

    private boolean matchesQuery(MailMessage message, String query) {
        if (!StringUtils.hasText(query)) {
            return true;
        }
        String normalizedQuery = query.toLowerCase(Locale.ROOT);
        return contains(message.subject(), normalizedQuery)
                || contains(message.senderName(), normalizedQuery)
                || contains(message.senderEmail(), normalizedQuery)
                || contains(message.to(), normalizedQuery)
                || contains(message.cc(), normalizedQuery)
                || contains(message.bcc(), normalizedQuery)
                || contains(message.body(), normalizedQuery);
    }

    private boolean contains(String value, String query) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(query);
    }

    private McpSearchResult toSearchResult(MailMessage message, FolderType folderType) {
        return new McpSearchResult(
                message.entryId(),
                folderType.name(),
                message.subject(),
                message.senderName(),
                message.senderEmail(),
                buildSnippet(message),
                message.unread(),
                message.receivedAt(),
                message.sentAt()
        );
    }

    private String buildSnippet(MailMessage message) {
        String source = StringUtils.hasText(message.body()) ? message.body() : message.htmlBody();
        if (!StringUtils.hasText(source)) {
            return null;
        }
        String normalized = source.replaceAll("\\s+", " ").trim();
        if (normalized.length() <= 240) {
            return normalized;
        }
        return normalized.substring(0, 237) + "...";
    }

    private OffsetDateTime sortTimestamp(McpSearchResult result) {
        return result.receivedAt() != null ? result.receivedAt() : result.sentAt();
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



