package tools.ceac.ai.modules.campus.interfaces.api;

import tools.ceac.ai.modules.campus.application.service.GetQuizAttemptReviewUseCase;
import tools.ceac.ai.modules.campus.application.service.GetQuizCommentUseCase;
import tools.ceac.ai.modules.campus.application.service.GetQuizConfigUseCase;
import tools.ceac.ai.modules.campus.application.service.GetQuizResultsUseCase;
import tools.ceac.ai.modules.campus.application.service.GetQuizStructureUseCase;
import tools.ceac.ai.modules.campus.application.service.DeleteQuizUserOverrideUseCase;
import tools.ceac.ai.modules.campus.application.service.GetQuizUserOverridesUseCase;
import tools.ceac.ai.modules.campus.application.service.SaveQuizCommentUseCase;
import tools.ceac.ai.modules.campus.application.service.SaveQuizUserOverrideUseCase;
import tools.ceac.ai.modules.campus.application.service.CreateQuizMultichoiceQuestionUseCase;
import tools.ceac.ai.modules.campus.application.service.DeleteQuizSlotUseCase;
import tools.ceac.ai.modules.campus.application.service.GetQuizQuestionCategoriesUseCase;
import tools.ceac.ai.modules.campus.domain.model.QuizAttempt;
import tools.ceac.ai.modules.campus.domain.model.QuizAttemptReview;
import tools.ceac.ai.modules.campus.domain.model.QuizCommentData;
import tools.ceac.ai.modules.campus.domain.model.QuizConfig;
import tools.ceac.ai.modules.campus.domain.model.QuizStructure;
import tools.ceac.ai.modules.campus.domain.model.QuizUserOverride;
import tools.ceac.ai.modules.campus.interfaces.api.dto.QuizAttemptAnswerOptionResponse;
import tools.ceac.ai.modules.campus.interfaces.api.dto.QuizAttemptQuestionResponse;
import tools.ceac.ai.modules.campus.interfaces.api.dto.QuizAttemptResponse;
import tools.ceac.ai.modules.campus.interfaces.api.dto.QuizAttemptReviewResponse;
import tools.ceac.ai.modules.campus.interfaces.api.dto.QuizCommentResponse;
import tools.ceac.ai.modules.campus.interfaces.api.dto.QuizConfigResponse;
import tools.ceac.ai.modules.campus.interfaces.api.dto.QuizOverrideSettingResponse;
import tools.ceac.ai.modules.campus.interfaces.api.dto.QuizQuestionCategoryResponse;
import tools.ceac.ai.modules.campus.interfaces.api.dto.QuizQuestionResultResponse;
import tools.ceac.ai.modules.campus.interfaces.api.dto.QuizReviewPhaseResponse;
import tools.ceac.ai.modules.campus.interfaces.api.dto.QuizSlotResponse;
import tools.ceac.ai.modules.campus.interfaces.api.dto.QuizStructureResponse;
import tools.ceac.ai.modules.campus.interfaces.api.dto.QuizUserOverrideResponse;
import tools.ceac.ai.modules.campus.interfaces.api.dto.DeleteQuizSlotResponse;
import tools.ceac.ai.modules.campus.interfaces.api.dto.SaveQuizCommentRequest;
import tools.ceac.ai.modules.campus.interfaces.api.dto.SaveQuizUserOverrideRequest;
import tools.ceac.ai.modules.campus.interfaces.api.dto.UpdateQuizMultichoiceQuestionRequest;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class QuizController {

    private final GetQuizResultsUseCase getQuizResultsUseCase;
    private final GetQuizAttemptReviewUseCase getQuizAttemptReviewUseCase;
    private final GetQuizCommentUseCase getQuizCommentUseCase;
    private final SaveQuizCommentUseCase saveQuizCommentUseCase;
    private final GetQuizUserOverridesUseCase getQuizUserOverridesUseCase;
    private final SaveQuizUserOverrideUseCase saveQuizUserOverrideUseCase;
    private final DeleteQuizUserOverrideUseCase deleteQuizUserOverrideUseCase;
    private final GetQuizConfigUseCase getQuizConfigUseCase;
    private final GetQuizStructureUseCase getQuizStructureUseCase;
    private final GetQuizQuestionCategoriesUseCase getQuizQuestionCategoriesUseCase;
    private final CreateQuizMultichoiceQuestionUseCase createQuizMultichoiceQuestionUseCase;
    private final DeleteQuizSlotUseCase deleteQuizSlotUseCase;

    public QuizController(GetQuizResultsUseCase getQuizResultsUseCase,
                          GetQuizAttemptReviewUseCase getQuizAttemptReviewUseCase,
                          GetQuizCommentUseCase getQuizCommentUseCase,
                          SaveQuizCommentUseCase saveQuizCommentUseCase,
                          GetQuizUserOverridesUseCase getQuizUserOverridesUseCase,
                          SaveQuizUserOverrideUseCase saveQuizUserOverrideUseCase,
                          DeleteQuizUserOverrideUseCase deleteQuizUserOverrideUseCase,
                          GetQuizConfigUseCase getQuizConfigUseCase,
                          GetQuizStructureUseCase getQuizStructureUseCase,
                          GetQuizQuestionCategoriesUseCase getQuizQuestionCategoriesUseCase,
                          CreateQuizMultichoiceQuestionUseCase createQuizMultichoiceQuestionUseCase,
                          DeleteQuizSlotUseCase deleteQuizSlotUseCase) {
        this.getQuizResultsUseCase = getQuizResultsUseCase;
        this.getQuizAttemptReviewUseCase = getQuizAttemptReviewUseCase;
        this.getQuizCommentUseCase = getQuizCommentUseCase;
        this.saveQuizCommentUseCase = saveQuizCommentUseCase;
        this.getQuizUserOverridesUseCase = getQuizUserOverridesUseCase;
        this.saveQuizUserOverrideUseCase = saveQuizUserOverrideUseCase;
        this.deleteQuizUserOverrideUseCase = deleteQuizUserOverrideUseCase;
        this.getQuizConfigUseCase = getQuizConfigUseCase;
        this.getQuizStructureUseCase = getQuizStructureUseCase;
        this.getQuizQuestionCategoriesUseCase = getQuizQuestionCategoriesUseCase;
        this.createQuizMultichoiceQuestionUseCase = createQuizMultichoiceQuestionUseCase;
        this.deleteQuizSlotUseCase = deleteQuizSlotUseCase;
    }

    @GetMapping("/quizzes/{cmid}/config")
    public QuizConfigResponse quizConfig(
            @Parameter(description = "ID del mÃ³dulo de quiz Moodle (cmid)", example = "314182")
            @PathVariable String cmid) {
        return toConfigResponse(getQuizConfigUseCase.execute(cmid));
    }

    @GetMapping("/quizzes/{cmid}/slots")
    public QuizStructureResponse quizSlots(
            @Parameter(description = "ID del mÃ³dulo de quiz Moodle (cmid)", example = "314207")
            @PathVariable String cmid) {
        return toStructureResponse(getQuizStructureUseCase.execute(cmid));
    }

    @GetMapping("/quizzes/{cmid}/question-categories")
    public List<QuizQuestionCategoryResponse> questionCategories(
            @Parameter(description = "ID del mÃ³dulo de quiz Moodle (cmid)", example = "314207")
            @PathVariable String cmid) {
        return getQuizQuestionCategoriesUseCase.execute(cmid)
                .stream()
                .map(c -> new QuizQuestionCategoryResponse(c.id(), c.name(), c.group(), c.categoryValue()))
                .toList();
    }

    @DeleteMapping("/quizzes/{cmid}/slots/{slotId}")
    public DeleteQuizSlotResponse deleteQuizSlot(
            @Parameter(description = "ID del mÃ³dulo de quiz Moodle (cmid)", example = "314207")
            @PathVariable String cmid,
            @Parameter(description = "ID del slot a eliminar", example = "570816")
            @PathVariable String slotId) {
        return deleteQuizSlotUseCase.execute(cmid, slotId);
    }

    @PostMapping("/quizzes/{cmid}/questions/multichoice")
    public ResponseEntity<Void> createMultichoiceQuestion(
            @Parameter(description = "ID del mÃ³dulo de quiz Moodle (cmid)", example = "314207")
            @PathVariable String cmid,
            @Parameter(description = "Valor de categorÃ­a obtenido de GET /api/quizzes/{cmid}/question-categories (campo categoryValue, formato 'categoryId,contextId')", example = "117056,379267")
            @RequestParam String category,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(
                    schema = @Schema(implementation = UpdateQuizMultichoiceQuestionRequest.class),
                    examples = @ExampleObject(value = """
                            {
                              "name": "Nueva pregunta de ejemplo",
                              "questionText": "<p>Â¿CuÃ¡l es la respuesta correcta?</p>",
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
                                { "text": "<p>OpciÃ³n A</p>", "fraction": "1.0",  "feedback": "<p>Correcto</p>" },
                                { "text": "<p>OpciÃ³n B</p>", "fraction": "-0.5", "feedback": "<p>Incorrecto</p>" },
                                { "text": "<p>OpciÃ³n C</p>", "fraction": "-0.5", "feedback": "<p>Incorrecto</p>" }
                              ],
                              "hints": []
                            }""")))
            @RequestBody UpdateQuizMultichoiceQuestionRequest request) {
        createQuizMultichoiceQuestionUseCase.execute(cmid, category, request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/quizzes/{id}/results")
    public List<QuizAttemptResponse> results(
            @Parameter(description = "ID del mÃ³dulo de quiz Moodle", example = "313877")
            @PathVariable String id) {
        return getQuizResultsUseCase.execute(id)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/quizzes/attempts/{attemptId}")
    public QuizAttemptReviewResponse attemptReview(
            @Parameter(description = "ID del intento de quiz Moodle", example = "1394723")
            @PathVariable String attemptId) {
        return toReviewResponse(getQuizAttemptReviewUseCase.execute(attemptId));
    }

    @GetMapping("/quizzes/attempts/{attemptId}/slots/{slot}/comment")
    public QuizCommentResponse quizComment(
            @Parameter(description = "ID del intento de quiz Moodle", example = "1394723")
            @PathVariable String attemptId,
            @Parameter(description = "NÃºmero de slot de la pregunta", example = "14")
            @PathVariable String slot) {
        return toCommentResponse(getQuizCommentUseCase.execute(attemptId, slot));
    }

    @DeleteMapping("/quizzes/overrides/{overrideId}")
    public ResponseEntity<Void> deleteUserOverride(
            @Parameter(description = "ID de la excepciÃ³n de usuario", example = "571")
            @PathVariable String overrideId) {
        deleteQuizUserOverrideUseCase.execute(overrideId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/quizzes/{cmid}/overrides")
    public ResponseEntity<Void> saveUserOverride(
            @Parameter(description = "ID del mÃ³dulo de quiz Moodle (cmid)", example = "314205")
            @PathVariable String cmid,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(
                    schema = @Schema(implementation = SaveQuizUserOverrideRequest.class),
                    examples = @ExampleObject(value = """
                            {
                              "userId": "14631",
                              "password": "123456",
                              "timeopen": "2026-02-19T00:00",
                              "timeclose": "2026-04-26T23:59",
                              "timelimitSeconds": 3600,
                              "attempts": 1
                            }""")))
            @RequestBody SaveQuizUserOverrideRequest request) {
        saveQuizUserOverrideUseCase.execute(
                cmid,
                request.userId(),
                request.password(),
                request.timeopen(),
                request.timeclose(),
                request.timelimitSeconds(),
                request.attempts()
        );
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/quizzes/{cmid}/overrides")
    public List<QuizUserOverrideResponse> userOverrides(
            @Parameter(description = "ID del mÃ³dulo de quiz Moodle (cmid)", example = "314205")
            @PathVariable String cmid) {
        return getQuizUserOverridesUseCase.execute(cmid)
                .stream()
                .map(this::toOverrideResponse)
                .toList();
    }

    @PostMapping("/quizzes/attempts/{attemptId}/slots/{slot}/comment")
    public ResponseEntity<Void> saveQuizComment(
            @Parameter(description = "ID del intento de quiz Moodle", example = "1394723")
            @PathVariable String attemptId,
            @Parameter(description = "NÃºmero de slot de la pregunta", example = "14")
            @PathVariable String slot,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(
                    schema = @Schema(implementation = SaveQuizCommentRequest.class),
                    examples = @ExampleObject(value = """
                            {
                              "mark": "1",
                              "comentario": "comentario"
                            }""")))
            @RequestBody SaveQuizCommentRequest request) {
        saveQuizCommentUseCase.execute(attemptId, slot, request.mark(), request.comentario());
        return ResponseEntity.noContent().build();
    }

    private QuizAttemptResponse toResponse(QuizAttempt attempt) {
        List<QuizQuestionResultResponse> questions = attempt.questions().stream()
                .map(q -> new QuizQuestionResultResponse(q.slot(), q.valor(), q.estado(), q.reviewUrl()))
                .toList();
        return new QuizAttemptResponse(
                attempt.userId(),
                attempt.attemptId(),
                attempt.fullName(),
                attempt.email(),
                attempt.estado(),
                attempt.comenzadoEl(),
                attempt.finalizado(),
                attempt.tiempoRequerido(),
                attempt.calificacion(),
                attempt.reviewUrl(),
                questions
        );
    }

    private QuizAttemptReviewResponse toReviewResponse(QuizAttemptReview review) {
        List<QuizAttemptQuestionResponse> questions = review.questions().stream()
                .map(q -> {
                    List<QuizAttemptAnswerOptionResponse> opciones = q.opciones().stream()
                            .map(o -> new QuizAttemptAnswerOptionResponse(
                                    o.letra(), o.texto(), o.seleccionada(), o.estado()))
                            .toList();
                    return new QuizAttemptQuestionResponse(
                            q.slot(), q.numero(), q.estado(), q.puntuacion(),
                            q.enunciado(), q.respuestaCorrecta(), opciones);
                })
                .toList();
        return new QuizAttemptReviewResponse(
                review.attemptId(),
                review.userId(),
                review.fullName(),
                review.comenzadoEl(),
                review.estado(),
                review.finalizadoEn(),
                review.tiempoEmpleado(),
                review.puntos(),
                review.calificacion(),
                questions
        );
    }

    private QuizUserOverrideResponse toOverrideResponse(QuizUserOverride override) {
        List<QuizOverrideSettingResponse> settings = override.settings().stream()
                .map(s -> new QuizOverrideSettingResponse(s.setting(), s.value()))
                .toList();
        return new QuizUserOverrideResponse(
                override.overrideId(),
                override.userId(),
                override.fullName(),
                override.email(),
                settings
        );
    }

    private QuizConfigResponse toConfigResponse(QuizConfig c) {
        return new QuizConfigResponse(
                c.cmid(), c.courseId(), c.name(), c.visible(),
                c.timeopen(), c.timeclose(), c.timelimitSeconds(), c.overduehandling(),
                c.gradepass(), c.attempts(), c.grademethod(),
                c.questionsperpage(), c.navmethod(),
                c.shuffleanswers(), c.preferredbehaviour(),
                toPhaseResponse(c.reviewDuringAttempt()),
                toPhaseResponse(c.reviewImmediatelyAfter()),
                toPhaseResponse(c.reviewLaterWhileOpen()),
                toPhaseResponse(c.reviewAfterClose()),
                c.decimalpoints(), c.questiondecimalpoints(),
                c.quizpassword(), c.browsersecurity(), c.requireSafeExamBrowser()
        );
    }

    private QuizReviewPhaseResponse toPhaseResponse(tools.ceac.ai.modules.campus.domain.model.QuizReviewPhase p) {
        return new QuizReviewPhaseResponse(
                p.attempt(), p.correctness(), p.marks(),
                p.specificFeedback(), p.generalFeedback(),
                p.rightAnswer(), p.overallFeedback()
        );
    }

    private QuizStructureResponse toStructureResponse(QuizStructure s) {
        List<QuizSlotResponse> slots = s.slots().stream()
                .map(slot -> new QuizSlotResponse(
                        slot.slotId(), slot.slotNumber(), slot.page(),
                        slot.questionType(), slot.questionName(), slot.questionText(),
                        slot.maxMark(), slot.bankCategoryUrl(), slot.questionId()))
                .toList();
        return new QuizStructureResponse(s.cmid(), s.totalQuestions(), s.totalMarks(), s.maxGrade(), slots);
    }

    private QuizCommentResponse toCommentResponse(QuizCommentData data) {
        List<QuizAttemptAnswerOptionResponse> opciones = data.opciones().stream()
                .map(o -> new QuizAttemptAnswerOptionResponse(
                        o.letra(), o.texto(), o.seleccionada(), o.estado()))
                .toList();
        return new QuizCommentResponse(
                data.attemptId(),
                data.slot(),
                data.usageId(),
                data.userId(),
                data.fullName(),
                data.cuestionario(),
                data.enunciado(),
                data.estado(),
                data.puntuacion(),
                data.mark(),
                data.maxMark(),
                data.minFraction(),
                data.maxFraction(),
                data.comentario(),
                data.sesskey(),
                data.sequencecheck(),
                data.itemid(),
                data.commentFormat(),
                data.respuestaCorrecta(),
                opciones
        );
    }
}

