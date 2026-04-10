package tools.ceac.ai.mcp.campus.infrastructure.campus;

import tools.ceac.ai.mcp.campus.domain.model.QuizAttempt;
import tools.ceac.ai.mcp.campus.domain.model.QuizQuestionResult;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Parses the Moodle quiz overview report HTML (/mod/quiz/report.php?mode=overview).
 */
@Component
public class MoodleQuizReportParser {

    // Fixed column indices (0-based) in the attempts table
    private static final int COL_FULLNAME    = 2;
    private static final int COL_EMAIL       = 3;
    private static final int COL_ESTADO      = 4;
    private static final int COL_COMENZADO   = 5;
    private static final int COL_FINALIZADO  = 6;
    private static final int COL_TIEMPO      = 7;
    private static final int COL_CALIFICACION = 8;
    private static final int FIRST_QUESTION_COL = 9;

    public List<QuizAttempt> parse(String html, String baseUrl) {
        Document doc = Jsoup.parse(html, baseUrl);
        Element table = doc.selectFirst("table#attempts");
        if (table == null) {
            return List.of();
        }

        List<String> questionSlots = parseQuestionSlots(table);
        List<QuizAttempt> result = new ArrayList<>();

        for (Element row : table.select("tbody tr")) {
            QuizAttempt attempt = parseRow(row, questionSlots, baseUrl);
            if (attempt != null) {
                result.add(attempt);
            }
        }
        return result;
    }

    private static final java.util.regex.Pattern FIRST_NUMBER = java.util.regex.Pattern.compile("\\d+");

    private List<String> parseQuestionSlots(Element table) {
        List<String> slots = new ArrayList<>();
        Elements headers = table.select("thead tr:first-child th");
        for (int i = FIRST_QUESTION_COL; i < headers.size(); i++) {
            String slotText = headers.get(i).text().trim();
            java.util.regex.Matcher m = FIRST_NUMBER.matcher(slotText);
            slots.add(m.find() ? m.group() : "q" + (i - FIRST_QUESTION_COL + 1));
        }
        return slots;
    }

    private QuizAttempt parseRow(Element row, List<String> questionSlots, String baseUrl) {
        // Skip summary/average rows (they have no user link in c2)
        Element cells[] = row.select("td").toArray(new Element[0]);
        if (cells.length < COL_CALIFICACION + 1) {
            return null;
        }

        // userId from profile link in fullName cell
        String userId = "";
        Element nameLink = cells[COL_FULLNAME].selectFirst("a[href*='user/view.php']");
        if (nameLink != null) {
            String href = nameLink.attr("href");
            userId = extractParam(href, "id");
        }
        if (userId.isBlank()) {
            return null; // not a student row
        }

        // attemptId from checkbox
        String attemptId = "";
        Element checkbox = row.selectFirst("input[name='attemptid[]']");
        if (checkbox != null) {
            attemptId = checkbox.val();
        }

        String fullName = nameLink != null ? nameLink.text().trim() : cells[COL_FULLNAME].text().trim();
        String email = cells[COL_EMAIL].text().trim();
        String estado = cells[COL_ESTADO].text().trim();
        String comenzadoEl = cells[COL_COMENZADO].text().trim();
        String finalizado = cells[COL_FINALIZADO].text().trim();
        String tiempoRequerido = cells[COL_TIEMPO].text().trim();
        String calificacion = cells[COL_CALIFICACION].text().trim();

        // Review URL for the whole attempt
        String reviewUrl = "";
        Element reviewLink = row.selectFirst("a.reviewlink");
        if (reviewLink != null) {
            reviewUrl = reviewLink.absUrl("href");
        }

        // Per-question columns
        List<QuizQuestionResult> questions = new ArrayList<>();
        for (int i = 0; i < questionSlots.size(); i++) {
            int colIndex = FIRST_QUESTION_COL + i;
            if (colIndex >= cells.length) break;
            Element cell = cells[colIndex];
            questions.add(parseQuestionCell(questionSlots.get(i), cell));
        }

        return new QuizAttempt(userId, attemptId, fullName, email, estado,
                comenzadoEl, finalizado, tiempoRequerido, calificacion, reviewUrl, questions);
    }

    private QuizQuestionResult parseQuestionCell(String slotFallback, Element cell) {
        String valor = cell.text().trim();
        String estado = "";
        if (cell.selectFirst("span.correct") != null || cell.selectFirst("i.fa-check") != null) {
            estado = "correct";
        } else if (cell.selectFirst("span.incorrect") != null || cell.selectFirst("i.fa-remove") != null) {
            estado = "incorrect";
        } else if (cell.selectFirst("span.notanswered") != null) {
            estado = "notanswered";
        }
        String reviewUrl = "";
        String slot = slotFallback;
        Element link = cell.selectFirst("a[href]");
        if (link != null) {
            reviewUrl = link.absUrl("href");
            String slotFromUrl = extractParam(reviewUrl, "slot");
            if (!slotFromUrl.isBlank()) {
                slot = slotFromUrl;
            }
        }
        return new QuizQuestionResult(slot, valor, estado, reviewUrl);
    }

    private String extractParam(String url, String param) {
        String prefix = param + "=";
        int start = url.indexOf(prefix);
        if (start == -1) return "";
        start += prefix.length();
        int end = url.indexOf('&', start);
        return end == -1 ? url.substring(start) : url.substring(start, end);
    }
}
