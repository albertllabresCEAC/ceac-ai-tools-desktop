package tools.ceac.ai.modules.outlook.application.service;

import java.util.List;
import org.springframework.stereotype.Service;
import tools.ceac.ai.modules.outlook.application.port.out.OutlookGateway;
import tools.ceac.ai.modules.outlook.domain.model.AttachmentContent;
import tools.ceac.ai.modules.outlook.domain.model.AttachmentInfo;
import tools.ceac.ai.modules.outlook.domain.model.AttachmentPayload;
import tools.ceac.ai.modules.outlook.domain.model.CreateDraftRequest;
import tools.ceac.ai.modules.outlook.domain.model.MailDraftResponse;
import tools.ceac.ai.modules.outlook.domain.model.MailMessage;
import tools.ceac.ai.modules.outlook.domain.model.MessageQuery;
import tools.ceac.ai.modules.outlook.domain.model.MessageSearchRequest;
import tools.ceac.ai.modules.outlook.domain.model.MessageSearchResult;
import tools.ceac.ai.modules.outlook.domain.model.SendMailRequest;
import tools.ceac.ai.modules.outlook.domain.model.StatusResponse;
import tools.ceac.ai.modules.outlook.domain.model.UpdateDraftRequest;

/**
 * Application service for Outlook MCP use cases.
 *
 * <p>This class keeps REST controllers and MCP tools decoupled from the COM-specific gateway.
 */
@Service
public class OutlookMailService {

    private final OutlookGateway gateway;

    public OutlookMailService(OutlookGateway gateway) {
        this.gateway = gateway;
    }

    public List<MailMessage> listMessages(MessageQuery query) {
        return gateway.listMessages(query);
    }

    public List<MessageSearchResult> searchMessages(MessageSearchRequest request) {
        return gateway.searchMessages(request);
    }

    public MailMessage getMessage(String entryId) {
        return gateway.getMessage(entryId);
    }

    public List<AttachmentInfo> listAttachments(String entryId) {
        return gateway.listAttachments(entryId);
    }

    public AttachmentContent downloadAttachment(String entryId, int attachmentIndex) {
        return gateway.downloadAttachment(entryId, attachmentIndex);
    }

    public MailDraftResponse createDraft(CreateDraftRequest request) {
        return gateway.createDraft(request);
    }

    public MailDraftResponse addAttachmentToDraft(String entryId, AttachmentPayload attachment) {
        return gateway.addAttachmentToDraft(entryId, attachment);
    }

    public MailDraftResponse updateDraft(String entryId, UpdateDraftRequest request) {
        return gateway.updateDraft(entryId, request);
    }

    public StatusResponse sendMail(SendMailRequest request) {
        return gateway.sendMail(request);
    }

    public StatusResponse sendExistingDraft(String entryId) {
        return gateway.sendExistingDraft(entryId);
    }

    public StatusResponse discardDraft(String entryId, boolean permanent) {
        return gateway.discardDraft(entryId, permanent);
    }
}

