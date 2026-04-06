package com.alber.outlookdesktop.model;

public record AttachmentContent(
        String fileName,
        String mediaType,
        String base64Content
) {
}
