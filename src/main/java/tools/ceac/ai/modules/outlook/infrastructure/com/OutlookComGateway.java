package tools.ceac.ai.modules.outlook.infrastructure.com;

import tools.ceac.ai.modules.outlook.application.port.out.OutlookGateway;
import tools.ceac.ai.modules.outlook.config.OutlookProperties;
import tools.ceac.ai.modules.outlook.domain.exception.OutlookComException;
import tools.ceac.ai.modules.outlook.domain.model.AttachmentContent;
import tools.ceac.ai.modules.outlook.domain.model.AttachmentInfo;
import tools.ceac.ai.modules.outlook.domain.model.AttachmentPayload;
import tools.ceac.ai.modules.outlook.domain.model.ComposeMode;
import tools.ceac.ai.modules.outlook.domain.model.CreateDraftRequest;
import tools.ceac.ai.modules.outlook.domain.model.FolderType;
import tools.ceac.ai.modules.outlook.domain.model.MailDraftResponse;
import tools.ceac.ai.modules.outlook.domain.model.MailMessage;
import tools.ceac.ai.modules.outlook.domain.model.MessageQuery;
import tools.ceac.ai.modules.outlook.domain.model.SendMailRequest;
import tools.ceac.ai.modules.outlook.domain.model.StatusResponse;
import tools.ceac.ai.modules.outlook.domain.model.UpdateDraftRequest;
import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.ComFailException;
import com.jacob.com.Dispatch;
import com.jacob.com.Variant;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * COM-backed implementation of the Outlook gateway.
 *
 * <p>This class contains the Outlook/JACOB specifics. The rest of the module talks to it through
 * the {@code OutlookGateway} port.
 */
@Service
public class OutlookComGateway implements OutlookGateway {

    private static final int OL_MAIL_ITEM = 0;
    private static final List<DateTimeFormatter> OUTLOOK_DATE_FORMATS = List.of(
            DateTimeFormatter.ofPattern("M/d/yyyy h:mm:ss a", Locale.US),
            DateTimeFormatter.ofPattern("M/d/yyyy h:mm a", Locale.US),
            DateTimeFormatter.ofPattern("d/M/yyyy H:mm:ss", Locale.US),
            DateTimeFormatter.ofPattern("d/M/yyyy H:mm", Locale.US),
            DateTimeFormatter.ofPattern("d/M/yyyy HH:mm:ss", Locale.US),
            DateTimeFormatter.ofPattern("d/M/yyyy HH:mm", Locale.US),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss", Locale.US),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.US)
    );

    private final OutlookProperties properties;
    private final JacobLibraryService jacobLibraryService;

    public OutlookComGateway(OutlookProperties properties, JacobLibraryService jacobLibraryService) {
        this.properties = properties;
        this.jacobLibraryService = jacobLibraryService;
    }

    public List<MailMessage> listMessages(MessageQuery query) {
        int limit = query.getLimit() != null ? query.getLimit() : properties.getDefaultPageSize();
        return withNamespace(namespace -> {
            Dispatch folder = getDefaultFolder(namespace, query.getFolder());
            Dispatch items = Dispatch.get(folder, "Items").toDispatch();
            List<MailMessage> result = new ArrayList<>();
            int count = Dispatch.get(items, "Count").getInt();
            for (int i = count; i >= 1 && result.size() < limit; i--) {
                Dispatch item = Dispatch.call(items, "Item", new Variant(i)).toDispatch();
                if (!"IPM.Note".equalsIgnoreCase(safeString(item, "MessageClass"))) {
                    continue;
                }
                if (query.isUnreadOnly() && !Dispatch.get(item, "UnRead").getBoolean()) {
                    continue;
                }
                result.add(toMailMessage(item));
            }
            result.sort(Comparator.comparing(MailMessage::receivedAt,
                    Comparator.nullsLast(Comparator.reverseOrder())));
            return result;
        });
    }

    public MailMessage getMessage(String entryId) {
        return withNamespace(namespace -> toMailMessage(getItemFromId(namespace, entryId)));
    }

