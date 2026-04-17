# Trello Card Custom Fields

## Scope

Trello card retrieval in this module now includes current custom field values directly in card
responses.

This affects:

- Single card detail.
- Card listing inside a list.
- MCP tools that expose those same card payloads.

## Trello API Behavior

The wrapper requests Trello cards with `customFieldItems=true`.

Endpoints used by the wrapper:

- `GET /cards/{cardId}`
- `GET /lists/{listId}/cards`

This keeps the integration at one Trello call per wrapper operation. The extra cost is mainly:

- Larger HTTP payloads.
- More JSON parsing.

It does not introduce an `N+1` request pattern for card listings.

## Returned Data

The card model now exposes:

- `customFieldItems`

Those items contain the current value references and raw values for the card. They do not include the
board-level definition metadata by themselves.

## Resolving Definitions

Custom field definitions remain a board-level concern in Trello. To resolve:

- field name
- field type
- list option labels

use the board custom field definition endpoint/tool:

- Local API and service flow based on `listCustomFields(boardId)`
- MCP tool `listarCamposPersonalizadosTrello(boardId)`

## Local API Impact

Affected local endpoints:

- `GET /api/trello/cards/{cardId}`
- `GET /api/trello/lists/{listId}/cards`

Both now return `customFieldItems` whenever Trello returns them.

## MCP Impact

Affected MCP tools:

- `verTarjetaTrello(cardId)`
- `listarTarjetasTrello(listId)`

Both descriptions explicitly state that values are embedded and that board definitions must be
resolved separately.
