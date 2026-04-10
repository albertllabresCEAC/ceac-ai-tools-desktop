package tools.ceac.ai.modules.campus.infrastructure.campus;

import tools.ceac.ai.modules.campus.domain.model.QuestionBankData;
import tools.ceac.ai.modules.campus.domain.model.QuizQuestionCategory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses {@code question/edit.php?cmid=XXXX} and extracts:
 * <ul>
 *   <li>The available question bank categories (with their full {@code categoryId,contextId} value)</li>
 *   <li>{@code courseId} and {@code sesskey} needed to fetch the blank question creation form</li>
 * </ul>
 *
 * <p>Categories live in:
 * <pre>
 *   div[data-filterregion="filtertypedata"]
 *     select[data-field-name="category"]
 *       option[value=""]  disabled  â† section group header
 *       option[value="N"]           â† selectable category
 * </pre>
 *
 * <p>Context IDs come from the inline {@code M.cfg} JS object:
 * <pre>
 *   M.cfg = { "courseId": 8824, "courseContextId": 379267, "contextid": 379290, "sesskey": "abc", ... }
 * </pre>
 * Categories whose group starts with {@code "Cuestionario:"} use the quiz contextId ({@code contextid});
 * all others (course-level) use {@code courseContextId}.
 */
@Component
public class MoodleQuestionBankParser {

    private static final Pattern MCFG_PATTERN =
            Pattern.compile("M\\.cfg\\s*=\\s*(\\{[^;]+\\})\\s*;", Pattern.DOTALL);

    private static final Pattern COURSEID_PATTERN    = Pattern.compile("\"courseId\"\\s*:\\s*(\\d+)");
    private static final Pattern COURSE_CTX_PATTERN  = Pattern.compile("\"courseContextId\"\\s*:\\s*(\\d+)");
    private static final Pattern QUIZ_CTX_PATTERN    = Pattern.compile("\"contextid\"\\s*:\\s*(\\d+)");
    private static final Pattern SESSKEY_PATTERN     = Pattern.compile("\"sesskey\"\\s*:\\s*\"([^\"]+)\"");

    public QuestionBankData parse(String html, String baseUrl) {
        Document doc = Jsoup.parse(html, baseUrl);

        // --- Extract M.cfg metadata ---
        String courseId      = "";
        String courseCtxId   = "";
        String quizCtxId     = "";
        String sesskey       = "";

        for (Element script : doc.select("script")) {
            String src = script.html();
            Matcher m = MCFG_PATTERN.matcher(src);
            if (m.find()) {
                String cfg = m.group(1);
                courseId    = extractGroup1(COURSEID_PATTERN,   cfg);
                courseCtxId = extractGroup1(COURSE_CTX_PATTERN, cfg);
                quizCtxId   = extractGroup1(QUIZ_CTX_PATTERN,   cfg);
                sesskey     = extractGroup1(SESSKEY_PATTERN,     cfg);
                break;
            }
        }

        // --- Parse categories ---
        List<QuizQuestionCategory> categories = new ArrayList<>();

        Element select = doc.selectFirst(
                "div[data-filterregion=filtertypedata] select[data-field-name=category]");

        if (select != null) {
            String currentGroup  = "";
            String currentCtxId  = courseCtxId;  // default to course context

            Elements options = select.select("option");
            for (Element option : options) {
                String value = option.attr("value");
                String text  = cleanText(option.text());

                if (value.isEmpty()) {
                    currentGroup = text;
                    // Quiz-level categories use the quiz's own contextId
                    currentCtxId = currentGroup.startsWith("Cuestionario:") ? quizCtxId : courseCtxId;
                } else {
                    String categoryValue = value + "," + currentCtxId;
                    categories.add(new QuizQuestionCategory(value, text, currentGroup, categoryValue));
                }
            }
        }

        return new QuestionBankData(courseId, sesskey, categories);
    }

    // -------------------------------------------------------------------------

    private String extractGroup1(Pattern p, String text) {
        Matcher m = p.matcher(text);
        return m.find() ? m.group(1) : "";
    }

    /** Strips non-breaking spaces (U+00A0) and surrounding whitespace. */
    private String cleanText(String raw) {
        return raw.replace('\u00A0', ' ').strip();
    }
}


