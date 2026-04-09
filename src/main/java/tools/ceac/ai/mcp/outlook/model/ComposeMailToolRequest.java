package tools.ceac.ai.mcp.outlook.model;

import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import org.springframework.ai.tool.annotation.ToolParam;

public class ComposeMailToolRequest {

    @ToolParam(required = false, description = "Composition mode. Allowed values: NEW, REPLY, REPLY_ALL. Defaults to NEW.")
    private String mode;

    @ToolParam(required = false, description = "Required only when mode is REPLY or REPLY_ALL. Must be the Outlook entryId of the original message.")
    private String originalEntryId;

    @ToolParam(required = false, description = "Message subject. Usually used with mode NEW.")
    private String subject;

    @ToolParam(required = false, description = "Primary recipients as a single string. Use Outlook-style separators such as semicolons between addresses if needed.")
    private String to;

    @ToolParam(required = false, description = "CC recipients as a single string.")
    private String cc;

    @ToolParam(required = false, description = "BCC recipients as a single string.")
    private String bcc;

    @ToolParam(required = false, description = "Plain-text body. Prefer this when HTML formatting is not required.")
    private String body;

    @ToolParam(required = false, description = "HTML body. Use either body or htmlBody. If both are provided, the service will pass both values through to Outlook.")
    private String htmlBody;

    @Valid
    @ToolParam(required = false, description = "Optional list of attachments. Each attachment object must include fileName and base64Content, and may include mediaType.")
    private List<AttachmentPayload> attachments = new ArrayList<>();

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getOriginalEntryId() {
        return originalEntryId;
    }

    public void setOriginalEntryId(String originalEntryId) {
        this.originalEntryId = originalEntryId;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getCc() {
        return cc;
    }

    public void setCc(String cc) {
        this.cc = cc;
    }

    public String getBcc() {
        return bcc;
    }

    public void setBcc(String bcc) {
        this.bcc = bcc;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getHtmlBody() {
        return htmlBody;
    }

    public void setHtmlBody(String htmlBody) {
        this.htmlBody = htmlBody;
    }

    public List<AttachmentPayload> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<AttachmentPayload> attachments) {
        this.attachments = attachments;
    }
}
