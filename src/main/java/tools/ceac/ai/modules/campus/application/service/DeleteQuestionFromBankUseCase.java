package tools.ceac.ai.modules.campus.application.service;

import tools.ceac.ai.modules.campus.application.port.out.CampusGateway;
import tools.ceac.ai.modules.campus.domain.exception.AuthenticationRequiredException;
import tools.ceac.ai.modules.campus.domain.model.DeleteQuestionConfirmData;
import tools.ceac.ai.modules.campus.domain.model.QuestionBankData;
import tools.ceac.ai.modules.campus.infrastructure.campus.MoodleDeleteQuestionConfirmParser;
import tools.ceac.ai.modules.campus.infrastructure.campus.MoodleQuestionBankParser;
import tools.ceac.ai.modules.campus.infrastructure.config.CampusProperties;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class DeleteQuestionFromBankUseCase {

    private final CampusGateway campusGateway;
    private final CampusSessionService sessionService;
    private final MoodleQuestionBankParser bankParser;
    private final MoodleDeleteQuestionConfirmParser confirmParser;
    private final CampusProperties properties;

    public DeleteQuestionFromBankUseCase(CampusGateway campusGateway,
                                          CampusSessionService sessionService,
                                          MoodleQuestionBankParser bankParser,
                                          MoodleDeleteQuestionConfirmParser confirmParser,
                                          CampusProperties properties) {
        this.campusGateway   = campusGateway;
        this.sessionService  = sessionService;
        this.bankParser      = bankParser;
        this.confirmParser   = confirmParser;
        this.properties      = properties;
    }

    /**
     * Deletes one or more questions permanently from the Moodle question bank.
     *
     * <p>Flow:
     * <ol>
     *   <li>GET {@code /question/edit.php?cmid={cmid}} â†’ extract {@code sesskey}.</li>
     *   <li>POST {@code /question/bank/deletequestion/delete.php} with {@code q{id}=1} for each
     *       question â†’ Moodle returns a confirmation page containing a {@code confirm} token.</li>
     *   <li>POST {@code /question/bank/deletequestion/delete.php} with {@code confirm} token
     *       â†’ Moodle deletes and responds with HTTP 303.</li>
     * </ol>
     *
     * @param cmid        course-module ID of the quiz whose bank context is used
     * @param questionIds list of Moodle question IDs to delete
     */
    public void execute(String cmid, List<String> questionIds) {
        if (!sessionService.isAuthenticated()) {
            throw new AuthenticationRequiredException("not_authenticated");
        }
        try {
            // Step 1 â€” obtain sesskey from the question bank page
            String bankHtml = campusGateway.getQuestionBank(cmid).body();
            QuestionBankData bankData = bankParser.parse(bankHtml, properties.baseUrl());
            String sesskey = bankData.sesskey();

            // Step 2 â€” POST to trigger confirmation page and extract confirm token
            String confirmHtml = campusGateway
                    .postQuestionBankDeleteStep1(cmid, sesskey, questionIds)
                    .body();
            DeleteQuestionConfirmData confirmData = confirmParser.parse(confirmHtml, properties.baseUrl());

            // Step 3 â€” POST with confirm token to execute the actual deletion (Moodle responds 303)
            campusGateway.postQuestionBankDeleteStep2(
                    confirmData.cmid(),
                    confirmData.sesskey(),
                    confirmData.confirm(),
                    confirmData.deleteSelected(),
                    confirmData.returnUrl(),
                    confirmData.courseId()
            );

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("question_bank_delete_failed", e);
        } catch (IOException e) {
            throw new IllegalStateException("question_bank_delete_failed", e);
        }
    }
}

