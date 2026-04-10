package tools.ceac.ai.mcp.campus.infrastructure.campus;

import tools.ceac.ai.mcp.campus.domain.model.MessageRecipient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Extracts hidden form fields from the itop_mailbox compose page
 * (/blocks/itop_mailbox/compose.php?message=...&action=reply&messageid=...).
 */
@Component
public class MoodleComposeFormParser {

    public record ComposeFormData(String sesskey, String course, String replyto, String itemid, String attachments, String recipients) {}

    public ComposeFormData parse(String html, String baseUrl) {
        Document doc = Jsoup.parse(html, baseUrl);

        String sesskey = attr(doc, "input[name='sesskey']", "value");
        String course = attr(doc, "input[name='course']", "value");

        String replytoRaw = attr(doc, "input[name='replyto']", "value");
        String replyto = replytoRaw.startsWith("user-") ? replytoRaw.substring(5) : replytoRaw;
        String itemid = attr(doc, "input[name='content[itemid]']", "value");
        String attachments = attr(doc, "input[name='attachments']", "value");

        // recipients is a <select>, not a hidden input
        Element recipientsEl = doc.selectFirst("select[name='recipients'] option");
        String recipients = recipientsEl != null ? recipientsEl.attr("value") : "user-" + replyto;

        return new ComposeFormData(sesskey, course, replyto, itemid, attachments, recipients);
    }

    /**
     * Parses the compose form HTML (obtained via allcourses=1 POST) and returns all potential
     * message recipients from the {@code <optgroup label="Alumnos">} section of the recipients select.
     */
    public List<MessageRecipient> parseRecipients(String html) {
        Document doc = Jsoup.parse(html);
        return doc.select("select[name='recipients[]'] optgroup[label='Alumnos'] option")
                .stream()
                .map(el -> {
                    String value = el.attr("value"); // e.g. "user-1657"
                    String id = value.startsWith("user-") ? value.substring(5) : value;
                    return new MessageRecipient(id, el.text());
                })
                .toList();
    }

    private String attr(Document doc, String selector, String attr) {
        Element el = doc.selectFirst(selector);
        return el != null ? el.attr(attr) : "";
    }
}