package tools.ceac.ai.mcp.campus.infrastructure.campus;

import tools.ceac.ai.mcp.campus.domain.model.QuizQuestionAnswer;
import tools.ceac.ai.mcp.campus.domain.model.QuizQuestionDetail;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Parses the Moodle question edit page
 * ({@code question/bank/editquestion/question.php?id=XXXX&cmid=XXXX})
 * and extracts the full question content for reading/display.
 *
 * <p>Moodle's Atto rich-text editor stores its content in a hidden
 * {@code <textarea>} that Jsoup can read directly.
 */
@Component
public class MoodleQuestionDetailParser {

    public QuizQuestionDetail parse(String html, String baseUrl, String questionId, String cmid) {
        Document doc = Jsoup.parse(html, baseUrl);

        String name        = inputVal(doc, "input[name=name]");
        String status      = selectedVal(doc, "select[name=status]");
        String defaultMark = inputVal(doc, "input[name=defaultmark]");
        String idNumber    = inputVal(doc, "input[name=idnumber]");
        String category    = selectedVal(doc, "select[name=category]");

        String questionText = textareaVal(doc, "textarea[name='questiontext[text]']");
        String generalFb    = textareaVal(doc, "textarea[name='generalfeedback[text]']");

        // single: select[name=single]
        String single = selectedVal(doc, "select[name=single]");

        // shuffleanswers: hidden value=0 + optional checkbox value=1
        // If the checkbox is checked the effective value is "1"
        Element shuffleCheckbox = doc.selectFirst("input[name=shuffleanswers][type=checkbox]");
        String shuffleAnswers = (shuffleCheckbox != null && shuffleCheckbox.hasAttr("checked")) ? "1" : "0";

        String answerNumbering       = selectedVal(doc, "select[name=answernumbering]");
        String showStdInstruction    = selectedVal(doc, "select[name=showstandardinstruction]");
        String penalty               = inputVal(doc, "input[name=penalty]");

        String correctFb          = textareaVal(doc, "textarea[name='correctfeedback[text]']");
        String partiallyCorrectFb = textareaVal(doc, "textarea[name='partiallycorrectfeedback[text]']");
        String incorrectFb        = textareaVal(doc, "textarea[name='incorrectfeedback[text]']");

        // Number of answer / hint slots from hidden inputs
        int noanswers = parseIntLenient(hiddenVal(doc, "noanswers"));
        int numhints  = parseIntLenient(hiddenVal(doc, "numhints"));

        // Answers
        List<QuizQuestionAnswer> answers = new ArrayList<>();
        for (int i = 0; i < noanswers; i++) {
            String text     = textareaVal(doc, "textarea[name='answer[" + i + "][text]']");
            String fraction = selectedVal(doc, "select[name='fraction[" + i + "]']");
            String feedback = textareaVal(doc, "textarea[name='feedback[" + i + "][text]']");
            // Only include slots that have actual content
            if (!text.isBlank() || !"0".equals(fraction)) {
                answers.add(new QuizQuestionAnswer(text, fraction, feedback));
            }
        }

        // Hints
        List<String> hints = new ArrayList<>();
        for (int i = 0; i < numhints; i++) {
            hints.add(textareaVal(doc, "textarea[name='hint[" + i + "][text]']"));
        }

        return new QuizQuestionDetail(
                questionId, cmid, category,
                name, questionText, status, defaultMark, idNumber,
                single, shuffleAnswers, answerNumbering, showStdInstruction,
                penalty, generalFb, correctFb, partiallyCorrectFb, incorrectFb,
                answers, hints
        );
    }

    // -------------------------------------------------------------------------

    private String inputVal(Document doc, String selector) {
        Element el = doc.selectFirst(selector);
        return el != null ? el.attr("value").strip() : "";
    }

    private String textareaVal(Document doc, String selector) {
        Element el = doc.selectFirst(selector);
        return el != null ? el.text() : "";
    }

    private String selectedVal(Document doc, String selector) {
        Element select = doc.selectFirst(selector);
        if (select == null) return "";
        Element selected = select.selectFirst("option[selected]");
        return selected != null ? selected.attr("value") : select.attr("value");
    }

    private String hiddenVal(Document doc, String name) {
        Element el = doc.selectFirst("input[type=hidden][name=" + name + "]");
        return el != null ? el.attr("value") : "0";
    }

    private int parseIntLenient(String s) {
        try { return Integer.parseInt(s.strip()); } catch (NumberFormatException e) { return 0; }
    }
}