    public List<AttachmentInfo> listAttachments(String entryId) {
        return withNamespace(namespace -> readAttachments(getItemFromId(namespace, entryId)));
    }

    public AttachmentContent downloadAttachment(String entryId, int attachmentIndex) {
        return withNamespace(namespace -> {
            Dispatch item = getItemFromId(namespace, entryId);
            Dispatch attachment = getAttachment(item, attachmentIndex);
            String fileName = safeString(attachment, "FileName");
            Path tempFile = createTempAttachmentPath(fileName);
            Dispatch.call(attachment, "SaveAsFile", tempFile.toString());
            try {
                byte[] content = Files.readAllBytes(tempFile);
                String mediaType = probeMediaType(tempFile);
                return new AttachmentContent(fileName, mediaType, Base64.getEncoder().encodeToString(content));
            } catch (IOException ex) {
                throw new OutlookComException("No se pudo leer el adjunto descargado", ex);
            } finally {
                deleteQuietly(tempFile);
            }
        });
    }

    public MailDraftResponse createDraft(CreateDraftRequest request) {
        validateComposeRequest(request.getMode(), request.getOriginalEntryId(), request.getTo(), request.getSubject());
        return withApplication(application -> {
            Dispatch item = createComposedMailItem(application, request.getMode(), request.getOriginalEntryId());
            applyMailFields(item, request.getSubject(), request.getTo(), request.getCc(), request.getBcc(),
                    request.getBody(), request.getHtmlBody(), request.getAttachments());
            Dispatch.call(item, "Save");
            return new MailDraftResponse(safeString(item, "EntryID"), safeString(item, "Subject"), "Draft created");
        });
    }

    public MailDraftResponse addAttachmentToDraft(String entryId, AttachmentPayload attachment) {
        return withNamespace(namespace -> {
            Dispatch item = getItemFromId(namespace, entryId);
            ensureMailItem(item);
            addAttachment(item, attachment);
            Dispatch.call(item, "Save");
            return new MailDraftResponse(entryId, safeString(item, "Subject"), "Attachment added");
        });
    }

    public MailDraftResponse updateDraft(String entryId, UpdateDraftRequest request) {
        return withNamespace(namespace -> {
            Dispatch item = getItemFromId(namespace, entryId);
            ensureMailItem(item);
            applyMailFields(item, request.getSubject(), request.getTo(), request.getCc(), request.getBcc(),
                    request.getBody(), request.getHtmlBody(), request.getAttachments());
            Dispatch.call(item, "Save");
            return new MailDraftResponse(entryId, safeString(item, "Subject"), "Draft updated");
        });
    }

    public StatusResponse sendMail(SendMailRequest request) {
        validateComposeRequest(request.getMode(), request.getOriginalEntryId(), request.getTo(), request.getSubject());
        return withApplication(application -> {
            Dispatch item = createComposedMailItem(application, request.getMode(), request.getOriginalEntryId());
            applyMailFields(item, request.getSubject(), request.getTo(), request.getCc(), request.getBcc(),
                    request.getBody(), request.getHtmlBody(), request.getAttachments());
            Dispatch.call(item, "Send");
            return new StatusResponse("OK", "Email sent");
        });
    }

    public StatusResponse sendExistingDraft(String entryId) {
        return withNamespace(namespace -> {
            Dispatch item = getItemFromId(namespace, entryId);
            ensureMailItem(item);
            Dispatch.call(item, "Send");
            return new StatusResponse("OK", "Draft sent");
        });
    }

    public StatusResponse discardDraft(String entryId, boolean permanent) {
        return withNamespace(namespace -> {
            Dispatch item = getItemFromId(namespace, entryId);
            ensureMailItem(item);
            if (permanent) {
                Dispatch.call(item, "Delete");
                return new StatusResponse("OK", "Draft permanently deleted");
            }
            Dispatch deletedFolder = getDefaultFolder(namespace, FolderType.DELETED);
            Dispatch.call(item, "Move", deletedFolder);
            return new StatusResponse("OK", "Draft moved to Deleted Items");
        });
    }

