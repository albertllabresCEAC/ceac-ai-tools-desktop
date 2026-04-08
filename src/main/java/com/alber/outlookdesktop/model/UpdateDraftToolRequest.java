package com.alber.outlookdesktop.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;
import org.springframework.ai.tool.annotation.ToolParam;

public class UpdateDraftToolRequest {

    @NotBlank
    @ToolParam(description = "Outlook entryId of the draft to update.")
    private String entryId;

    @ToolParam(required = false, description = "New subject for the draft.")
    private String subject;

    @ToolParam(required = false, description = "New TO recipients as a single string.")
    private String to;

    @ToolParam(required = false, description = "New CC recipients as a single string.")
    private String cc;

    @ToolParam(required = false, description = "New BCC recipients as a single string.")
    private String bcc;

    @ToolParam(required = false, description = "New plain-text body.")
    private String body;

    @ToolParam(required = false, description = "New HTML body.")
    private String htmlBody;

    @Valid
    @ToolParam(required = false, description = "Optional list of attachments to append to the draft. Existing attachments are preserved.")
    private List<AttachmentPayload> attachments = new ArrayList<>();

    public String getEntryId() {
        return entryId;
    }

    public void setEntryId(String entryId) {
        this.entryId = entryId;
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
