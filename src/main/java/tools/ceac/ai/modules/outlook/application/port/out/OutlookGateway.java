package tools.ceac.ai.modules.outlook.application.port.out;

import java.util.List;
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
 * Outbound port for Outlook Desktop operations.
 *
 * <p>The application layer depends on this interface while the COM-specific implementation lives in
 * the infrastructure layer.
 */
public interface OutlookGateway {

    List<MailMessage> listMessages(MessageQuery query);

    List<MessageSearchResult> searchMessages(MessageSearchRequest request);

    MailMessage getMessage(String entryId);

    List<AttachmentInfo> listAttachments(String entryId);

    AttachmentContent downloadAttachment(String entryId, int attachmentIndex);

    MailDraftResponse createDraft(CreateDraftRequest request);

    MailDraftResponse addAttachmentToDraft(String entryId, AttachmentPayload attachment);

    MailDraftResponse updateDraft(String entryId, UpdateDraftRequest request);

    StatusResponse sendMail(SendMailRequest request);

    StatusResponse sendExistingDraft(String entryId);

    StatusResponse discardDraft(String entryId, boolean permanent);
}

