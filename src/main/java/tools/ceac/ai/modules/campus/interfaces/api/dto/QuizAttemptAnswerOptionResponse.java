package tools.ceac.ai.modules.campus.interfaces.api.dto;

public record QuizAttemptAnswerOptionResponse(
        String letra,
        String texto,
        boolean seleccionada,
        String estado
) {}