    private void applyMailFields(Dispatch item,
                                 String subject,
                                 String to,
                                 String cc,
                                 String bcc,
                                 String body,
                                 String htmlBody,
                                 List<AttachmentPayload> attachments) {
        setIfPresent(item, "Subject", subject);
        setIfPresent(item, "To", to);
        setIfPresent(item, "CC", cc);
        setIfPresent(item, "BCC", bcc);
        if (StringUtils.hasText(htmlBody)) {
            Dispatch.put(item, "HTMLBody", htmlBody);
        } else if (body != null) {
            Dispatch.put(item, "Body", body);
        }
        if (attachments != null) {
            for (AttachmentPayload attachment : attachments) {
                addAttachment(item, attachment);
            }
        }
    }

    private Dispatch createComposedMailItem(ActiveXComponent application, ComposeMode mode, String originalEntryId) {
        ComposeMode effectiveMode = mode != null ? mode : ComposeMode.NEW;
        return switch (effectiveMode) {
            case NEW -> application.invoke("CreateItem", new Variant(OL_MAIL_ITEM)).toDispatch();
            case REPLY, REPLY_ALL -> {
                Dispatch namespace = application.invoke("GetNamespace", "MAPI").toDispatch();
                Dispatch originalItem = getItemFromId(namespace, originalEntryId);
                ensureMailItem(originalItem);
                yield Dispatch.call(originalItem, effectiveMode == ComposeMode.REPLY ? "Reply" : "ReplyAll").toDispatch();
            }
        };
    }

    private void validateComposeRequest(ComposeMode mode, String originalEntryId, String to, String subject) {
        ComposeMode effectiveMode = mode != null ? mode : ComposeMode.NEW;
        if (effectiveMode == ComposeMode.NEW) {
            if (!StringUtils.hasText(to)) {
                throw new OutlookComException("to is required when mode=NEW");
            }
            if (!StringUtils.hasText(subject)) {
                throw new OutlookComException("subject is required when mode=NEW");
            }
            return;
        }
        if (!StringUtils.hasText(originalEntryId)) {
            throw new OutlookComException("originalEntryId is required when mode=" + effectiveMode);
        }
    }

