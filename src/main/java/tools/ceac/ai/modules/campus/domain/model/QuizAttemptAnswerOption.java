package tools.ceac.ai.modules.campus.domain.model;

/** One answer option for a multiple-choice question in a quiz attempt review. */
public record QuizAttemptAnswerOption(
        String letra,
        String texto,
        boolean seleccionada,
        String estado
) {}

