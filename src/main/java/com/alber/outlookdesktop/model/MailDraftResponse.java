package com.alber.outlookdesktop.model;

public record MailDraftResponse(
        String entryId,
        String subject,
        String message
) {
}
