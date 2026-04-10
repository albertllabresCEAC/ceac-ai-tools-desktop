package tools.ceac.ai.modules.campus.infrastructure.campus;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Parses {@code question/bank/editquestion/question.php?id=XXXX&cmid=XXXX}
 * and extracts all hidden fields needed to reconstruct the POST submission.
 *
 * <p>Fields extracted:
 * <ul>
 *   <li>{@code sesskey}</li>
 *   <li>{@code questiontext[itemid]}, {@code generalfeedback[itemid]}</li>
 *   <li>{@code answer[N][itemid]}, {@code feedback[N][itemid]} for each answer slot N</li>
 *   <li>{@code correctfeedback[itemid]}, {@code partiallycorrectfeedback[itemid]}, {@code incorrectfeedback[itemid]}</li>
 *   <li>{@code hint[N][itemid]} for each hint slot N</li>
 *   <li>{@code noanswers}, {@code numhints}, {@code courseid}, {@code qtype}</li>
 *   <li>All {@code mform_isexpanded_id_*} hidden fields</li>
 *   <li>{@code _qf__qtype_multichoice_edit_form}</li>
 * </ul>
 */
@Component
public class MoodleQuestionEditParser {

    /**
     * Parses the question edit form HTML and returns all hidden fields as an
     * ordered map (preserving form field order for reproducibility).
     *
     * @param html    raw HTML of the question edit page
     * @param baseUrl base URL used by Jsoup to resolve relative links
     * @return map of field name â†’ value for every relevant hidden input
     */
    public Map<String, String> parseHiddenFields(String html, String baseUrl) {
        Document doc = Jsoup.parse(html, baseUrl);
        Map<String, String> fields = new LinkedHashMap<>();

        Elements hiddenInputs = doc.select("input[type=hidden]");
        for (Element input : hiddenInputs) {
            String name  = input.attr("name");
            String value = input.attr("value");
            if (name.isBlank()) continue;

            if (isRelevantHiddenField(name)) {
                fields.put(name, value);
            }
        }

        return fields;
    }

    /**
     * Convenience: returns how many answer slots the form currently has
     * (the value of the {@code noanswers} hidden field).
     */
    public int parseNoAnswers(Map<String, String> hiddenFields) {
        String v = hiddenFields.get("noanswers");
        return v != null ? parseIntLenient(v) : 0;
    }

    /**
     * Convenience: returns how many hint slots the form currently has
     * (the value of the {@code numhints} hidden field).
     */
    public int parseNumHints(Map<String, String> hiddenFields) {
        String v = hiddenFields.get("numhints");
        return v != null ? parseIntLenient(v) : 0;
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private boolean isRelevantHiddenField(String name) {
        return name.equals("sesskey")
                || name.equals("noanswers")
                || name.equals("numhints")
                || name.equals("courseid")
                || name.equals("qtype")
                || name.equals("_qf__qtype_multichoice_edit_form")
                || name.endsWith("[itemid]")
                || name.startsWith("mform_isexpanded_id_");
    }

    private int parseIntLenient(String s) {
        if (s == null || s.isBlank()) return 0;
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}

