package tools.ceac.ai.modules.campus.infrastructure.campus;

import tools.ceac.ai.modules.campus.domain.model.QuizAttemptAnswerOption;
import tools.ceac.ai.modules.campus.domain.model.QuizAttemptQuestion;
import tools.ceac.ai.modules.campus.domain.model.QuizAttemptReview;
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
 * Parses the Moodle quiz attempt review page (/mod/quiz/review.php?attempt=N).
 */
@Component
public class MoodleQuizAttemptReviewParser {

    private static final Pattern LAST_NUMBER = Pattern.compile("(\\d+)$");

    public QuizAttemptReview parse(String html, String attemptId, String baseUrl) {
        Document doc = Jsoup.parse(html, baseUrl);

        String userId = "";
        String fullName = "";
        String comenzadoEl = "";
        String estado = "";
        String finalizadoEn = "";
        String tiempoEmpleado = "";
        String puntos = "";
        String calificacion = "";

        Element summary = doc.selectFirst("table.quizreviewsummary");
        if (summary != null) {
            for (Element row : summary.select("tr")) {
                Element th = row.selectFirst("th");
                Element td = row.selectFirst("td");

                // First row: student name/link (th contains a link, no td label pattern)
                if (th != null && td == null) {
                    Element link = row.selectFirst("a[href*='user/view.php']");
                    if (link != null) {
                        fullName = link.text().trim();
                        userId = extractParam(link.attr("href"), "id");
                    }
                    continue;
                }

                if (th == null || td == null) continue;

                String label = th.text().trim().toLowerCase();
                String value = td.text().trim();

                if (label.contains("comenzado") || label.contains("started")) {
                    comenzadoEl = value;
                } else if (label.contains("estado") || label.contains("state")) {
                    estado = value;
                } else if (label.contains("finalizado") || label.contains("completed") || label.contains("finished")) {
                    finalizadoEn = value;
                } else if (label.contains("tiempo") || label.contains("time")) {
                    tiempoEmpleado = value;
                } else if (label.contains("puntos") || label.contains("marks")) {
                    puntos = value;
                } else if (label.contains("calificaci") || label.contains("grade")) {
                    calificacion = value;
                }
            }
        }

        List<QuizAttemptQuestion> questions = new ArrayList<>();
        for (Element que : doc.select("div.que")) {
            QuizAttemptQuestion q = parseQuestion(que);
            if (q != null) {
                questions.add(q);
            }
        }

        return new QuizAttemptReview(attemptId, userId, fullName, comenzadoEl, estado,
                finalizadoEn, tiempoEmpleado, puntos, calificacion, questions);
    }

    private QuizAttemptQuestion parseQuestion(Element que) {
        // slot from id="question-{attemptId}-{slot}"
        String id = que.id();
        String slot = "";
        Matcher m = LAST_NUMBER.matcher(id);
        if (m.find()) {
            slot = m.group(1);
        }
        if (slot.isBlank()) return null;

        String numero = "";
        Element qno = que.selectFirst("span.rui-qno");
        if (qno == null) qno = que.selectFirst(".info .no");
        if (qno != null) numero = qno.text().trim();

        // estado from div classes: correct, incorrect, notanswered, etc.
        String estado = "";
        if (que.hasClass("correct")) {
            estado = "correct";
        } else if (que.hasClass("incorrect")) {
            estado = "incorrect";
        } else if (que.hasClass("notanswered")) {
            estado = "notanswered";
        } else if (que.hasClass("partiallycorrect")) {
            estado = "partiallycorrect";
        }

        String puntuacion = "";
        Element grade = que.selectFirst("div.grade");
        if (grade != null) puntuacion = grade.text().trim();

        String enunciado = "";
        Element qtext = que.selectFirst("div.qtext");
        if (qtext != null) enunciado = qtext.text().trim();

        String respuestaCorrecta = "";
        Element rightAnswer = que.selectFirst("div.rightanswer");
        if (rightAnswer != null) respuestaCorrecta = rightAnswer.text().trim();

        List<QuizAttemptAnswerOption> opciones = new ArrayList<>();
        Element answerDiv = que.selectFirst("div.answer");
        if (answerDiv != null) {
            for (Element opt : answerDiv.children()) {
                String letra = "";
                Element answerNumber = opt.selectFirst("span.answernumber");
                if (answerNumber != null) letra = answerNumber.text().trim();

                String texto = "";
                Element textDiv = opt.selectFirst("div.flex-fill");
                if (textDiv != null) texto = textDiv.text().trim();

                boolean seleccionada = opt.selectFirst("input[checked]") != null;

                String optEstado = "";
                if (opt.hasClass("correct")) {
                    optEstado = "correct";
                } else if (opt.hasClass("incorrect")) {
                    optEstado = "incorrect";
                }

                if (!letra.isBlank() || !texto.isBlank()) {
                    opciones.add(new QuizAttemptAnswerOption(letra, texto, seleccionada, optEstado));
                }
            }
        }

        return new QuizAttemptQuestion(slot, numero, estado, puntuacion, enunciado, respuestaCorrecta, opciones);
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


