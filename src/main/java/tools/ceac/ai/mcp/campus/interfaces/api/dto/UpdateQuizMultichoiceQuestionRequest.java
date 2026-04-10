package tools.ceac.ai.mcp.campus.interfaces.api.dto;

import java.util.List;

/**
 * Request body for updating a multichoice quiz question.
 *
 * <p>Fields provided by the API consumer. All content fields map directly
 * to the Moodle question edit form POST parameters.
 *
 * <p>Answer fractions must be valid Moodle grade values, e.g.
 * {@code "1"} (100 %), {@code "0"}, {@code "-0.25"}, etc.
 */
public record UpdateQuizMultichoiceQuestionRequest(

        /** Question name (title). */
        String name,

        /** HTML/plain text for the question body. */
        String questionText,

        /**
         * Moodle question status. Typical values: {@code "ready"}, {@code "draft"}.
         * Pass {@code null} to keep the existing value.
         */
        String status,

        /** Default mark (e.g. {@code "1"}). */
        String defaultMark,

        /**
         * {@code "1"} = single answer (radio), {@code "0"} = multiple answers (checkboxes).
         */
        String single,

        /**
         * {@code "1"} = shuffle answer options, {@code "0"} = keep order.
         */
        String shuffleAnswers,

        /**
         * Numbering style. E.g. {@code "abc"}, {@code "ABCD"}, {@code "123"}, {@code "none"}.
         */
        String answerNumbering,

        /**
         * {@code "1"} = show the standard "Choose one:" instruction, {@code "0"} = hide it.
         */
        String showStandardInstruction,

        /** Per-answer penalty fraction for interactive/adaptive modes. E.g. {@code "0.3333333"}. */
        String penalty,

        /** General feedback shown after the question is submitted. */
        String generalFeedback,

        /** Feedback shown when the student answers correctly (100 %). */
        String correctFeedback,

        /** Feedback shown when the student answers partially correctly. */
        String partiallyCorrectFeedback,

        /** Feedback shown when the student answers incorrectly (0 %). */
        String incorrectFeedback,

        /** Ordered list of answer options. */
        List<AnswerItem> answers,

        /** Optional hints (shown on each «Try again» in interactive mode). */
        List<String> hints

) {

    public record AnswerItem(
            /** Answer text (HTML/plain). */
            String text,

            /**
             * Grade fraction, e.g. {@code "1"}, {@code "0"}, {@code "-0.25"}.
             * Use {@code "1"} for the correct answer in a single-answer question.
             */
            String fraction,

            /** Feedback shown when this specific answer is chosen. */
            String feedback
    ) {}
}