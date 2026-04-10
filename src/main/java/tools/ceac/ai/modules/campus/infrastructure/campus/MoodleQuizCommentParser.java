package tools.ceac.ai.modules.campus.infrastructure.campus;

import tools.ceac.ai.modules.campus.domain.model.QuizAttemptAnswerOption;
import tools.ceac.ai.modules.campus.domain.model.QuizCommentData;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Parses the Moodle quiz manual grading page (/mod/quiz/comment.php?attempt=N&slot=S).
 */
@Component
public class MoodleQuizCommentParser {

    public QuizCommentData parse(String html, String attemptId, String slot, String baseUrl) {
        Document doc = Jsoup.parse(html, baseUrl);

        // â”€â”€ summary table â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        String userId = "";
        String fullName = "";
        String cuestionario = "";

        Element summary = doc.selectFirst("table.quizreviewsummary");
        if (summary != null) {
            for (Element row : summary.select("tr")) {
                Element th = row.selectFirst("th");
                Element td = row.selectFirst("td");

                if (th != null && td == null) {
                    // First row: only a th with the user avatar + link in td
                    continue;
                }
                if (th != null && td != null) {
                    // Check if th contains user link (no label text)
                    Element userLink = row.selectFirst("a[href*='user/view.php']");
                    if (userLink != null && fullName.isBlank()) {
                        fullName = userLink.text().trim();
                        userId = extractParam(userLink.attr("href"), "id");
                        continue;
                    }
                    String label = th.text().trim().toLowerCase();
                    String value = td.text().trim();
                    if (label.contains("cuestionario") || label.contains("quiz")) {
                        cuestionario = value;
                    }
                    // "Pregunta" row is the question text but we get it from div.que
                }
                // Row where th has avatar link and td has the name link
                if (td != null) {
                    Element nameLink = td.selectFirst("a[href*='user/view.php']");
                    if (nameLink != null && fullName.isBlank()) {
                        fullName = nameLink.text().trim();
                        userId = extractParam(nameLink.attr("href"), "id");
                    }
                }
            }
        }

        // â”€â”€ question block â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        String enunciado = "";
        String estado = "";
        String puntuacion = "";
        String respuestaCorrecta = "";
        String comentario = "";
        String mark = "";
        String maxMark = "";
        String minFraction = "";
        String maxFraction = "";
        String sequencecheck = "";
        String itemid = "";
        String commentFormat = "1";
        List<QuizAttemptAnswerOption> opciones = new ArrayList<>();

        Element que = doc.selectFirst("div.que");
        if (que != null) {
            // estado
            if (que.hasClass("correct")) estado = "correct";
            else if (que.hasClass("incorrect")) estado = "incorrect";
            else if (que.hasClass("notanswered")) estado = "notanswered";
            else if (que.hasClass("partiallycorrect")) estado = "partiallycorrect";

            Element grade = que.selectFirst("div.grade");
            if (grade != null) puntuacion = grade.text().trim();

            Element qtext = que.selectFirst("div.qtext");
            if (qtext != null) enunciado = qtext.text().trim();

            Element rightAnswer = que.selectFirst("div.rightanswer");
            if (rightAnswer != null) respuestaCorrecta = rightAnswer.text().trim();

            // answer options
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
                    if (opt.hasClass("correct")) optEstado = "correct";
                    else if (opt.hasClass("incorrect")) optEstado = "incorrect";

                    if (!letra.isBlank() || !texto.isBlank()) {
                        opciones.add(new QuizAttemptAnswerOption(letra, texto, seleccionada, optEstado));
                    }
                }
            }

            // comment textarea (name ends with "-comment", but not "-comment:itemid")
            Element textarea = que.selectFirst("textarea[name$='-comment']");
            if (textarea != null) comentario = textarea.text().trim();

            // hidden form fields within the question
            Element markInput = que.selectFirst("input[name$='-mark']");
            if (markInput != null) mark = markInput.attr("value");

            Element maxMarkInput = que.selectFirst("input[name$='-maxmark']");
            if (maxMarkInput != null) maxMark = maxMarkInput.attr("value");

            Element seqInput = que.selectFirst("input[name$=':sequencecheck']");
            if (seqInput != null) sequencecheck = seqInput.attr("value");

            Element itemidInput = que.selectFirst("input[name$='-comment:itemid']");
            if (itemidInput != null) itemid = itemidInput.attr("value");

            Element formatInput = que.selectFirst("input[name$='-commentformat']");
            if (formatInput != null) commentFormat = formatInput.attr("value");

            Element minInput = que.selectFirst("input[name$=':minfraction']");
            if (minInput != null) minFraction = minInput.attr("value");

            Element maxInput = que.selectFirst("input[name$=':maxfraction']");
            if (maxInput != null) maxFraction = maxInput.attr("value");
        }

        // â”€â”€ form-level hidden fields â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        String sesskey = "";
        Element sesskeyInput = doc.selectFirst("input[name='sesskey']");
        if (sesskeyInput != null) sesskey = sesskeyInput.attr("value");

        // â”€â”€ extract usageId from field name: q{usageId}:{slot}_:sequencecheck â”€
        String usageId = "";
        Element seqInputForName = doc.selectFirst("input[name$=':sequencecheck']");
        if (seqInputForName != null) {
            String fieldName = seqInputForName.attr("name");
            // fieldName = "q1408382:14_:sequencecheck"
            int colon = fieldName.indexOf(':');
            if (colon > 1) usageId = fieldName.substring(1, colon); // skip leading 'q'
        }

        return new QuizCommentData(
                attemptId, slot, usageId, userId, fullName, cuestionario, enunciado,
                estado, puntuacion, mark, maxMark, minFraction, maxFraction,
                comentario, sesskey, sequencecheck, itemid, commentFormat,
                respuestaCorrecta, opciones
        );
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

