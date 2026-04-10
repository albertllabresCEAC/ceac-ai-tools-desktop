package tools.ceac.ai.modules.campus.application.service;

import tools.ceac.ai.modules.campus.application.port.out.CampusGateway;
import tools.ceac.ai.modules.campus.domain.exception.AuthenticationRequiredException;
import tools.ceac.ai.modules.campus.infrastructure.campus.MoodleQuestionEditParser;
import tools.ceac.ai.modules.campus.infrastructure.config.CampusProperties;
import tools.ceac.ai.modules.campus.interfaces.api.dto.UpdateQuizMultichoiceQuestionRequest;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Updates a multichoice quiz question via a two-step scrape:
 * <ol>
 *   <li>GET the question edit form to collect hidden fields (sesskey, itemids, etc.)</li>
 *   <li>POST the merged params back to persist the changes.</li>
 * </ol>
 *
 * <p>A 303 redirect response from Moodle indicates success.
 */
@Service
public class UpdateQuizQuestionUseCase {

    private final CampusGateway campusGateway;
    private final CampusSessionService sessionService;
    private final MoodleQuestionEditParser questionEditParser;
    private final CampusProperties properties;

    public UpdateQuizQuestionUseCase(CampusGateway campusGateway,
                                     CampusSessionService sessionService,
                                     MoodleQuestionEditParser questionEditParser,
                                     CampusProperties properties) {
        this.campusGateway = campusGateway;
        this.sessionService = sessionService;
        this.questionEditParser = questionEditParser;
        this.properties = properties;
    }

    public void execute(String questionId, String cmid, UpdateQuizMultichoiceQuestionRequest req) {
        if (!sessionService.isAuthenticated()) {
            throw new AuthenticationRequiredException("not_authenticated");
        }
        try {
            // Step 1 â€” GET the form and parse hidden fields
            String formHtml = campusGateway.getQuestionEditForm(questionId, cmid).body();
            Map<String, String> hidden = questionEditParser.parseHiddenFields(formHtml, properties.baseUrl());

            int noanswers = questionEditParser.parseNoAnswers(hidden);
            int numhints  = questionEditParser.parseNumHints(hidden);

            // Step 2 â€” Build the POST body
            Map<String, String> params = buildParams(questionId, cmid, req, hidden, noanswers, numhints);
            campusGateway.postQuestionEdit(questionId, cmid, params);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("question_edit_failed", e);
        } catch (IOException e) {
            throw new IllegalStateException("question_edit_failed", e);
        }
    }

    // -------------------------------------------------------------------------

    private Map<String, String> buildParams(String questionId, String cmid,
                                             UpdateQuizMultichoiceQuestionRequest req,
                                             Map<String, String> hidden,
                                             int noanswers, int numhints) {
        Map<String, String> p = new LinkedHashMap<>();

        // Structural / hardcoded
        p.put("returnurl", "/mod/quiz/edit.php?cmid=" + cmid);
        p.put("inpopup",   "0");
        p.put("cmid",      cmid);
        p.put("id",        questionId);
        p.put("makecopy",  "0");
        p.put("appendqnumstring", "");
        p.put("mdlscrollto", "0");

        // Hidden fields from the GET form (sesskey, itemids, noanswers, numhints, courseid, qtype, mform_isexpanded_*, _qf__*)
        p.putAll(hidden);

        // Consumer-supplied fields
        p.put("name",        str(req.name()));
        p.put("status",      str(req.status()));
        p.put("defaultmark", str(req.defaultMark()));
        p.put("questiontext[text]",   str(req.questionText()));
        p.put("questiontext[format]", "1");
        p.put("generalfeedback[text]",   str(req.generalFeedback()));
        p.put("generalfeedback[format]", "1");
        p.put("single",     str(req.single()));
        // shuffleanswers uses a double-input pattern: hidden 0 + optional 1
        p.put("shuffleanswers", "0");
        if ("1".equals(req.shuffleAnswers())) {
            p.put("shuffleanswers", "1");
        }
        p.put("answernumbering",       str(req.answerNumbering()));
        p.put("showstandardinstruction", str(req.showStandardInstruction()));
        p.put("penalty",               str(req.penalty()));

        // Correct / incorrect feedback blocks
        p.put("correctfeedback[text]",            str(req.correctFeedback()));
        p.put("correctfeedback[format]",          "1");
        p.put("partiallycorrectfeedback[text]",   str(req.partiallyCorrectFeedback()));
        p.put("partiallycorrectfeedback[format]", "1");
        p.put("incorrectfeedback[text]",          str(req.incorrectFeedback()));
        p.put("incorrectfeedback[format]",        "1");

        // Answers â€” iterate up to noanswers (from form), pad with empties if consumer sent fewer
        List<UpdateQuizMultichoiceQuestionRequest.AnswerItem> answers =
                req.answers() != null ? req.answers() : List.of();
        for (int i = 0; i < noanswers; i++) {
            if (i < answers.size()) {
                UpdateQuizMultichoiceQuestionRequest.AnswerItem a = answers.get(i);
                p.put("answer[" + i + "][text]",   str(a.text()));
                p.put("fraction[" + i + "]",       str(a.fraction()));
                p.put("feedback[" + i + "][text]", str(a.feedback()));
            } else {
                p.put("answer[" + i + "][text]",   "");
                p.put("fraction[" + i + "]",       "0");
                p.put("feedback[" + i + "][text]", "");
            }
            p.put("answer[" + i + "][format]",   "0");
            p.put("feedback[" + i + "][format]", "0");
        }

        // Hints
        List<String> hints = req.hints() != null ? req.hints() : List.of();
        for (int i = 0; i < numhints; i++) {
            p.put("hint[" + i + "][text]",   i < hints.size() ? str(hints.get(i)) : "");
            p.put("hint[" + i + "][format]", "1");
        }

        // Tags (force multiselect submission)
        p.put("tags", "_qf__force_multiselect_submission");

        p.put("submitbutton", "Guardar cambios");

        return p;
    }

    private static String str(String v) {
        return v != null ? v : "";
    }
}

