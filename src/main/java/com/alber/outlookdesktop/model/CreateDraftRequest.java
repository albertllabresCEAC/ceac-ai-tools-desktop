package com.alber.outlookdesktop.model;

import jakarta.validation.Valid;

import java.util.ArrayList;
import java.util.List;

public class CreateDraftRequest {

    private ComposeMode mode = ComposeMode.NEW;

    private String originalEntryId;

    private String subject;

    private String to;
    private String cc;
    private String bcc;
    private String body;
    private String htmlBody;
    @Valid
    private List<AttachmentPayload> attachments = new ArrayList<>();

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public ComposeMode getMode() {
        return mode;
    }

    public void setMode(ComposeMode mode) {
        this.mode = mode;
    }

    public String getOriginalEntryId() {
        return originalEntryId;
    }

    public void setOriginalEntryId(String originalEntryId) {
        this.originalEntryId = originalEntryId;
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
