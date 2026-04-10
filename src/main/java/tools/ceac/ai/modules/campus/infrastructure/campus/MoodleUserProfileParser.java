package tools.ceac.ai.modules.campus.infrastructure.campus;

import tools.ceac.ai.modules.campus.domain.model.CourseRef;
import tools.ceac.ai.modules.campus.domain.model.UserProfile;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Parses the Moodle public user profile page (/user/profile.php?id={userId}).
 *
 * Data extracted:
 *   - fullName       : h1 in page-header
 *   - email          : dt "DirecciÃ³n de correo" â†’ dd a (HTML-entity-obfuscated, Jsoup decodes it)
 *   - country        : dt "PaÃ­s" â†’ dd text
 *   - timezone       : dt "Zona horaria" â†’ dd text
 *   - courses        : dt "Perfiles de curso" â†’ dd ul li a  (course id from ?course= param)
 *   - firstAccess    : dt "Primer acceso al sitio" â†’ dd text
 *   - lastAccess     : dt "Ãšltimo acceso al sitio" â†’ dd text
 */
@Component
public class MoodleUserProfileParser {

    public UserProfile parse(String html, String baseUrl, String userId) {
        Document doc = Jsoup.parse(html, baseUrl);

        String fullName = parseFullName(doc);
        String email = parseDtValue(doc, "DirecciÃ³n de correo", true);
        String country = parseDtValue(doc, "PaÃ­s", false);
        String timezone = parseDtValue(doc, "Zona horaria", false);
        List<CourseRef> courses = parseCourses(doc);
        String firstAccess = parseDtValue(doc, "Primer acceso al sitio", false);
        String lastAccess = parseDtValue(doc, "Ãšltimo acceso al sitio", false);

        return new UserProfile(userId, fullName, email, country, timezone, courses, firstAccess, lastAccess);
    }

    private String parseFullName(Document doc) {
        Element h1 = doc.selectFirst("#page-header h1");
        if (h1 != null) return h1.text().trim();
        // fallback: breadcrumb current page
        Element crumb = doc.selectFirst("ol.breadcrumb li:last-child a");
        return crumb != null ? crumb.text().trim() : "";
    }

    /**
     * Finds a <dt> whose text matches the label and returns the text of its sibling <dd>.
     * If {@code linkText} is true, returns the text content of the first <a> inside <dd>.
     */
    private String parseDtValue(Document doc, String label, boolean linkText) {
        for (Element dt : doc.select("dl dt")) {
            if (dt.text().trim().equalsIgnoreCase(label)) {
                Element dd = dt.nextElementSibling();
                if (dd == null) continue;
                if (linkText) {
                    Element a = dd.selectFirst("a");
                    return a != null ? a.text().trim() : dd.text().trim();
                }
                // Strip the relative time in parentheses, e.g. " (74 dÃ­as 23 horas)"
                return dd.ownText().trim().replaceAll("\\s*\\(.*\\)\\s*$", "").trim();
            }
        }
        return "";
    }

    private List<CourseRef> parseCourses(Document doc) {
        List<CourseRef> result = new ArrayList<>();
        for (Element dt : doc.select("dl dt")) {
            if (dt.text().trim().equalsIgnoreCase("Perfiles de curso")) {
                Element dd = dt.nextElementSibling();
                if (dd == null) continue;
                for (Element a : dd.select("ul li a")) {
                    String href = a.attr("href");
                    String courseId = extractParam(href, "course");
                    String courseName = a.text().trim();
                    if (!courseId.isBlank() && !courseName.isBlank()) {
                        result.add(new CourseRef(courseId, courseName));
                    }
                }
                break;
            }
        }
        return result;
    }

    private String extractParam(String url, String param) {
        String prefix = param + "=";
        int start = url.indexOf(prefix);
        if (start == -1) return "";
        start += prefix.length();
        int end = url.indexOf('&', start);
        return end == -1 ? url.substring(start) : url.substring(start, end);
    }
}

