package tools.ceac.ai.mcp.campus.infrastructure.campus;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import tools.ceac.ai.mcp.campus.domain.model.Conversation;
import tools.ceac.ai.mcp.campus.domain.model.ConversationMember;
import tools.ceac.ai.mcp.campus.domain.model.ConversationMessage;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Parses the JSON response from Moodle's core_message_get_conversations AJAX call.
 */
@Component
public class MoodleConversationsParser {

    private final ObjectMapper objectMapper;

    public MoodleConversationsParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<Conversation> parse(String json) throws IOException {
        AjaxResponse[] responses = objectMapper.readValue(json, AjaxResponse[].class);
        if (responses == null || responses.length == 0 || responses[0].data == null) {
            return Collections.emptyList();
        }
        List<JsonConversation> raw = responses[0].data.conversations;
        if (raw == null) return Collections.emptyList();

        return raw.stream().map(this::toDomain).toList();
    }

    private Conversation toDomain(JsonConversation c) {
        List<ConversationMember> members = c.members == null ? List.of() :
                c.members.stream().map(m -> new ConversationMember(
                        m.id, m.fullname, m.profileimageurl,
                        m.isonline, m.isblocked, m.iscontact
                )).toList();

        List<ConversationMessage> messages = c.messages == null ? List.of() :
                c.messages.stream().map(m -> new ConversationMessage(
                        m.id, m.useridfrom, m.text, m.timecreated
                )).toList();

        return new Conversation(
                c.id, c.name, c.type, c.unreadcount,
                c.isread, c.isfavourite, c.ismuted,
                members, messages
        );
    }

    // ── JSON mapping classes ───────────────────────────────────────────────────

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class AjaxResponse {
        public boolean error;
        public JsonData data;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class JsonData {
        public List<JsonConversation> conversations;
        public int totalcount;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class JsonConversation {
        public long id;
        public String name;
        public int type;
        public Integer unreadcount;
        public boolean isread;
        public boolean isfavourite;
        public boolean ismuted;
        public List<JsonMember> members;
        public List<JsonMessage> messages;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class JsonMember {
        public long id;
        public String fullname;
        public String profileimageurl;
        public Boolean isonline;
        public boolean isblocked;
        public boolean iscontact;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class JsonMessage {
        public long id;
        public long useridfrom;
        public String text;
        public long timecreated;
    }
}