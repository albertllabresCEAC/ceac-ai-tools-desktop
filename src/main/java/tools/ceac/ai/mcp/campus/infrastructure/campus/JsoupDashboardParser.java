package tools.ceac.ai.mcp.campus.infrastructure.campus;

import tools.ceac.ai.mcp.campus.application.port.out.DashboardParser;
import tools.ceac.ai.mcp.campus.domain.model.CourseSummary;
import tools.ceac.ai.mcp.campus.domain.model.DashboardSnapshot;
import tools.ceac.ai.mcp.campus.infrastructure.config.CampusProperties;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Jsoup-based dashboard parser implementation.
 */
@Component
public class JsoupDashboardParser implements DashboardParser {
    private static final Pattern COURSE_ID_PATTERN = Pattern.compile("[?&]id=(\\d+)");

    private final CampusProperties properties;

    public JsoupDashboardParser(CampusProperties properties) {
        this.properties = properties;
    }

    private static final Pattern LANG_PATTERN    = Pattern.compile("\"language\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern USERID_PATTERN  = Pattern.compile("data-userid=\"(\\d+)\"");

    @Override
    public DashboardSnapshot parse(String html) {
        Document doc = Jsoup.parse(html, properties.baseUrl());
        String pageTitle      = doc.title();
        String userDisplayName = extractUserDisplayName(doc);
        String userId         = extractUserId(doc, html);
        String email          = extractEmail(doc);
        String language       = extractLanguage(html);
        int unreadMessages    = extractUnreadMessages(doc);
        List<CourseSummary> courses = extractCourses(doc);
        String sesskey        = extractSesskey(doc);
        return new DashboardSnapshot(pageTitle, userDisplayName, userId, email, language, unreadMessages, courses, sesskey);
    }

    private String extractUserId(Document doc, String html) {
        Element el = doc.selectFirst("[data-userid]");
        if (el != null && !el.attr("data-userid").isBlank()) {
            return el.attr("data-userid");
        }
        Matcher m = USERID_PATTERN.matcher(html);
        return m.find() ? m.group(1) : "";
    }

    private String extractEmail(Document doc) {
        Element el = doc.selectFirst(".dropdown-user-mail");
        if (el != null && !el.text().isBlank()) {
            return el.text().trim();
        }
        return "";
    }

    private String extractLanguage(String html) {
        Matcher m = LANG_PATTERN.matcher(html);
        return m.find() ? m.group(1) : "";
    }

    private int extractUnreadMessages(Document doc) {
        Element el = doc.selectFirst(".rui-unreadcount span[aria-hidden=true]");
        if (el != null) {
            try { return Integer.parseInt(el.text().trim()); } catch (NumberFormatException ignored) {}
        }
        return 0;
    }

    private String extractUserDisplayName(Document doc) {
        // Tema Alpha: <span class="rui-fullname">Albert Llabres Darder</span>
        Element el = doc.selectFirst(".rui-fullname");
        if (el != null && !el.text().isBlank()) {
            return el.text().trim();
        }
        // Fallback: atributo title del avatar de iniciales
        el = doc.selectFirst("span.userinitials[title]");
        if (el != null && !el.attr("title").isBlank()) {
            return el.attr("title").trim();
        }
        // Fallback genérico Moodle boost
        el = doc.selectFirst(".usermenu .usertext");
        if (el != null && !el.text().isBlank()) {
            return el.text().trim();
        }
        return "";
    }

    private static final Pattern SESSKEY_PATTERN = Pattern.compile("\"sesskey\"\\s*:\\s*\"([^\"]+)\"");

    private String extractSesskey(Document doc) {
        Element input = doc.selectFirst("input[name=sesskey]");
        if (input != null && !input.val().isBlank()) {
            return input.val();
        }
        for (Element script : doc.select("script")) {
            Matcher m = SESSKEY_PATTERN.matcher(script.html());
            if (m.find()) {
                return m.group(1);
            }
        }
        return "";
    }

    private String extractCourseId(String url) {
        Matcher m = COURSE_ID_PATTERN.matcher(url);
        return m.find() ? m.group(1) : "";
    }

    private List<CourseSummary> extractCourses(Document doc) {
        Map<String, String> unique = new LinkedHashMap<>();
        Elements links = doc.select("a[href*=/course/view.php?id=]");
        for (Element link : links) {
            String href = link.absUrl("href");
            if (href.isBlank()) {
                continue;
            }
            String text = link.text().trim();
            unique.putIfAbsent(href, text.isBlank() ? href : text);
        }

        List<CourseSummary> courses = new ArrayList<>();
        for (Map.Entry<String, String> entry : unique.entrySet()) {
            String url = entry.getKey();
            String id = extractCourseId(url);
            courses.add(new CourseSummary(id, entry.getValue(), url));
        }
        return courses;
    }
}
