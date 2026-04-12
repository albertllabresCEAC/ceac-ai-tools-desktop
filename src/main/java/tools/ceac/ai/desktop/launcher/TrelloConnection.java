package tools.ceac.ai.desktop.launcher;

/**
 * Local Trello connection captured by the launcher for the current desktop session.
 *
 * <p>The Trello token never comes from the control plane. It is acquired interactively in the
 * operator browser and kept local to the machine so the embedded Trello runtime can use it.
 */
public record TrelloConnection(
        String accessToken,
        String memberId,
        String username,
        String fullName,
        String profileUrl
) {
    public String displayName() {
        if (fullName != null && !fullName.isBlank()) {
            if (username != null && !username.isBlank()) {
                return fullName + " (@" + username + ")";
            }
            return fullName;
        }
        if (username != null && !username.isBlank()) {
            return "@" + username;
        }
        return "Cuenta Trello";
    }
}
