package tools.ceac.ai.mcp.campus.interfaces.api;

import tools.ceac.ai.mcp.campus.application.service.CreateQuestionInBankUseCase;
import tools.ceac.ai.mcp.campus.application.service.DeleteQuestionFromBankUseCase;
import tools.ceac.ai.mcp.campus.application.service.GetQuizQuestionUseCase;
import tools.ceac.ai.mcp.campus.application.service.UpdateQuizQuestionUseCase;
import tools.ceac.ai.mcp.campus.interfaces.api.dto.QuizQuestionDetailResponse;
import tools.ceac.ai.mcp.campus.interfaces.api.dto.UpdateQuizMultichoiceQuestionRequest;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class QuestionController {

    private final GetQuizQuestionUseCase getQuizQuestionUseCase;
    private final UpdateQuizQuestionUseCase updateQuizQuestionUseCase;
    private final CreateQuestionInBankUseCase createQuestionInBankUseCase;
    private final DeleteQuestionFromBankUseCase deleteQuestionFromBankUseCase;

    public QuestionController(GetQuizQuestionUseCase getQuizQuestionUseCase,
                              UpdateQuizQuestionUseCase updateQuizQuestionUseCase,
                              CreateQuestionInBankUseCase createQuestionInBankUseCase,
                              DeleteQuestionFromBankUseCase deleteQuestionFromBankUseCase) {
        this.getQuizQuestionUseCase = getQuizQuestionUseCase;
        this.updateQuizQuestionUseCase = updateQuizQuestionUseCase;
        this.createQuestionInBankUseCase = createQuestionInBankUseCase;
        this.deleteQuestionFromBankUseCase = deleteQuestionFromBankUseCase;
    }

    @GetMapping("/questions/{questionId}")
    public QuizQuestionDetailResponse getQuestion(
            @Parameter(description = "ID de la pregunta Moodle", example = "3444787")
            @PathVariable String questionId,
            @Parameter(description = "ID del módulo de quiz Moodle (cmid)", example = "302206")
            @RequestParam String cmid) {
        var q = getQuizQuestionUseCase.execute(questionId, cmid);
        var answers = q.answers().stream()
                .map(a -> new QuizQuestionDetailResponse.AnswerItemResponse(a.text(), a.fraction(), a.feedback()))
                .toList();
        return new QuizQuestionDetailResponse(
                q.questionId(), q.cmid(), q.category(),
                q.name(), q.questionText(), q.status(), q.defaultMark(), q.idNumber(),
                q.single(), q.shuffleAnswers(), q.answerNumbering(), q.showStandardInstruction(),
                q.penalty(), q.generalFeedback(), q.correctFeedback(),
                q.partiallyCorrectFeedback(), q.incorrectFeedback(),
                answers, q.hints()
        );
    }

    @PutMapping("/questions/{questionId}/multichoice")
    public ResponseEntity<Void> updateMultichoiceQuestion(
            @Parameter(description = "ID de la pregunta Moodle", example = "3444787")
            @PathVariable String questionId,
            @Parameter(description = "ID del módulo de quiz Moodle (cmid)", example = "302206")
            @RequestParam String cmid,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(
                    schema = @Schema(implementation = UpdateQuizMultichoiceQuestionRequest.class),
                    examples = @ExampleObject(value = """
                            {
                              "name": "Pregunta de ejemplo",
                              "questionText": "<p>¿Cuál es la respuesta correcta?</p>",
                              "status": "ready",
                              "defaultMark": "1",
                              "single": "1",
                              "shuffleAnswers": "1",
                              "answerNumbering": "abc",
                              "showStandardInstruction": "0",
                              "penalty": "0.3333333",
                              "generalFeedback": "",
                              "correctFeedback": "Respuesta correcta.",
                              "partiallyCorrectFeedback": "Respuesta parcialmente correcta.",
                              "incorrectFeedback": "Respuesta incorrecta.",
                              "answers": [
                                { "text": "Opción A", "fraction": "1.0", "feedback": "" },
                                { "text": "Opción B", "fraction": "-0.5", "feedback": "" },
                                { "text": "Opción C", "fraction": "-0.5", "feedback": "" }
                              ],
                              "hints": []
                            }""")))
            @RequestBody UpdateQuizMultichoiceQuestionRequest request) {
        updateQuizQuestionUseCase.execute(questionId, cmid, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/questions/{questionId}/bank")
    public ResponseEntity<Void> deleteQuestionFromBank(
            @Parameter(description = "ID de la pregunta Moodle a eliminar del banco", example = "3444787")
            @PathVariable String questionId,
            @Parameter(description = "ID del módulo de quiz Moodle (cmid) que define el contexto del banco", example = "314207")
            @RequestParam String cmid) {
        deleteQuestionFromBankUseCase.execute(cmid, List.of(questionId));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/questions/bank/multichoice")
    public ResponseEntity<Void> createMultichoiceQuestionInBank(
            @Parameter(description = "ID del módulo de quiz Moodle (cmid) que define el contexto del banco", example = "314207")
            @RequestParam String cmid,
            @Parameter(description = "Valor de categoría obtenido de GET /api/quizzes/{cmid}/question-categories (campo categoryValue, formato 'categoryId,contextId')", example = "117056,379267")
            @RequestParam String category,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(
                    schema = @Schema(implementation = UpdateQuizMultichoiceQuestionRequest.class),
                    examples = @ExampleObject(value = """
                            {
                              "name": "Nueva pregunta de banco",
                              "questionText": "<p>¿Cuál es la respuesta correcta?</p>",
                              "status": "ready",
                              "defaultMark": "1",
                              "single": "1",
                              "shuffleAnswers": "1",
                              "answerNumbering": "abc",
                              "showStandardInstruction": "0",
                              "penalty": "0.3333333",
                              "generalFeedback": "",
                              "correctFeedback": "Respuesta correcta.",
                              "partiallyCorrectFeedback": "Respuesta parcialmente correcta.",
                              "incorrectFeedback": "Respuesta incorrecta.",
                              "answers": [
                                { "text": "<p>Opción A</p>", "fraction": "1.0",  "feedback": "<p>Correcto</p>" },
                                { "text": "<p>Opción B</p>", "fraction": "-0.5", "feedback": "<p>Incorrecto</p>" },
                                { "text": "<p>Opción C</p>", "fraction": "-0.5", "feedback": "<p>Incorrecto</p>" }
                              ],
                              "hints": []
                            }""")))
            @RequestBody UpdateQuizMultichoiceQuestionRequest request) {
        createQuestionInBankUseCase.execute(cmid, category, request);
        return ResponseEntity.noContent().build();
    }
}
