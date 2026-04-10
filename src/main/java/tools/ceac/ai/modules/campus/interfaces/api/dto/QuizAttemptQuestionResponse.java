package tools.ceac.ai.modules.campus.interfaces.api.dto;

import java.util.List;

public record QuizAttemptQuestionResponse(
        String slot,
        String numero,
        String estado,
        String puntuacion,
        String enunciado,
        String respuestaCorrecta,
        List<QuizAttemptAnswerOptionResponse> opciones
) {}

