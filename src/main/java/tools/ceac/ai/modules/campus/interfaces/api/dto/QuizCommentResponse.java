package tools.ceac.ai.modules.campus.interfaces.api.dto;

import java.util.List;

public record QuizCommentResponse(
        String attemptId,
        String slot,
        String usageId,
        String userId,
        String fullName,
        String cuestionario,
        String enunciado,
        String estado,
        String puntuacion,
        String mark,
        String maxMark,
        String minFraction,
        String maxFraction,
        String comentario,
        String sesskey,
        String sequencecheck,
        String itemid,
        String commentFormat,
        String respuestaCorrecta,
        List<QuizAttemptAnswerOptionResponse> opciones
) {}

