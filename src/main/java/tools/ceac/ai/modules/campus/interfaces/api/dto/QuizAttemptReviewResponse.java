package tools.ceac.ai.modules.campus.interfaces.api.dto;

import java.util.List;

public record QuizAttemptReviewResponse(
        String attemptId,
        String userId,
        String fullName,
        String comenzadoEl,
        String estado,
        String finalizadoEn,
        String tiempoEmpleado,
        String puntos,
        String calificacion,
        List<QuizAttemptQuestionResponse> questions
) {}

