package tools.ceac.ai.desktop.launcher;

import com.sun.jna.platform.win32.Crypt32Util;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Locale;
import java.util.Optional;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * Persists the Trello access token in the local user profile so the launcher can reuse the
 * browser authorization across desktop sessions.
 *
 * <p>On Windows the token is encrypted with DPAPI before it is stored in user preferences.
 */
public class TrelloLocalTokenStore {

    private static final String NODE_PATH = "tools/ceac/ai/desktop/trello";
    private static final String TOKEN_KEY = "access-token";
    private static final String FORMAT_DPAPI = "dpapi";
    private static final String FORMAT_PLAIN = "plain";
    private static final boolean WINDOWS = System.getProperty("os.name", "")
            .toLowerCase(Locale.ROOT)
            .contains("win");

    private final Preferences preferences = Preferences.userRoot().node(NODE_PATH);

    /**
     * Loads the locally persisted Trello token, if present.
     */
    public Optional<String> loadAccessToken() {
        String storedValue = preferences.get(TOKEN_KEY, null);
        if (storedValue == null || storedValue.isBlank()) {
            return Optional.empty();
        }

        try {
            int separator = storedValue.indexOf(':');
            if (separator <= 0 || separator == storedValue.length() - 1) {
                throw new IllegalStateException("Formato de token Trello no reconocido.");
            }

            String format = storedValue.substring(0, separator);
            byte[] payload = Base64.getDecoder().decode(storedValue.substring(separator + 1));
            byte[] clearBytes = switch (format) {
                case FORMAT_DPAPI -> Crypt32Util.cryptUnprotectData(payload);
                case FORMAT_PLAIN -> payload;
                default -> throw new IllegalStateException("Formato de token Trello no soportado: " + format);
            };

            String accessToken = new String(clearBytes, StandardCharsets.UTF_8).trim();
            if (accessToken.isBlank()) {
                clear();
                return Optional.empty();
            }
            return Optional.of(accessToken);
        } catch (Exception exception) {
            clearQuietly();
            throw new IllegalStateException("No he podido leer el token Trello guardado localmente.", exception);
        }
    }

    /**
     * Persists the Trello token for the current desktop user.
     */
    public void saveAccessToken(String accessToken) {
        String resolvedAccessToken = requireAccessToken(accessToken);
        byte[] clearBytes = resolvedAccessToken.getBytes(StandardCharsets.UTF_8);
        byte[] protectedBytes = WINDOWS ? Crypt32Util.cryptProtectData(clearBytes) : clearBytes;
        String format = WINDOWS ? FORMAT_DPAPI : FORMAT_PLAIN;
        String storedValue = format + ":" + Base64.getEncoder().encodeToString(protectedBytes);

        try {
            preferences.put(TOKEN_KEY, storedValue);
            preferences.flush();
        } catch (BackingStoreException exception) {
            throw new IllegalStateException("No he podido guardar el token Trello localmente.", exception);
        }
    }

    /**
     * Deletes any locally persisted Trello token for the current user.
     */
    public void clear() {
        try {
            preferences.remove(TOKEN_KEY);
            preferences.flush();
        } catch (BackingStoreException exception) {
            throw new IllegalStateException("No he podido limpiar el token Trello local.", exception);
        }
    }

    private void clearQuietly() {
        try {
            preferences.remove(TOKEN_KEY);
            preferences.flush();
        } catch (BackingStoreException ignored) {
        }
    }

    private String requireAccessToken(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            throw new IllegalStateException("No hay token Trello para persistir.");
        }
        return accessToken.trim();
    }
}
