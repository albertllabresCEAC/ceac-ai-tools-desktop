package tools.ceac.ai.modules.campus.domain.model;

import java.util.List;

/** A question within a quiz attempt review, including the student's answer options. */
public record QuizAttemptQuestion(
        String slot,
        String numero,
        String estado,
        String puntuacion,
        String enunciado,
        String respuestaCorrecta,
        List<QuizAttemptAnswerOption> opciones
) {}

