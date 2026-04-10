package tools.ceac.ai.mcp.campus.application.service;

import tools.ceac.ai.mcp.campus.infrastructure.campus.CampusSessionHttpClient;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Application service that owns session synchronization and auth state checks.
 */
@Service
public class CampusSessionService {
    private static final Pattern SESSKEY_PATTERN = Pattern.compile("\"sesskey\"\\s*:\\s*\"([^\"]+)\"");

    private final CampusSessionHttpClient sessionHttpClient;
    private volatile String sesskey = "";

    public CampusSessionService(CampusSessionHttpClient sessionHttpClient) {
        this.sessionHttpClient = sessionHttpClient;
    }

    public void storeSesskey(String sesskey) {
        this.sesskey = sesskey == null ? "" : sesskey;
    }

    public void storeSesskeyFromHtml(String html) {
        if (html == null) return;
        Matcher m = SESSKEY_PATTERN.matcher(html);
        if (m.find()) {
            storeSesskey(m.group(1));
        }
    }

    public String getSesskey() {
        return sesskey;
    }

    public void syncFromEmbeddedBrowser() {
        sessionHttpClient.syncFromEmbeddedBrowser();
    }

    public boolean isAuthenticated() {
        return sessionHttpClient.hasMoodleSessionCookie();
    }

    public void clearSession() {
        sessionHttpClient.clearCookies();
    }

    public String debugCookies() {
        return sessionHttpClient.debugCookies();
    }
}
