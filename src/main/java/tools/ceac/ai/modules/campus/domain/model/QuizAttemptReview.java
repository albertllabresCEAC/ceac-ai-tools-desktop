package tools.ceac.ai.modules.campus.domain.model;

import java.util.List;

/** Full review of a quiz attempt including all questions, answers, and scores. */
public record QuizAttemptReview(
        String attemptId,
        String userId,
        String fullName,
        String comenzadoEl,
        String estado,
        String finalizadoEn,
        String tiempoEmpleado,
        String puntos,
        String calificacion,
        List<QuizAttemptQuestion> questions
) {}


