package tools.ceac.ai.modules.campus.infrastructure.campus;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import tools.ceac.ai.modules.campus.domain.model.MessageRecipient;
import org.springframework.stereotype.Component;

/**
 * Parses the JSON response from Moodle's {@code core_message_message_search_users} AJAX call.
 *
 * <p>The runtime exposes a simplified directory shape and only keeps the Moodle user id and full
 * name.
 */
@Component
public class MoodleSearchUsersParser {

    private final ObjectMapper objectMapper;

    public MoodleSearchUsersParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Parses only the {@code contacts} branch from the Moodle response.
     */
    public List<MessageRecipient> parse(String json) throws IOException {
        AjaxResponse[] responses = objectMapper.readValue(json, AjaxResponse[].class);
        if (responses == null || responses.length == 0 || responses[0].data == null) {
            return Collections.emptyList();
        }
        return flatten(responses[0].data.contacts)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private List<JsonUser> flatten(List<JsonUser> contacts) {
        if (contacts == null || contacts.isEmpty()) {
            return List.of();
        }
        return contacts;
    }

    private MessageRecipient toDomain(JsonUser user) {
        return new MessageRecipient(String.valueOf(user.id), user.fullname);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class AjaxResponse {
        public boolean error;
        public JsonData data;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class JsonData {
        public List<JsonUser> contacts;
        public List<JsonUser> noncontacts;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class JsonUser {
        public long id;
        public String fullname;
    }

    /**
     * Parses and merges both {@code contacts} and {@code noncontacts} from Moodle's response.
     */
    public List<MessageRecipient> parseMerged(String json) throws IOException {
        AjaxResponse[] responses = objectMapper.readValue(json, AjaxResponse[].class);
        if (responses == null || responses.length == 0 || responses[0].data == null) {
            return Collections.emptyList();
        }
        JsonData data = responses[0].data;
        List<JsonUser> contacts = data.contacts == null ? List.of() : data.contacts;
        List<JsonUser> noncontacts = data.noncontacts == null ? List.of() : data.noncontacts;
        return java.util.stream.Stream.concat(contacts.stream(), noncontacts.stream())
                .map(this::toDomain)
                .toList();
    }
}
