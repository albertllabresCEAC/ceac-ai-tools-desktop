package tools.ceac.ai.modules.outlook.domain.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.ai.tool.annotation.ToolParam;

public class MessageSearchRequest {

    @ToolParam(required = false, description = "Free-text query matched against subject, sender, recipients and body.")
    @Schema(description = "Free-text query matched against subject, sender, recipients and body.")
    private String query;

    @ToolParam(required = false, description = "Folder to search. Allowed values: INBOX, DRAFTS, SENT, OUTBOX, DELETED, ALL. Defaults to ALL.")
    @Schema(defaultValue = "ALL", allowableValues = {"INBOX", "DRAFTS", "SENT", "OUTBOX", "DELETED", "ALL"})
    private String folder = "ALL";

    @ToolParam(required = false, description = "Maximum number of matches to return.")
    @Min(1)
    @Max(200)
    @Schema(defaultValue = "20")
    private Integer limit = 20;

    @ToolParam(required = false, description = "When true, restricts the search to unread messages only.")
    @Schema(defaultValue = "false")
    private Boolean unreadOnly = false;

    @ToolParam(required = false, description = "Only include messages received on or after this ISO-8601 date-time.")
    @Schema(description = "Only include messages received on or after this ISO-8601 date-time.")
    private String since;

    @Schema(description = "Optional wrapper for MCP/API clients that send the payload as {\"request\": {...}}.")
    private MessageSearchRequest request;

    public String getQuery() {
        return query != null ? query : nestedValue(MessageSearchRequest::getQuery);
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getFolder() {
        return folder != null ? folder : nestedValue(MessageSearchRequest::getFolder);
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }

    public Integer getLimit() {
        return limit != null ? limit : nestedValue(MessageSearchRequest::getLimit);
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public Boolean getUnreadOnly() {
        return unreadOnly != null ? unreadOnly : nestedValue(MessageSearchRequest::getUnreadOnly);
    }

    public void setUnreadOnly(Boolean unreadOnly) {
        this.unreadOnly = unreadOnly;
    }

    public String getSince() {
        return since != null ? since : nestedValue(MessageSearchRequest::getSince);
    }

    public void setSince(String since) {
        this.since = since;
    }

    public MessageSearchRequest getRequest() {
        return request;
    }

    public void setRequest(MessageSearchRequest request) {
        this.request = request;
    }

    private <T> T nestedValue(java.util.function.Function<MessageSearchRequest, T> extractor) {
        if (request == null) {
            return null;
        }
        return extractor.apply(request);
    }
}
