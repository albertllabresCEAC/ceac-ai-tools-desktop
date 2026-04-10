package tools.ceac.ai.modules.campus.infrastructure.campus;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

/**
 * Extracts the sesskey from the Moodle quiz override edit form
 * (/mod/quiz/overrideedit.php?action=adduser&cmid=N).
 */
@Component
public class MoodleQuizOverrideEditParser {

    public String parseSesskey(String html, String baseUrl) {
        Document doc = Jsoup.parse(html, baseUrl);
        Element input = doc.selectFirst("input[name='sesskey']");
        return input != null ? input.attr("value") : "";
    }
}

