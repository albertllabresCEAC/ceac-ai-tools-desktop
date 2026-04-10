package tools.ceac.ai.modules.campus.application.service;

import tools.ceac.ai.modules.campus.application.port.out.CampusGateway;
import tools.ceac.ai.modules.campus.domain.exception.AuthenticationRequiredException;
import tools.ceac.ai.modules.campus.domain.model.QuestionBankData;
import tools.ceac.ai.modules.campus.infrastructure.campus.MoodleQuestionBankParser;
import tools.ceac.ai.modules.campus.infrastructure.campus.MoodleQuestionEditParser;
import tools.ceac.ai.modules.campus.infrastructure.config.CampusProperties;
import tools.ceac.ai.modules.campus.interfaces.api.dto.UpdateQuizMultichoiceQuestionRequest;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Creates a new multichoice question in the Moodle question bank via a two-step scrape:
 * <ol>
 *   <li>GET {@code question/edit.php?cmid=â€¦} â†’ parse courseId, sesskey, and available categories</li>
 *   <li>GET the blank question creation form â†’ collect fresh itemids</li>
 *   <li>POST the merged params to persist the new question.</li>
 * </ol>
 *
 * <p>The {@code categoryValue} parameter must come from
 * {@code GET /api/quizzes/{cmid}/question-categories} (field {@code categoryValue},
 * format: {@code "categoryId,contextId"}, e.g. {@code "117056,379267"}).
 */
@Service
public class CreateQuizMultichoiceQuestionUseCase {

    private final CampusGateway campusGateway;
    private final CampusSessionService sessionService;
    private final MoodleQuestionBankParser questionBankParser;
    private final MoodleQuestionEditParser questionEditParser;
    private final CampusProperties properties;

    public CreateQuizMultichoiceQuestionUseCase(CampusGateway campusGateway,
                                                CampusSessionService sessionService,
                                                MoodleQuestionBankParser questionBankParser,
                                                MoodleQuestionEditParser questionEditParser,
                                                CampusProperties properties) {
        this.campusGateway       = campusGateway;
        this.sessionService      = sessionService;
        this.questionBankParser  = questionBankParser;
        this.questionEditParser  = questionEditParser;
        this.properties          = properties;
    }

    /**
     * @param cmid          Quiz module ID
     * @param categoryValue Full category value from the categories endpoint
     *                      (e.g. {@code "117056,379267"})
     * @param req           Question content provided by the API consumer
     */
    public void execute(String cmid, String categoryValue,
                        UpdateQuizMultichoiceQuestionRequest req) {
        if (!sessionService.isAuthenticated()) {
            throw new AuthenticationRequiredException("not_authenticated");
        }
        try {
            String base = properties.baseUrl();

            // Step 1 â€” GET question bank page to extract courseId + sesskey
            String bankHtml = campusGateway.getQuestionBank(cmid).body();
            QuestionBankData bankData = questionBankParser.parse(bankHtml, base);

            // categoryId is the part before the comma (e.g. "117056" from "117056,379267")
            String categoryId = categoryValue.contains(",")
                    ? categoryValue.substring(0, categoryValue.indexOf(','))
                    : categoryValue;

            // Step 2 â€” GET blank question creation form to collect fresh itemids
            String formHtml = campusGateway.getNewQuestionForm(
                    cmid, bankData.courseId(), categoryId, bankData.sesskey()).body();
            Map<String, String> hidden = questionEditParser.parseHiddenFields(formHtml, base);

            int noanswers = questionEditParser.parseNoAnswers(hidden);
            int numhints  = questionEditParser.parseNumHints(hidden);

            // Step 3 â€” POST the new question
            Map<String, String> params = buildParams(
                    cmid, bankData.courseId(), categoryValue, req, hidden, noanswers, numhints);
            campusGateway.postQuestionEdit("", cmid, params);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("question_create_failed", e);
        } catch (IOException e) {
            throw new IllegalStateException("question_create_failed", e);
        }
    }

    // -------------------------------------------------------------------------

    private Map<String, String> buildParams(String cmid, String courseId,
                                             String categoryValue,
                                             UpdateQuizMultichoiceQuestionRequest req,
                                             Map<String, String> hidden,
                                             int noanswers, int numhints) {
        Map<String, String> p = new LinkedHashMap<>();

        // Structural / hardcoded
        p.put("returnurl", "/mod/quiz/edit.php?cmid=" + cmid + "&addonpage=0");
        p.put("inpopup",           "0");
        p.put("cmid",              cmid);
        p.put("courseid",          courseId);
        p.put("id",                "");          // empty = new question
        p.put("makecopy",          "0");
        p.put("appendqnumstring",  "addquestion");
        p.put("mdlscrollto",       "0");
        p.put("qtype",             "multichoice");
        p.put("idnumber",          "");

        // Hidden fields from the blank form (sesskey, itemids, noanswers, numhints, mform_isexpanded_*, _qf__*)
        p.putAll(hidden);

        // category (full "categoryId,contextId" value)
        p.put("category", categoryValue);

        // Consumer-supplied fields
        p.put("name",        str(req.name()));
        p.put("status",      str(req.status()));
        p.put("defaultmark", str(req.defaultMark()));
        p.put("questiontext[text]",   str(req.questionText()));
        p.put("questiontext[format]", "1");
        p.put("generalfeedback[text]",   str(req.generalFeedback()));
        p.put("generalfeedback[format]", "1");
        p.put("single",     str(req.single()));
        p.put("shuffleanswers", "0");
        if ("1".equals(req.shuffleAnswers())) {
            p.put("shuffleanswers", "1");
        }
        p.put("answernumbering",         str(req.answerNumbering()));
        p.put("showstandardinstruction", str(req.showStandardInstruction()));
        p.put("penalty",                 str(req.penalty()));

        p.put("correctfeedback[text]",            str(req.correctFeedback()));
        p.put("correctfeedback[format]",          "1");
        p.put("partiallycorrectfeedback[text]",   str(req.partiallyCorrectFeedback()));
        p.put("partiallycorrectfeedback[format]", "1");
        p.put("incorrectfeedback[text]",          str(req.incorrectFeedback()));
        p.put("incorrectfeedback[format]",        "1");

        // Answers â€” format=1 (HTML) for new questions
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
                p.put("fraction[" + i + "]",       "0.0");
                p.put("feedback[" + i + "][text]", "");
            }
            p.put("answer[" + i + "][format]",   "1");
            p.put("feedback[" + i + "][format]", "1");
        }

        // Hints
        List<String> hints = req.hints() != null ? req.hints() : List.of();
        for (int i = 0; i < numhints; i++) {
            p.put("hint[" + i + "][text]",   i < hints.size() ? str(hints.get(i)) : "");
            p.put("hint[" + i + "][format]", "1");
        }

        p.put("tags",         "_qf__force_multiselect_submission");
        p.put("submitbutton", "Guardar cambios");

        return p;
    }

    private static String str(String v) {
        return v != null ? v : "";
    }
}


