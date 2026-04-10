package tools.ceac.ai.modules.outlook.domain.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public class AddAttachmentRequest {

    @Valid
    @NotNull
    private AttachmentPayload attachment;

    public AttachmentPayload getAttachment() {
        return attachment;
    }

    public void setAttachment(AttachmentPayload attachment) {
        this.attachment = attachment;
    }
}


