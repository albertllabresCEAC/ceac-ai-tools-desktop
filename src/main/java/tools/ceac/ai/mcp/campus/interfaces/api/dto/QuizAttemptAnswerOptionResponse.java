package tools.ceac.ai.mcp.campus.interfaces.api.dto;

public record QuizAttemptAnswerOptionResponse(
        String letra,
        String texto,
        boolean seleccionada,
        String estado
) {}