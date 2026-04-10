package tools.ceac.ai.modules.campus.infrastructure.campus;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import tools.ceac.ai.modules.campus.domain.model.ConversationDetail;
import tools.ceac.ai.modules.campus.domain.model.ConversationMember;
import tools.ceac.ai.modules.campus.domain.model.ConversationMessage;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Parses the JSON response from Moodle's core_message_get_conversation_messages AJAX call.
 */
@Component
public class MoodleConversationMessagesParser {

    private final ObjectMapper objectMapper;

    public MoodleConversationMessagesParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ConversationDetail parse(String json) throws IOException {
        AjaxResponse[] responses = objectMapper.readValue(json, AjaxResponse[].class);
        if (responses == null || responses.length == 0 || responses[0].data == null) {
            return new ConversationDetail(0, Collections.emptyList(), Collections.emptyList());
        }
        JsonData data = responses[0].data;

        List<ConversationMember> members = data.members == null ? List.of() :
                data.members.stream().map(m -> new ConversationMember(
                        m.id, m.fullname, m.profileimageurl,
                        m.isonline, m.isblocked, m.iscontact
                )).toList();

        List<ConversationMessage> messages = data.messages == null ? List.of() :
                data.messages.stream().map(m -> new ConversationMessage(
                        m.id, m.useridfrom, m.text, m.timecreated
                )).toList();

        return new ConversationDetail(data.id, members, messages);
    }

    // â”€â”€ JSON mapping classes â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class AjaxResponse {
        public boolean error;
        public JsonData data;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class JsonData {
        public long id;
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

