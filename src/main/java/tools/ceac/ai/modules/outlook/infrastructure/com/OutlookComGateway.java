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
import tools.ceac.ai.modules.outlook.domain.model.MessageSearchRequest;
import tools.ceac.ai.modules.outlook.domain.model.MessageSearchResult;
import tools.ceac.ai.modules.outlook.domain.model.SendMailRequest;
import tools.ceac.ai.modules.outlook.domain.model.StatusResponse;
import tools.ceac.ai.modules.outlook.domain.model.UpdateDraftRequest;
import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.ComFailException;
import com.jacob.com.Dispatch;
import com.jacob.com.Variant;
import org.jsoup.Jsoup;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * COM-backed implementation of {@link OutlookGateway} using the JACOB library to communicate
 * with the locally installed Outlook Desktop application via the Windows COM/MAPI interface.
 *
 * <h2>Performance strategy</h2>
 * <ul>
 *   <li><strong>Server-side filtering</strong> — {@code listMessages} and {@code searchMessages}
 *       apply a DASL filter ({@code @SQL=...}) via {@code Items.Restrict()} or
 *       {@code MAPIFolder.GetTable(filter)} before iterating any item. This ensures that Outlook
 *       itself narrows the candidate set by {@code ReceivedTime} and {@code UnRead} state, so only
 *       matching items cross the COM boundary.</li>
 *   <li><strong>Lightweight listing</strong> — {@code listMessages} reads body and attachment
 *       metadata per item but deliberately skips {@code htmlBody}, {@code bcc} and {@code sentAt},
 *       which are not available from the Table API and require a heavier per-item load.</li>
 *   <li><strong>Lazy body resolution</strong> — {@code matchesSearchQuery} checks cheap scalar
 *       properties ({@code Subject}, {@code SenderName}, {@code SenderEmailAddress}, {@code To},
 *       {@code CC}) before falling back to {@code readBodyText}, which may issue up to six COM
 *       round-trips. Short-circuit evaluation ensures the body is only read when earlier checks
 *       all miss.</li>
 *   <li><strong>Cached reflection</strong> — {@code Variant} method handles are resolved once at
 *       class load time and reused, avoiding repeated {@link Class#getMethod} lookups in the
 *       hot date-conversion path.</li>
 * </ul>
 *
 * <h2>Date format for DASL filters</h2>
 * All date filters sent to Outlook use the locale-independent DASL property
 * {@code "urn:schemas:httpmail:date"} with a UTC timestamp in
 * {@code yyyy-MM-dd HH:mm:ss} format. This avoids locale-specific JET filter failures on
 * non-US Windows installations.
 */
@Service
public class OutlookComGateway implements OutlookGateway {

    private static final int OL_MAIL_ITEM = 0;
    private static final int DEFAULT_LOOKBACK_DAYS = 7;
    private static final String MAPI_BODY_PROPERTY = "http://schemas.microsoft.com/mapi/proptag/0x1000001F";
    private static final String MAPI_BODY_PROPERTY_STRING8 = "http://schemas.microsoft.com/mapi/proptag/0x1000001E";
    private static final String MAPI_HTML_PROPERTY = "http://schemas.microsoft.com/mapi/proptag/0x1013001F";
    private static final String MAPI_HTML_PROPERTY_STRING8 = "http://schemas.microsoft.com/mapi/proptag/0x1013001E";
    private static final String MAPI_HAS_ATTACH_PROPERTY = "http://schemas.microsoft.com/mapi/proptag/0x0E1B000B";
    private static final String MAPI_RECEIVED_TIME_FILTER_PROPERTY =
            "https://schemas.microsoft.com/mapi/proptag/0x0E060040";
    private static final String MAPI_READ_FILTER_PROPERTY =
            "https://schemas.microsoft.com/mapi/proptag/0x0E69000B";
    private static final List<String> TABLE_COLUMNS = List.of(
            "EntryID",
            "Subject",
            "SenderName",
            "SenderEmailAddress",
            "ReceivedTime",
            "UnRead",
            "Size",
            "To",
            "CC"
    );
    private static final DateTimeFormatter RESTRICT_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
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

    private static final Method VARIANT_TO_JAVA_OBJECT = resolveVariantMethod("toJavaObject");
    private static final Method VARIANT_GET_DATE = resolveVariantMethod("getDate");

    private static Method resolveVariantMethod(String name) {
        try {
            return Variant.class.getMethod(name);
        } catch (Exception ignored) {
            return null;
        }
    }

    private final OutlookProperties properties;
    private final JacobLibraryService jacobLibraryService;
    private final Map<String, String> storeIdByEntryId = new ConcurrentHashMap<>();

    public OutlookComGateway(OutlookProperties properties, JacobLibraryService jacobLibraryService) {
        this.properties = properties;
        this.jacobLibraryService = jacobLibraryService;
    }

    public List<MailMessage> listMessages(MessageQuery query) {
        int limit = query.getLimit() != null ? query.getLimit() : properties.getDefaultPageSize();
        return withNamespace(namespace -> {
            Dispatch folder = getDefaultFolder(namespace, query.getFolder());
            boolean descending = isDescendingSort(query);
            OffsetDateTime since = effectiveSince(query);
            try {
                return listMessagesWithTable(namespace, folder, limit, since, query.isUnreadOnly(), descending);
            } catch (Exception ex) {
                return listMessagesWithItems(folder, limit, since, query.isUnreadOnly(), descending);
            }
        });
    }

    public List<MessageSearchResult> searchMessages(MessageSearchRequest request) {
        MessageSearchRequest effectiveRequest = request != null ? request : new MessageSearchRequest();
        int limit = sanitizeSearchLimit(effectiveRequest.getLimit());
        boolean unreadOnly = Boolean.TRUE.equals(effectiveRequest.getUnreadOnly());
        OffsetDateTime since = parseBoundary(effectiveRequest.getSince(), "since");
        OffsetDateTime until = parseBoundary(effectiveRequest.getUntil(), "until");
        validateSearchRange(since, until);
        String normalizedQuery = normalizeQuery(effectiveRequest.getQuery());

        return withNamespace(namespace -> {
            Map<String, MessageSearchResult> matches = new LinkedHashMap<>();
            for (FolderType folderType : resolveSearchFolders(effectiveRequest.getFolder())) {
                Dispatch folder = getDefaultFolder(namespace, folderType);
                for (MessageSearchResult match : searchFolder(namespace, folder, folderType, normalizedQuery, unreadOnly, since, until)) {
                    matches.putIfAbsent(match.entryId(), match);
                }
            }
            return matches.values().stream()
                    .sorted(Comparator.comparing(this::sortTimestamp, Comparator.nullsLast(Comparator.reverseOrder())))
                    .limit(limit)
                    .toList();
        });
    }

    public MailMessage getMessage(String entryId) {
        return withNamespace(namespace -> toFullMailMessage(getItemFromId(namespace, entryId)));
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

    private List<FolderType> resolveSearchFolders(String folder) {
        if (!StringUtils.hasText(folder) || "ALL".equalsIgnoreCase(folder.trim())) {
            return List.of(FolderType.INBOX, FolderType.SENT, FolderType.DRAFTS);
        }
        try {
            return List.of(FolderType.valueOf(folder.trim().toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException exception) {
            throw new OutlookComException("Folder invalida. Usa: INBOX, DRAFTS, SENT, OUTBOX, DELETED, ALL", exception);
        }
    }

    private Dispatch getItemFromId(Dispatch namespace, String entryId) {
        if (!StringUtils.hasText(entryId)) {
            throw new OutlookComException("entryId is required");
        }
        List<String> candidates = new ArrayList<>();
        String cachedStoreId = storeIdByEntryId.get(entryId);
        if (StringUtils.hasText(cachedStoreId)) {
            candidates.add(cachedStoreId);
        }
        for (String storeId : resolveStoreIds(namespace)) {
            if (!candidates.contains(storeId)) {
                candidates.add(storeId);
            }
        }
        return getItemFromId(namespace, entryId, candidates);
    }

    private Dispatch getItemFromId(Dispatch namespace, String entryId, List<String> storeIds) {
        if (!StringUtils.hasText(entryId)) {
            throw new OutlookComException("entryId is required");
        }
        for (String storeId : storeIds) {
            if (!StringUtils.hasText(storeId)) {
                continue;
            }
            try {
                Dispatch item = Dispatch.call(namespace, "GetItemFromID", entryId, storeId).toDispatch();
                storeIdByEntryId.put(entryId, storeId);
                return item;
            } catch (ComFailException ignored) {
            }
        }
        try {
            Dispatch item = Dispatch.call(namespace, "GetItemFromID", entryId).toDispatch();
            String resolvedStoreId = resolveStoreIdFromItem(item);
            if (StringUtils.hasText(resolvedStoreId)) {
                storeIdByEntryId.put(entryId, resolvedStoreId);
            }
            return item;
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

    private MailMessage toFullMailMessage(Dispatch item) {
        ensureMailItem(item);
        String plainBody = readBodyText(item);
        String htmlBody = readHtmlBody(item);
        return new MailMessage(
                safeString(item, "EntryID"),
                safeString(item, "Subject"),
                safeString(item, "SenderName"),
                readSenderEmail(item),
                readRecipients(item, 1),
                readRecipients(item, 2),
                readRecipients(item, 3),
                plainBody,
                htmlBody,
                safeBoolean(item, "UnRead"),
                safeDate(item, "ReceivedTime"),
                safeDate(item, "SentOn"),
                hasAttachments(item) ? readAttachments(item) : List.of()
        );
    }

    private MailMessage toMailMessageSummary(
            Dispatch row,
            String to,
            String cc,
            String bcc,
            String body,
            List<AttachmentInfo> attachments
    ) {
        return new MailMessage(
                safeRowString(row, "EntryID"),
                safeRowString(row, "Subject"),
                safeRowString(row, "SenderName"),
                safeRowString(row, "SenderEmailAddress"),
                to,
                cc,
                bcc,
                body,
                null,
                safeRowBoolean(row, "UnRead"),
                safeRowDate(row, "ReceivedTime"),
                null,
                attachments
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
        String directRecipients = switch (recipientType) {
            case 1 -> safeStringOrNull(item, "To");
            case 2 -> safeStringOrNull(item, "CC");
            case 3 -> safeStringOrNull(item, "BCC");
            default -> null;
        };
        if (StringUtils.hasText(directRecipients)) {
            return directRecipients;
        }
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

    private String safeStringOrNull(Dispatch dispatch, String property) {
        try {
            Variant variant = Dispatch.get(dispatch, property);
            return variant == null || variant.isNull() ? null : variant.toString();
        } catch (Exception ignored) {
            return null;
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
        Method method = "toJavaObject".equals(methodName) ? VARIANT_TO_JAVA_OBJECT
                : "getDate".equals(methodName) ? VARIANT_GET_DATE
                : null;
        if (method == null) return null;
        try {
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

    private List<MailMessage> listMessagesWithTable(
            Dispatch namespace,
            Dispatch folder,
            int limit,
            OffsetDateTime since,
            boolean unreadOnly,
            boolean descending
    ) {
        String filter = buildRestrictFilter(since, unreadOnly);
        Dispatch table = filter != null
                ? Dispatch.call(folder, "GetTable", filter).toDispatch()
                : Dispatch.call(folder, "GetTable").toDispatch();
        Dispatch columns = Dispatch.get(table, "Columns").toDispatch();
        Dispatch.call(columns, "RemoveAll");
        for (String column : TABLE_COLUMNS) {
            Dispatch.call(columns, "Add", column);
        }
        Dispatch.call(table, "Sort", "[ReceivedTime]", new Variant(descending));

        List<MailMessage> result = new ArrayList<>(limit);
        while (!Dispatch.get(table, "EndOfTable").getBoolean() && result.size() < limit) {
            Dispatch row = Dispatch.call(table, "GetNextRow").toDispatch();
            OffsetDateTime receivedAt = safeRowDate(row, "ReceivedTime");
            boolean unread = safeRowBoolean(row, "UnRead");
            if (!matchesListFilter(receivedAt, unread, since, unreadOnly)) {
                if (canStopScanning(receivedAt, since, descending)) {
                    break;
                }
                continue;
            }
            String to = safeRowString(row, "To");
            String cc = safeRowString(row, "CC");
            String entryId = safeRowString(row, "EntryID");
            String body = null;
            List<AttachmentInfo> attachments = List.of();
            if (StringUtils.hasText(entryId)) {
                try {
                    Dispatch item = getItemFromId(namespace, entryId, List.of());
                    body = readBodyText(item);
                    attachments = hasAttachments(item) ? readAttachments(item) : List.of();
                } catch (Exception ignored) {
                }
            }
            result.add(toMailMessageSummary(row, to, cc, null, body, attachments));
        }
        return result;
    }

    private List<MailMessage> listMessagesWithItems(
            Dispatch folder,
            int limit,
            OffsetDateTime since,
            boolean unreadOnly,
            boolean descending
    ) {
        Dispatch items = Dispatch.get(folder, "Items").toDispatch();
        String filter = buildRestrictFilter(since, unreadOnly);
        Dispatch collection;
        try {
            collection = filter != null ? Dispatch.call(items, "Restrict", filter).toDispatch() : items;
        } catch (Exception ignored) {
            collection = items;
        }
        Dispatch.call(collection, "Sort", "[ReceivedTime]", new Variant(descending));

        List<MailMessage> result = new ArrayList<>();
        int count = Dispatch.get(collection, "Count").getInt();
        for (int index = 1; index <= count && result.size() < limit; index++) {
            Dispatch item = Dispatch.call(collection, "Item", new Variant(index)).toDispatch();
            if (!"IPM.Note".equalsIgnoreCase(safeString(item, "MessageClass"))) {
                continue;
            }
            OffsetDateTime receivedAt = safeDate(item, "ReceivedTime");
            boolean unread = safeBoolean(item, "UnRead");
            if (!matchesListFilter(receivedAt, unread, since, unreadOnly)) {
                if (canStopScanning(receivedAt, since, descending)) {
                    break;
                }
                continue;
            }
            result.add(toMailMessageLight(item));
        }
        return result;
    }

    private List<MessageSearchResult> searchFolder(
            Dispatch namespace,
            Dispatch folder,
            FolderType folderType,
            String normalizedQuery,
            boolean unreadOnly,
            OffsetDateTime since,
            OffsetDateTime until
    ) {
        Dispatch items = Dispatch.get(folder, "Items").toDispatch();
        String filter = buildRestrictFilter(since, until, unreadOnly);
        Dispatch collection;
        try {
            collection = filter != null ? Dispatch.call(items, "Restrict", filter).toDispatch() : items;
        } catch (Exception ignored) {
            collection = items;
        }
        Dispatch.call(collection, "Sort", "[ReceivedTime]", new Variant(true));

        List<MessageSearchResult> result = new ArrayList<>();
        int count = Dispatch.get(collection, "Count").getInt();
        String storeId = resolveStoreId(folder);
        for (int index = 1; index <= count; index++) {
            Dispatch item = Dispatch.call(collection, "Item", new Variant(index)).toDispatch();
            if (!"IPM.Note".equalsIgnoreCase(safeString(item, "MessageClass"))) {
                continue;
            }
            OffsetDateTime receivedAt = safeDate(item, "ReceivedTime");
            boolean unread = safeBoolean(item, "UnRead");
            if (!matchesSearchFilter(receivedAt, unread, since, until, unreadOnly)) {
                if (canStopScanning(receivedAt, since, true)) {
                    break;
                }
                continue;
            }
            String entryId = safeString(item, "EntryID");
            if (!StringUtils.hasText(entryId)) {
                continue;
            }
            if (!matchesSearchQuery(item, normalizedQuery)) {
                continue;
            }
            if (StringUtils.hasText(storeId)) {
                storeIdByEntryId.putIfAbsent(entryId, storeId);
            }
            result.add(toSearchResult(item, folderType));
        }
        return result;
    }

    private boolean isDescendingSort(MessageQuery query) {
        return !"asc".equalsIgnoreCase(query.getSortOrder());
    }

    private int sanitizeSearchLimit(Integer limit) {
        if (limit == null) {
            return 20;
        }
        return Math.max(1, Math.min(limit, 200));
    }

    private String normalizeQuery(String query) {
        return StringUtils.hasText(query) ? query.trim().toLowerCase(Locale.ROOT) : null;
    }

    private OffsetDateTime parseBoundary(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String normalized = value.trim();
        try {
            return OffsetDateTime.parse(normalized);
        } catch (DateTimeParseException ignored) {
        }
        try {
            return OffsetDateTime.ofInstant(Instant.parse(normalized), ZoneId.systemDefault());
        } catch (DateTimeParseException ignored) {
        }
        try {
            return LocalDateTime.parse(normalized).atZone(ZoneId.systemDefault()).toOffsetDateTime();
        } catch (DateTimeParseException exception) {
            throw new OutlookComException(fieldName + " debe ser una fecha ISO-8601 valida", exception);
        }
    }

    private void validateSearchRange(OffsetDateTime since, OffsetDateTime until) {
        if (since != null && until != null && until.isBefore(since)) {
            throw new OutlookComException("until debe ser mayor o igual que since");
        }
    }

    private OffsetDateTime effectiveSince(MessageQuery query) {
        return query.getSince() != null
                ? query.getSince()
                : OffsetDateTime.now(ZoneId.systemDefault()).minusDays(DEFAULT_LOOKBACK_DAYS);
    }

    private boolean matchesListFilter(OffsetDateTime receivedAt, boolean unread, OffsetDateTime since, boolean unreadOnly) {
        if (unreadOnly && !unread) {
            return false;
        }
        if (since != null && receivedAt != null && receivedAt.isBefore(since)) {
            return false;
        }
        return since == null || receivedAt != null;
    }

    private boolean matchesSearchFilter(
            OffsetDateTime receivedAt,
            boolean unread,
            OffsetDateTime since,
            OffsetDateTime until,
            boolean unreadOnly
    ) {
        if (!matchesListFilter(receivedAt, unread, since, unreadOnly)) {
            return false;
        }
        if (until != null && receivedAt != null && receivedAt.isAfter(until)) {
            return false;
        }
        return until == null || receivedAt != null;
    }

    private boolean canStopScanning(OffsetDateTime receivedAt, OffsetDateTime since, boolean descending) {
        return descending && since != null && receivedAt != null && receivedAt.isBefore(since);
    }

    private boolean matchesSearchQuery(Dispatch item, String normalizedQuery) {
        if (!StringUtils.hasText(normalizedQuery)) {
            return true;
        }
        if (containsIgnoreCase(safeStringOrNull(item, "Subject"), normalizedQuery)) return true;
        if (containsIgnoreCase(safeStringOrNull(item, "SenderName"), normalizedQuery)) return true;
        if (containsIgnoreCase(safeStringOrNull(item, "SenderEmailAddress"), normalizedQuery)) return true;
        if (containsIgnoreCase(safeStringOrNull(item, "To"), normalizedQuery)) return true;
        if (containsIgnoreCase(safeStringOrNull(item, "CC"), normalizedQuery)) return true;
        if (containsIgnoreCase(safeStringOrNull(item, "BCC"), normalizedQuery)) return true;
        return containsIgnoreCase(readBodyText(item), normalizedQuery);
    }

    private boolean containsIgnoreCase(String value, String query) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(query);
    }

    private String buildRestrictFilter(OffsetDateTime since, boolean unreadOnly) {
        return buildRestrictFilter(since, null, unreadOnly);
    }

    private String buildRestrictFilter(OffsetDateTime since, OffsetDateTime until, boolean unreadOnly) {
        List<String> parts = new ArrayList<>();
        if (since != null) {
            String utcDate = since.withOffsetSameInstant(ZoneOffset.UTC).format(RESTRICT_DATE_FORMAT);
            parts.add("\"urn:schemas:httpmail:date\" >= '" + utcDate + "'");
        }
        if (until != null) {
            String utcDate = until.withOffsetSameInstant(ZoneOffset.UTC).format(RESTRICT_DATE_FORMAT);
            parts.add("\"urn:schemas:httpmail:date\" <= '" + utcDate + "'");
        }
        if (unreadOnly) {
            parts.add("\"urn:schemas:httpmail:read\" = 0");
        }
        return parts.isEmpty() ? null : "@SQL=" + String.join(" AND ", parts);
    }

    private MailMessage toMailMessageLight(Dispatch item) {
        return new MailMessage(
                safeString(item, "EntryID"),
                safeString(item, "Subject"),
                safeString(item, "SenderName"),
                safeStringOrNull(item, "SenderEmailAddress"),
                safeStringOrNull(item, "To"),
                safeStringOrNull(item, "CC"),
                null,
                readBodyText(item),
                null,
                safeBoolean(item, "UnRead"),
                safeDate(item, "ReceivedTime"),
                null,
                hasAttachments(item) ? readAttachments(item) : List.of()
        );
    }

    private MailMessage toListMailMessageFromItem(Dispatch item) {
        ensureMailItem(item);
        return new MailMessage(
                safeString(item, "EntryID"),
                safeString(item, "Subject"),
                safeString(item, "SenderName"),
                readSenderEmail(item),
                readRecipients(item, 1),
                readRecipients(item, 2),
                readRecipients(item, 3),
                readBodyText(item),
                null,
                safeBoolean(item, "UnRead"),
                safeDate(item, "ReceivedTime"),
                null,
                hasAttachments(item) ? readAttachments(item) : List.of()
        );
    }

    private MessageSearchResult toSearchResult(Dispatch item, FolderType folderType) {
        return new MessageSearchResult(
                safeString(item, "EntryID"),
                folderType.name(),
                safeString(item, "Subject"),
                safeString(item, "SenderName"),
                readSenderEmail(item),
                safeStringOrNull(item, "To"),
                safeStringOrNull(item, "CC"),
                safeStringOrNull(item, "BCC"),
                readBodyText(item),
                safeBoolean(item, "UnRead"),
                safeDate(item, "ReceivedTime"),
                safeDate(item, "SentOn"),
                hasAttachments(item) ? readAttachments(item) : List.of()
        );
    }

    private OffsetDateTime sortTimestamp(MessageSearchResult result) {
        return result.receivedAt() != null ? result.receivedAt() : result.sentAt();
    }

    private Variant readRowValue(Dispatch row, String column) {
        try {
            return Dispatch.call(row, "Item", column);
        } catch (Exception ex) {
            throw new OutlookComException("No se pudo leer la columna de Outlook: " + column, ex);
        }
    }

    private String safeRowString(Dispatch row, String column) {
        Variant variant = readRowValue(row, column);
        return variant == null || variant.isNull() ? null : variant.toString();
    }

    private boolean safeRowBoolean(Dispatch row, String column) {
        Variant variant = readRowValue(row, column);
        return variant != null && !variant.isNull() && variant.getBoolean();
    }

    private OffsetDateTime safeRowDate(Dispatch row, String column) {
        Variant variant = readRowValue(row, column);
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
        throw new OutlookComException("No se pudo interpretar la columna fecha de Outlook: " + column);
    }

    private List<String> resolveStoreIds(Dispatch namespace) {
        List<String> storeIds = new ArrayList<>();
        try {
            Dispatch stores = Dispatch.get(namespace, "Stores").toDispatch();
            int count = Dispatch.get(stores, "Count").getInt();
            for (int index = 1; index <= count; index++) {
                Dispatch store = Dispatch.call(stores, "Item", new Variant(index)).toDispatch();
                String storeId = safeString(store, "StoreID");
                if (StringUtils.hasText(storeId)) {
                    storeIds.add(storeId);
                }
            }
        } catch (Exception ignored) {
        }
        return storeIds;
    }

    private String resolveStoreId(Dispatch folder) {
        try {
            String directStoreId = safeString(folder, "StoreID");
            if (StringUtils.hasText(directStoreId)) {
                return directStoreId;
            }
        } catch (Exception ignored) {
        }
        try {
            Dispatch store = Dispatch.get(folder, "Store").toDispatch();
            return safeString(store, "StoreID");
        } catch (Exception ignored) {
            return null;
        }
    }

    private String resolveStoreIdFromItem(Dispatch item) {
        try {
            Dispatch parent = Dispatch.get(item, "Parent").toDispatch();
            return resolveStoreId(parent);
        } catch (Exception ignored) {
            return null;
        }
    }

    private String readBodyText(Dispatch item) {
        String plainBody = firstNonBlank(
                readPropertyAccessorString(item, MAPI_BODY_PROPERTY),
                readPropertyAccessorString(item, MAPI_BODY_PROPERTY_STRING8)
        );
        if (StringUtils.hasText(plainBody)) {
            return plainBody;
        }
        String html = firstNonBlank(
                readPropertyAccessorString(item, MAPI_HTML_PROPERTY),
                readPropertyAccessorString(item, MAPI_HTML_PROPERTY_STRING8)
        );
        if (StringUtils.hasText(html)) {
            return htmlToText(html);
        }
        try {
            String directBody = safeString(item, "Body");
            if (directBody != null) {
                return directBody;
            }
        } catch (Exception ignored) {
        }
        try {
            String directHtml = safeString(item, "HTMLBody");
            if (StringUtils.hasText(directHtml)) {
                return htmlToText(directHtml);
            }
        } catch (Exception ignored) {
        }
        return "";
    }

    private String readHtmlBody(Dispatch item) {
        String html = firstNonBlank(
                readPropertyAccessorString(item, MAPI_HTML_PROPERTY),
                readPropertyAccessorString(item, MAPI_HTML_PROPERTY_STRING8)
        );
        return html != null ? html : "";
    }

    private boolean hasAttachments(Dispatch item) {
        Dispatch propertyAccessor = Dispatch.get(item, "PropertyAccessor").toDispatch();
        try {
            Variant variant = Dispatch.call(propertyAccessor, "GetProperty", MAPI_HAS_ATTACH_PROPERTY);
            return variant != null && !variant.isNull() && variant.getBoolean();
        } catch (Exception ignored) {
            return false;
        }
    }

    private String readPropertyAccessorString(Dispatch item, String propertyName) {
        Dispatch propertyAccessor = Dispatch.get(item, "PropertyAccessor").toDispatch();
        try {
            return variantToText(Dispatch.call(propertyAccessor, "GetProperty", propertyName));
        } catch (Exception ignored) {
            return null;
        }
    }

    private String variantToText(Variant variant) {
        if (variant == null || variant.isNull()) {
            return null;
        }
        Object candidate = invokeVariantMethod(variant, "toJavaObject");
        String fromObject = objectToText(candidate);
        if (fromObject != null) {
            return fromObject;
        }
        String text = variant.toString();
        return StringUtils.hasText(text) ? text : null;
    }

    private String objectToText(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String text) {
            return text;
        }
        if (value instanceof byte[] bytes) {
            return decodeBytes(bytes);
        }
        if (value.getClass().isArray()) {
            int length = java.lang.reflect.Array.getLength(value);
            byte[] bytes = new byte[length];
            for (int index = 0; index < length; index++) {
                Object element = java.lang.reflect.Array.get(value, index);
                if (!(element instanceof Number number)) {
                    return null;
                }
                bytes[index] = number.byteValue();
            }
            return decodeBytes(bytes);
        }
        return null;
    }

    private String decodeBytes(byte[] bytes) {
        if (bytes.length == 0) {
            return "";
        }
        Charset charset = looksLikeUtf16(bytes) ? StandardCharsets.UTF_16LE : StandardCharsets.UTF_8;
        return new String(bytes, charset).replace("\u0000", "");
    }

    private boolean looksLikeUtf16(byte[] bytes) {
        if (bytes.length < 2) {
            return false;
        }
        int zeroBytes = 0;
        for (int index = 1; index < bytes.length; index += 2) {
            if (bytes[index] == 0) {
                zeroBytes++;
            }
        }
        return zeroBytes > 0 && zeroBytes >= bytes.length / 4;
    }

    private String htmlToText(String html) {
        return StringUtils.hasText(html) ? Jsoup.parse(html).text() : "";
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value;
            }
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


