package tools.ceac.ai.modules.campus.infrastructure.campus;

import tools.ceac.ai.modules.campus.domain.model.QuizEditMetadata;
import tools.ceac.ai.modules.campus.domain.model.QuizSlot;
import tools.ceac.ai.modules.campus.domain.model.QuizStructure;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses {@code /mod/quiz/edit.php?cmid=XXXX} and extracts the quiz slot structure.
 *
 * <p>DOM structure:
 * <pre>
 *   ul.slots
 *     li.section
 *       div.content
 *         ul.section.img-text          &lt;-- iteramos aquÃ­
 *           li.pagenumber#page-N       &lt;-- separador de pÃ¡gina
 *           li.activity.slot#slot-N   &lt;-- slot fijo o aleatorio
 * </pre>
 */
@Component
public class MoodleQuizEditParser {

    private static final Pattern SLOT_ID_PATTERN    = Pattern.compile("^slot-(\\d+)$");
    private static final Pattern QTYPE_PATTERN      = Pattern.compile("\\bqtype_(\\w+)\\b");
    private static final Pattern QUESTION_ID_PATTERN = Pattern.compile("[?&]id=(\\d+)");

    private static final Pattern MCFG_PATTERN     = Pattern.compile("M\\.cfg\\s*=\\s*(\\{[^;]+\\})\\s*;", Pattern.DOTALL);
    private static final Pattern SESSKEY_PATTERN  = Pattern.compile("\"sesskey\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern COURSEID_PATTERN = Pattern.compile("\"courseId\"\\s*:\\s*(\\d+)");
    private static final Pattern QUIZID_PATTERN   = Pattern.compile("[?&]quizid=(\\d+)");

    public QuizStructure parse(String html, String cmid, String baseUrl) {
        Document doc = Jsoup.parse(html, baseUrl);

        String totalQuestions = textOf(doc, "span.numberofquestions");
        String totalMarks     = textOf(doc, "span.mod_quiz_summarks");

        String maxGrade = "";
        Element maxGradeInput = doc.selectFirst("input#inputmaxgrade");
        if (maxGradeInput != null) maxGrade = maxGradeInput.attr("value");

        int total = parseIntLenient(totalQuestions);

        List<QuizSlot> slots = new ArrayList<>();

        // The slots live in ul.section.img-text (inside the section's div.content)
        Element slotsUl = doc.selectFirst("ul.section.img-text");
        if (slotsUl == null) {
            return new QuizStructure(cmid, total, totalMarks, maxGrade, slots);
        }

        int currentPage = 0;

        for (Element item : slotsUl.children()) {

            // Page separator: <li class="pagenumber ... page" id="page-N">
            if (item.hasClass("pagenumber")) {
                String pageId = item.id(); // "page-1", "page-2", ...
                if (pageId.startsWith("page-")) {
                    currentPage = parseIntLenient(pageId.substring(5));
                } else {
                    currentPage++;
                }
                continue;
            }

            // Slot: <li class="activity ... slot" id="slot-XXXXXX">
            if (!item.hasClass("slot")) continue;

            Matcher m = SLOT_ID_PATTERN.matcher(item.id());
            if (!m.matches()) continue;
            String slotId = m.group(1);

            // --- Slot number ---
            // <span class="slotnumber unshuffled"><span class="accesshide">Pregunta</span> 1</span>
            int slotNumber = 0;
            Element slotNumEl = item.selectFirst("span.slotnumber.unshuffled");
            if (slotNumEl != null) {
                slotNumber = parseIntLenient(slotNumEl.ownText().trim());
            }

            // --- Question type from CSS classes ---
            boolean isRandom = item.hasClass("random") || item.hasClass("qtype_random");
            String questionType;
            if (isRandom) {
                questionType = "random";
            } else {
                Matcher qt = QTYPE_PATTERN.matcher(item.className());
                questionType = qt.find() ? qt.group(1) : "unknown";
            }

            // --- Max mark ---
            // <span class="instancemaxmark decimalplaces_2" title="Nota mÃ¡xima">1,00</span>
            String maxMark = "";
            Element markEl = item.selectFirst("span.instancemaxmark");
            if (markEl != null) maxMark = markEl.ownText().trim();

            // --- Question name and text ---
            // <span class="questionname">...</span>
            // <span class="questiontext">...</span>
            String questionName = null;
            String questionText = null;
            Element nameEl = item.selectFirst("span.questionname");
            if (nameEl != null) questionName = nameEl.text().trim();
            Element textEl = item.selectFirst("span.questiontext");
            if (textEl != null) questionText = textEl.text().trim();

            // --- Bank category link (random slots only) ---
            String bankCategoryUrl = null;
            Element bankLink = item.selectFirst("a.mod_quiz_random_qbank_link");
            if (bankLink != null) bankCategoryUrl = bankLink.attr("abs:href");

            // --- Question ID in the bank (fixed slots only) ---
            // Extracted from the edit link: question/bank/editquestion/question.php?...&id=XXXXXX
            String questionId = null;
            Element editLink = item.selectFirst("a[href*=editquestion/question.php]");
            if (editLink != null) {
                Matcher qid = QUESTION_ID_PATTERN.matcher(editLink.attr("href"));
                if (qid.find()) questionId = qid.group(1);
            }

            slots.add(new QuizSlot(
                    slotId, slotNumber, currentPage,
                    questionType, questionName, questionText,
                    maxMark, bankCategoryUrl, questionId));
        }

        return new QuizStructure(cmid, total, totalMarks, maxGrade, slots);
    }

    /**
     * Extracts sesskey, courseId and quizId from the quiz edit page
     * ({@code /mod/quiz/edit.php?cmid=XXXX}).
     * <ul>
     *   <li>{@code sesskey} and {@code courseId} come from the inline {@code M.cfg} JS object.</li>
     *   <li>{@code quizId} is read from a hidden {@code <input name="quizid">} or, as fallback,
     *       from any link that contains {@code ?quizid=N} or {@code &quizid=N}.</li>
     * </ul>
     */
    public QuizEditMetadata parseEditMetadata(String html, String baseUrl) {
        Document doc = Jsoup.parse(html, baseUrl);

        String sesskey  = "";
        String courseId = "";

        for (Element script : doc.select("script")) {
            String src = script.html();
            Matcher m = MCFG_PATTERN.matcher(src);
            if (m.find()) {
                String cfg = m.group(1);
                sesskey  = extract(SESSKEY_PATTERN,  cfg);
                courseId = extract(COURSEID_PATTERN, cfg);
                break;
            }
        }

        // quizId: try hidden input first, then scan all link hrefs
        String quizId = "";
        Element quizIdInput = doc.selectFirst("input[name=quizid]");
        if (quizIdInput != null) {
            quizId = quizIdInput.attr("value");
        }
        if (quizId.isBlank()) {
            for (Element a : doc.select("a[href]")) {
                Matcher m = QUIZID_PATTERN.matcher(a.attr("href"));
                if (m.find()) { quizId = m.group(1); break; }
            }
        }

        return new QuizEditMetadata(sesskey, courseId, quizId);
    }

    private String extract(Pattern p, String text) {
        Matcher m = p.matcher(text);
        return m.find() ? m.group(1) : "";
    }

    private String textOf(Document doc, String cssSelector) {
        Element el = doc.selectFirst(cssSelector);
        return el != null ? el.text().trim() : "";
    }

    private int parseIntLenient(String s) {
        if (s == null || s.isBlank()) return 0;
        try {
            String digits = s.replaceAll("[^0-9]", "");
            return digits.isEmpty() ? 0 : Integer.parseInt(digits);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}

