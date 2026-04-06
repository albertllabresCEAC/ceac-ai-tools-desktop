package com.alber.outlookdesktop.model;

import jakarta.validation.constraints.NotBlank;

public class AttachmentPayload {

    @NotBlank
    private String fileName;

    private String mediaType;

    @NotBlank
    private String base64Content;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public String getBase64Content() {
        return base64Content;
    }

    public void setBase64Content(String base64Content) {
        this.base64Content = base64Content;
    }
}
