package tools.ceac.ai.mcp.campus.interfaces.api.dto;

import java.util.List;

public record QuizAttemptResponse(
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
        List<QuizQuestionResultResponse> questions
) {}