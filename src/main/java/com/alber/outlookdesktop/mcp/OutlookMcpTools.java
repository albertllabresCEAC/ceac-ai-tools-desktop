package com.alber.outlookdesktop.mcp;

import com.alber.outlookdesktop.model.AttachmentContent;
import com.alber.outlookdesktop.model.AttachmentInfo;
import com.alber.outlookdesktop.model.AttachmentPayload;
import com.alber.outlookdesktop.model.ComposeMode;
import com.alber.outlookdesktop.model.CreateDraftRequest;
import com.alber.outlookdesktop.model.FolderType;
import com.alber.outlookdesktop.model.MailDraftResponse;
import com.alber.outlookdesktop.model.MailMessage;
import com.alber.outlookdesktop.model.MessageQuery;
import com.alber.outlookdesktop.model.SendMailRequest;
import com.alber.outlookdesktop.model.StatusResponse;
import com.alber.outlookdesktop.model.UpdateDraftRequest;
import com.alber.outlookdesktop.service.OutlookComException;
import com.alber.outlookdesktop.service.OutlookService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OutlookMcpTools {

    private final OutlookService outlookService;

    public OutlookMcpTools(OutlookService outlookService) {
        this.outlookService = outlookService;
    }

    @Tool(description = "List Outlook Desktop messages from a folder. Valid folders: INBOX, DRAFTS, SENT, OUTBOX, DELETED.")
    public List<MailMessage> listMessages(String folder, Integer limit, Boolean unreadOnly) {
        MessageQuery query = new MessageQuery();
        if (folder != null) {
            query.setFolder(parseFolder(folder));
        }
        query.setLimit(limit);
        query.setUnreadOnly(Boolean.TRUE.equals(unreadOnly));
        return outlookService.listMessages(query);
    }

    @Tool(description = "Get a full Outlook Desktop message by its entryId.")
    public MailMessage getMessage(String entryId) {
        return outlookService.getMessage(entryId);
    }

    @Tool(description = "Create an Outlook Desktop draft email. mode supports NEW, REPLY and REPLY_ALL. originalEntryId is required for reply modes.")
    public MailDraftResponse createDraft(String mode,
                                         String originalEntryId,
                                         String subject,
                                         String to,
                                         String cc,
                                         String bcc,
                                         String body,
                                         String htmlBody,
                                         List<AttachmentPayload> attachments) {
        CreateDraftRequest request = new CreateDraftRequest();
        request.setMode(parseComposeMode(mode));
        request.setOriginalEntryId(originalEntryId);
        request.setSubject(subject);
        request.setTo(to);
        request.setCc(cc);
        request.setBcc(bcc);
        request.setBody(body);
        request.setHtmlBody(htmlBody);
        request.setAttachments(attachments);
        return outlookService.createDraft(request);
    }

    @Tool(description = "Attach a Base64 file payload to an existing Outlook draft.")
    public MailDraftResponse addAttachmentToDraft(String entryId, AttachmentPayload attachment) {
        return outlookService.addAttachmentToDraft(entryId, attachment);
    }

    @Tool(description = "Update subject, recipients, body and optionally append attachments to an existing Outlook draft.")
    public MailDraftResponse updateDraft(String entryId,
                                         String subject,
                                         String to,
                                         String cc,
                                         String bcc,
                                         String body,
                                         String htmlBody,
                                         List<AttachmentPayload> attachments) {
        UpdateDraftRequest request = new UpdateDraftRequest();
        request.setSubject(subject);
        request.setTo(to);
        request.setCc(cc);
        request.setBcc(bcc);
        request.setBody(body);
        request.setHtmlBody(htmlBody);
        request.setAttachments(attachments);
        return outlookService.updateDraft(entryId, request);
    }

    @Tool(description = "List attachments available in an Outlook email by entryId.")
    public List<AttachmentInfo> listAttachments(String entryId) {
        return outlookService.listAttachments(entryId);
    }

    @Tool(description = "Get an Outlook email attachment content encoded as Base64.")
    public AttachmentContent getAttachment(String entryId, Integer attachmentIndex) {
        return outlookService.downloadAttachment(entryId, attachmentIndex);
    }

    @Tool(description = "Send an Outlook email. mode supports NEW, REPLY and REPLY_ALL. originalEntryId is required for reply modes.")
    public StatusResponse sendMail(String mode,
                                   String originalEntryId,
                                   String subject,
                                   String to,
                                   String cc,
                                   String bcc,
                                   String body,
                                   String htmlBody,
                                   List<AttachmentPayload> attachments) {
        SendMailRequest request = new SendMailRequest();
        request.setMode(parseComposeMode(mode));
        request.setOriginalEntryId(originalEntryId);
        request.setSubject(subject);
        request.setTo(to);
        request.setCc(cc);
        request.setBcc(bcc);
        request.setBody(body);
        request.setHtmlBody(htmlBody);
        request.setAttachments(attachments);
        return outlookService.sendMail(request);
    }

    @Tool(description = "Send an existing Outlook draft by its entryId.")
    public StatusResponse sendDraft(String entryId) {
        return outlookService.sendExistingDraft(entryId);
    }

    @Tool(description = "Discard an existing Outlook draft. By default it moves to Deleted Items. Set permanent=true to delete permanently.")
    public StatusResponse discardDraft(String entryId, Boolean permanent) {
        return outlookService.discardDraft(entryId, Boolean.TRUE.equals(permanent));
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
}
