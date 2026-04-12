package tools.ceac.ai.modules.trello.domain.model;

import jakarta.validation.constraints.NotBlank;

public record CreateTrelloCardRequest(
        @NotBlank String listId,
        @NotBlank String name,
        String description,
        String due,
        String position
) {
}
