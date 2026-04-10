package tools.ceac.ai.mcp.campus.domain.model;

import java.util.List;

/**
 * All data needed to display and grade an open-ended quiz question.
 * Contains both display fields (enunciado, opciones, estado) and the hidden
 * form fields required to POST the grade (sesskey, sequencecheck, usageId, itemid, etc.).
 */
public record QuizCommentData(
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
        List<QuizAttemptAnswerOption> opciones
) {}