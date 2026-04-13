package tools.ceac.ai.modules.trello.domain.model;

public record TrelloOperationResult(
        boolean success,
        String operation,
        String targetType,
        String targetId,
        String message
) {
}