    private void addAttachment(Dispatch item, AttachmentPayload attachment) {
        if (attachment == null || !StringUtils.hasText(attachment.getBase64Content())
                || !StringUtils.hasText(attachment.getFileName())) {
            return;
        }
        Path tempFile = createTempAttachmentPath(attachment.getFileName());
        try {
            byte[] bytes = Base64.getDecoder().decode(attachment.getBase64Content());
            Files.write(tempFile, bytes, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
            Dispatch attachments = Dispatch.get(item, "Attachments").toDispatch();
            Dispatch.call(attachments, "Add", tempFile.toAbsolutePath().toString());
        } catch (IllegalArgumentException ex) {
            throw new OutlookComException("Adjunto en Base64 invalido: " + attachment.getFileName(), ex);
        } catch (IOException ex) {
            throw new OutlookComException("No se pudo materializar temporalmente el adjunto: " + attachment.getFileName(), ex);
        } finally {
            deleteQuietly(tempFile);
        }
    }

    private Dispatch getDefaultFolder(Dispatch namespace, FolderType folderType) {
        return Dispatch.call(namespace, "GetDefaultFolder", new Variant(folderType.getComValue())).toDispatch();
    }

    private Dispatch getItemFromId(Dispatch namespace, String entryId) {
        if (!StringUtils.hasText(entryId)) {
            throw new OutlookComException("entryId is required");
        }
        try {
            return Dispatch.call(namespace, "GetItemFromID", entryId).toDispatch();
        } catch (ComFailException ex) {
            throw new OutlookComException("No se pudo recuperar el elemento de Outlook con entryId=" + entryId, ex);
        }
    }

    private Dispatch getAttachment(Dispatch item, int attachmentIndex) {
        Dispatch attachments = Dispatch.get(item, "Attachments").toDispatch();
        int count = Dispatch.get(attachments, "Count").getInt();
        if (attachmentIndex < 1 || attachmentIndex > count) {
            throw new OutlookComException("attachmentIndex fuera de rango: " + attachmentIndex);
        }
        return Dispatch.call(attachments, "Item", new Variant(attachmentIndex)).toDispatch();
    }

    private List<AttachmentInfo> readAttachments(Dispatch item) {
        Dispatch attachments = Dispatch.get(item, "Attachments").toDispatch();
        int count = Dispatch.get(attachments, "Count").getInt();
        List<AttachmentInfo> result = new ArrayList<>(count);
        for (int index = 1; index <= count; index++) {
            Dispatch attachment = Dispatch.call(attachments, "Item", new Variant(index)).toDispatch();
            result.add(new AttachmentInfo(
                    index,
                    safeString(attachment, "FileName"),
                    safeLong(attachment, "Size")
            ));
        }
        return result;
    }

    private MailMessage toMailMessage(Dispatch item) {
        ensureMailItem(item);
        return new MailMessage(
                safeString(item, "EntryID"),
                safeString(item, "Subject"),
                safeString(item, "SenderName"),
                readSenderEmail(item),
                readRecipients(item, 1),
                readRecipients(item, 2),
                readRecipients(item, 3),
                safeString(item, "Body"),
                safeString(item, "HTMLBody"),
                safeBoolean(item, "UnRead"),
                safeDate(item, "ReceivedTime"),
                safeDate(item, "SentOn"),
                readAttachments(item)
        );
    }

    private String readSenderEmail(Dispatch item) {
        try {
            String emailType = safeString(item, "SenderEmailType");
            String senderEmailAddress = safeString(item, "SenderEmailAddress");
            if (!StringUtils.hasText(emailType) || "SMTP".equalsIgnoreCase(emailType)) {
                return senderEmailAddress;
            }
            if ("EX".equalsIgnoreCase(emailType)) {
                String exchangeAddress = readExchangePrimarySmtp(item);
                if (StringUtils.hasText(exchangeAddress)) {
                    return exchangeAddress;
                }
            }
            return senderEmailAddress;
        } catch (OutlookComException ex) {
            return null;
        }
    }

    private void ensureMailItem(Dispatch item) {
        String messageClass = safeString(item, "MessageClass");
        if (StringUtils.hasText(messageClass) && !Objects.equals(messageClass, "IPM.Note")) {
            throw new OutlookComException("El elemento no es un correo compatible: " + messageClass);
        }
    }

    private String readRecipients(Dispatch item, int recipientType) {
        try {
            Dispatch recipients = Dispatch.get(item, "Recipients").toDispatch();
            int count = Dispatch.get(recipients, "Count").getInt();
            List<String> emails = new ArrayList<>();
            for (int index = 1; index <= count; index++) {
                Dispatch recipient = Dispatch.call(recipients, "Item", new Variant(index)).toDispatch();
                int type = safeInt(recipient, "Type");
                if (type != recipientType) {
                    continue;
                }
                String email = readRecipientEmail(recipient);
                if (StringUtils.hasText(email)) {
                    emails.add(email);
                }
            }
            return String.join(";", emails);
        } catch (Exception ex) {
            return null;
        }
    }

    private String readExchangePrimarySmtp(Dispatch item) {
        String smtp = null;
        try {
            Dispatch sender = Dispatch.get(item, "Sender").toDispatch();
            smtp = readPrimarySmtpFromAddressEntry(sender);
        } catch (Exception ignored) {
        }
        if (StringUtils.hasText(smtp)) {
            return smtp;
        }
        try {
            Dispatch addressEntry = Dispatch.get(item, "Sender").toDispatch();
            smtp = readPrimarySmtpFromAddressEntry(addressEntry);
        } catch (Exception ignored) {
        }
        return smtp;
    }

    private String readPrimarySmtpFromAddressEntry(Dispatch addressEntry) {
        if (addressEntry == null) {
            return null;
        }
        try {
            Dispatch exchangeUser = Dispatch.call(addressEntry, "GetExchangeUser").toDispatch();
            String primarySmtpAddress = safeString(exchangeUser, "PrimarySmtpAddress");
            if (StringUtils.hasText(primarySmtpAddress)) {
                return primarySmtpAddress;
            }
        } catch (Exception ignored) {
        }
        try {
            Dispatch exchangeDistributionList = Dispatch.call(addressEntry, "GetExchangeDistributionList").toDispatch();
            String primarySmtpAddress = safeString(exchangeDistributionList, "PrimarySmtpAddress");
            if (StringUtils.hasText(primarySmtpAddress)) {
                return primarySmtpAddress;
            }
        } catch (Exception ignored) {
        }
        try {
            return safeString(addressEntry, "Address");
        } catch (Exception ignored) {
            return null;
        }
    }

    private String readRecipientEmail(Dispatch recipient) {
        try {
            Dispatch addressEntry = Dispatch.get(recipient, "AddressEntry").toDispatch();
            String addressEntryType = safeString(addressEntry, "Type");
            if ("SMTP".equalsIgnoreCase(addressEntryType)) {
                String address = safeString(addressEntry, "Address");
                if (StringUtils.hasText(address)) {
                    return address;
                }
            }
            if ("EX".equalsIgnoreCase(addressEntryType)) {
                String smtp = readPrimarySmtpFromAddressEntry(addressEntry);
                if (StringUtils.hasText(smtp)) {
                    return smtp;
                }
            }
        } catch (Exception ignored) {
        }
        try {
            String address = safeString(recipient, "Address");
            if (StringUtils.hasText(address)) {
                return address;
            }
        } catch (Exception ignored) {
        }
        try {
            return safeString(recipient, "Name");
        } catch (Exception ignored) {
            return null;
        }
    }

    private void setIfPresent(Dispatch item, String property, String value) {
        if (value != null) {
            Dispatch.put(item, property, value);
        }
    }

    private String safeString(Dispatch dispatch, String property) {
        try {
            Variant variant = Dispatch.get(dispatch, property);
            return variant == null || variant.isNull() ? null : variant.toString();
        } catch (Exception ex) {
            throw new OutlookComException("No se pudo leer la propiedad de Outlook: " + property, ex);
        }
    }

    private boolean safeBoolean(Dispatch dispatch, String property) {
        try {
            Variant variant = Dispatch.get(dispatch, property);
            return variant != null && !variant.isNull() && variant.getBoolean();
        } catch (Exception ex) {
            throw new OutlookComException("No se pudo leer la propiedad booleana de Outlook: " + property, ex);
        }
    }

    private long safeLong(Dispatch dispatch, String property) {
        try {
            Variant variant = Dispatch.get(dispatch, property);
            if (variant == null || variant.isNull()) {
                return 0L;
            }
            try {
                return variant.getLong();
            } catch (Exception ignored) {
            }
            try {
                return variant.getInt();
            } catch (Exception ignored) {
            }
            String value = variant.toString();
            if (!StringUtils.hasText(value)) {
                return 0L;
            }
            return Long.parseLong(value.trim());
        } catch (Exception ex) {
            throw new OutlookComException("No se pudo leer la propiedad numerica de Outlook: " + property, ex);
        }
    }

    private int safeInt(Dispatch dispatch, String property) {
        try {
            Variant variant = Dispatch.get(dispatch, property);
            if (variant == null || variant.isNull()) {
                return 0;
            }
            try {
                return variant.getInt();
            } catch (Exception ignored) {
            }
            String value = variant.toString();
            if (!StringUtils.hasText(value)) {
                return 0;
            }
            return Integer.parseInt(value.trim());
        } catch (Exception ex) {
            throw new OutlookComException("No se pudo leer la propiedad entera de Outlook: " + property, ex);
        }
    }

    private OffsetDateTime safeDate(Dispatch dispatch, String property) {
        try {
            Variant variant = Dispatch.get(dispatch, property);
            if (variant == null || variant.isNull()) {
                return null;
            }
            OffsetDateTime directDate = tryConvertVariantDate(variant);
            if (directDate != null) {
                return directDate;
            }
            String value = variant.toString();
            if (!StringUtils.hasText(value)) {
                return null;
            }
        } catch (Exception ex) {
            throw new OutlookComException("No se pudo leer la propiedad fecha de Outlook: " + property, ex);
        }
        return null;
    }

    private OffsetDateTime tryConvertVariantDate(Variant variant) {
        Object candidate = invokeVariantMethod(variant, "toJavaObject");
        OffsetDateTime fromObject = toOffsetDateTime(candidate);
        if (fromObject != null) {
            return fromObject;
        }
        candidate = invokeVariantMethod(variant, "getDate");
        fromObject = toOffsetDateTime(candidate);
        if (fromObject != null) {
            return fromObject;
        }
        String text = variant.toString();
        if (!StringUtils.hasText(text)) {
            return null;
        }
        for (DateTimeFormatter formatter : OUTLOOK_DATE_FORMATS) {
            try {
                LocalDateTime localDateTime = LocalDateTime.parse(text.trim(), formatter);
                return localDateTime.atZone(ZoneId.systemDefault()).toOffsetDateTime();
            } catch (DateTimeParseException ignored) {
            }
        }
        try {
            return OffsetDateTime.parse(text.trim());
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }

    private Object invokeVariantMethod(Variant variant, String methodName) {
        try {
            Method method = Variant.class.getMethod(methodName);
            return method.invoke(variant);
        } catch (Exception ignored) {
            return null;
        }
    }

    private OffsetDateTime toOffsetDateTime(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof OffsetDateTime offsetDateTime) {
            return offsetDateTime;
        }
        if (value instanceof Date date) {
            return OffsetDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        }
        if (value instanceof Instant instant) {
            return OffsetDateTime.ofInstant(instant, ZoneId.systemDefault());
        }
        if (value instanceof LocalDateTime localDateTime) {
            return localDateTime.atZone(ZoneId.systemDefault()).toOffsetDateTime();
        }
        return null;
    }

    private Path createTempAttachmentPath(String originalName) {
        try {
            Path tempDir = Path.of(properties.getTempDir());
            Files.createDirectories(tempDir);
            String safeName = StringUtils.hasText(originalName) ? originalName : "attachment.bin";
            return Files.createTempFile(tempDir, "outlook-", "-" + safeName);
        } catch (IOException ex) {
            throw new OutlookComException("No se pudo crear el archivo temporal para adjuntos", ex);
        }
    }

    private String probeMediaType(Path file) {
        try {
            String mediaType = Files.probeContentType(file);
            if (StringUtils.hasText(mediaType)) {
                return mediaType;
            }
        } catch (IOException ignored) {
        }
        return MediaType.APPLICATION_OCTET_STREAM_VALUE;
    }

    private void deleteQuietly(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
        }
    }

    private <T> T withApplication(OutlookApplicationCallback<T> callback) {
        try {
            jacobLibraryService.ensureLoaded();
            ActiveXComponent application = new ActiveXComponent("Outlook.Application");
            return callback.doWithApplication(application);
        } catch (UnsatisfiedLinkError ex) {
            throw new OutlookComException("La DLL de JACOB no se ha podido cargar. Verifica jacob.dll.path y la arquitectura x86/x64.", ex);
        } catch (ComFailException ex) {
            throw new OutlookComException("No se pudo abrir Outlook Desktop via COM. Verifica que Outlook este instalado y configurado.", ex);
        }
    }

    private <T> T withNamespace(OutlookNamespaceCallback<T> callback) {
        return withApplication(application -> {
            Dispatch namespace = application.invoke("GetNamespace", "MAPI").toDispatch();
            return callback.doWithNamespace(namespace);
        });
    }

    @FunctionalInterface
    private interface OutlookApplicationCallback<T> {
        T doWithApplication(ActiveXComponent application);
    }

    @FunctionalInterface
    private interface OutlookNamespaceCallback<T> {
        T doWithNamespace(Dispatch namespace);
    }
}


