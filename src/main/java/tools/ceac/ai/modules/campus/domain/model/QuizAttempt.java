package tools.ceac.ai.modules.campus.domain.model;

import java.util.List;

/** Summary of a student's quiz attempt as extracted from the quiz overview report. */
public record QuizAttempt(
        String userId,
        String attemptId,
        String fullName,
        String email,
        String estado,
        String comenzadoEl,
        String finalizado,
        String tiempoRequerido,
        String calificacion,
        String reviewUrl,
        List<QuizQuestionResult> questions
) {}

