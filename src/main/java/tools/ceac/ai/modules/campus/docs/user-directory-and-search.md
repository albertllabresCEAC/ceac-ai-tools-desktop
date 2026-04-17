# Campus User Directory And Search

## Scope

This module exposes two user-oriented capabilities on top of the existing campus messaging flow:

- Full user directory listing.
- User search by free text.

The same capabilities are available through local REST endpoints and MCP tools.

## Full Directory Listing

The full directory listing is based on the messaging compose flow used by Moodle and the
`itop_mailbox` block.

Flow:

1. `GET /blocks/itop_mailbox/compose.php`
2. Extract `sesskey` from the compose form.
3. `POST /blocks/itop_mailbox/compose.php` with `allcourses=1`
4. Parse the HTML `<select name="recipients[]">`
5. Return a flat list of `{ id, fullName }`

Only student recipients exposed by that compose form are returned.

## User Search

The user search does not filter the full directory locally. It calls Moodle's AJAX endpoint for
message user search:

- `POST /lib/ajax/service.php?sesskey=...&info=core_message_message_search_users`

Payload shape:

```json
[
  {
    "index": 0,
    "methodname": "core_message_message_search_users",
    "args": {
      "userid": "7281",
      "search": "Juan",
      "limitnum": 51,
      "limitfrom": 0
    }
  }
]
```

The implementation merges both `contacts` and `noncontacts` from Moodle's response and maps them to
the simplified runtime shape:

```json
[
  { "id": "7101", "fullName": "Juan Carlos Alonso Jimenez" }
]
```

Requirements:

- Authenticated campus session.
- Valid `sesskey`.
- Current Moodle user id from dashboard context.
- Non-empty search string.

## Local REST Endpoints

Message aliases:

- `GET /api/messages/users`
- `GET /api/messages/users/search?query=Juan&limitNum=51&limitFrom=0`

User aliases:

- `GET /api/users`
- `GET /api/users/search?query=Juan&limitNum=51&limitFrom=0`

All four endpoints return the same simplified DTO shape:

- `id`
- `fullName`

## MCP Tools

The MCP layer exposes the same functionality with three tool names:

- `getMessageRecipients()`
- `getAllUsers()`
- `searchUsers(query)`

`getMessageRecipients()` and `getAllUsers()` are aliases over the same full-directory retrieval.

## Notes

- Full directory listing and free-text search are intentionally separate flows.
- The listing flow returns what the compose form exposes.
- The search flow returns what Moodle's messaging search API returns for the current user context.
