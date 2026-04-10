package tools.ceac.ai.mcp.campus.infrastructure.campus;

import tools.ceac.ai.mcp.campus.domain.model.QuizConfig;
import tools.ceac.ai.mcp.campus.domain.model.QuizReviewPhase;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

/**
 * Parses the Moodle quiz module-edit page ({@code /course/modedit.php?update=N&return=1})
 * and extracts quiz configuration settings from the form fields.
 */
@Component
public class MoodleQuizModEditParser {

    public QuizConfig parse(String html, String baseUrl) {
        Document doc = Jsoup.parse(html, baseUrl);

        String cmid      = inputValue(doc, "coursemodule");
        String courseId  = inputValue(doc, "course");
        String name      = inputValue(doc, "name");
        boolean visible  = "1".equals(selectValue(doc, "visible"));

        // --- Timing ---
        boolean timeopenEnabled  = isChecked(doc, "timeopen[enabled]");
        boolean timecloseEnabled = isChecked(doc, "timeclose[enabled]");
        boolean timelimitEnabled = isChecked(doc, "timelimit[enabled]");

        String timeopen  = timeopenEnabled  ? buildDatetime(doc, "timeopen")  : null;
        String timeclose = timecloseEnabled ? buildDatetime(doc, "timeclose") : null;

        Long timelimitSeconds = null;
        if (timelimitEnabled) {
            String numStr  = inputValue(doc, "timelimit[number]");
            String unitStr = selectValue(doc, "timelimit[timeunit]");
            long num  = numStr.isEmpty()  ? 0L : Long.parseLong(numStr);
            long unit = unitStr.isEmpty() ? 1L : Long.parseLong(unitStr);
            timelimitSeconds = num * unit;
        }

        String overduehandling = selectValue(doc, "overduehandling");

        // --- Grade ---
        String gradepass  = inputValue(doc, "gradepass");
        int attempts      = parseIntOrZero(selectValue(doc, "attempts"));
        int grademethod   = parseIntOrZero(selectValue(doc, "grademethod"));

        // --- Layout ---
        int questionsperpage = parseIntOrZero(selectValue(doc, "questionsperpage"));
        String navmethod     = selectValue(doc, "navmethod");

        // --- Question behaviour ---
        boolean shuffleanswers  = "1".equals(selectValue(doc, "shuffleanswers"));
        String preferredbehaviour = selectValue(doc, "preferredbehaviour");

        // --- Review options ---
        QuizReviewPhase reviewDuring    = parsePhase(doc, "during");
        QuizReviewPhase reviewImmediate = parsePhase(doc, "immediately");
        QuizReviewPhase reviewOpen      = parsePhase(doc, "open");
        QuizReviewPhase reviewClosed    = parsePhase(doc, "closed");

        // --- Display ---
        int decimalpoints         = parseIntOrZero(selectValue(doc, "decimalpoints"));
        int questiondecimalpoints = parseIntOrZero(selectValue(doc, "questiondecimalpoints"));

        // --- Restrictions ---
        String quizpassword = inputValue(doc, "quizpassword");
        String browsersecurity = selectValue(doc, "browsersecurity");
        boolean requireSafeExamBrowser = "1".equals(selectValue(doc, "seb_requiresafeexambrowser"));

        return new QuizConfig(
                cmid, courseId, name, visible,
                timeopen, timeclose, timelimitSeconds, overduehandling,
                gradepass, attempts, grademethod,
                questionsperpage, navmethod,
                shuffleanswers, preferredbehaviour,
                reviewDuring, reviewImmediate, reviewOpen, reviewClosed,
                decimalpoints, questiondecimalpoints,
                quizpassword, browsersecurity, requireSafeExamBrowser
        );
    }

    // -------------------------------------------------------------------------

    private QuizReviewPhase parsePhase(Document doc, String phase) {
        return new QuizReviewPhase(
                isChecked(doc, "reviewattempt["       + phase + "]"),
                isChecked(doc, "reviewcorrectness["   + phase + "]"),
                isChecked(doc, "reviewmarks["         + phase + "]"),
                isChecked(doc, "reviewspecificfeedback[" + phase + "]"),
                isChecked(doc, "reviewgeneralfeedback["  + phase + "]"),
                isChecked(doc, "reviewrightanswer["   + phase + "]"),
                isChecked(doc, "reviewoverallfeedback[" + phase + "]")
        );
    }

    /**
     * Builds an ISO local datetime string from the five date-part selects
     * (e.g. {@code "timeopen[day]"}, {@code "timeopen[month]"}, ...).
     */
    private String buildDatetime(Document doc, String prefix) {
        String day    = selectValue(doc, prefix + "[day]");
        String month  = selectValue(doc, prefix + "[month]");
        String year   = selectValue(doc, prefix + "[year]");
        String hour   = selectValue(doc, prefix + "[hour]");
        String minute = selectValue(doc, prefix + "[minute]");
        if (year.isEmpty() || month.isEmpty() || day.isEmpty()) return null;
        return String.format("%s-%02d-%02dT%02d:%02d",
                year,
                Integer.parseInt(month),
                Integer.parseInt(day),
                hour.isEmpty()   ? 0 : Integer.parseInt(hour),
                minute.isEmpty() ? 0 : Integer.parseInt(minute));
    }

    /** Returns the {@code value} attribute of the currently selected {@code <option>}. */
    private String selectValue(Document doc, String name) {
        Element opt = doc.selectFirst("select[name=\"" + name + "\"] option[selected]");
        return opt != null ? opt.attr("value") : "";
    }

    /** Returns the {@code value} attribute of an {@code <input>} element. */
    private String inputValue(Document doc, String name) {
        Element el = doc.selectFirst("input[name=\"" + name + "\"]");
        return el != null ? el.attr("value") : "";
    }

    /** Returns true if a checkbox input is present and has the {@code checked} attribute. */
    private boolean isChecked(Document doc, String name) {
        Element el = doc.selectFirst("input[type=checkbox][name=\"" + name + "\"]");
        return el != null && el.hasAttr("checked");
    }

    private int parseIntOrZero(String s) {
        try {
            return s.isEmpty() ? 0 : Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
