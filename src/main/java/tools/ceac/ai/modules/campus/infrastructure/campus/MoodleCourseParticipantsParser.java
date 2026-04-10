package tools.ceac.ai.modules.campus.infrastructure.campus;

import tools.ceac.ai.modules.campus.domain.model.CourseParticipant;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Parses the Moodle course participants page (/user/index.php?id={courseId}).
 *
 * Row structure (Moodle 4.x RUI theme):
 *   c0 â€“ checkbox
 *   c1 â€“ <th> name link (contains span.userinitials + text node with full name)
 *   c2 â€“ email (plain text)
 *   c3 â€“ roles (via a.quickeditlink own text)
 *   c4 â€“ groups (via a.quickeditlink own text)
 *   c5 â€“ last access
 *   c6 â€“ enrolment status
 */
@Component
public class MoodleCourseParticipantsParser {

    public List<CourseParticipant> parse(String html, String baseUrl) {
        Document doc = Jsoup.parse(html, baseUrl);
        List<CourseParticipant> result = new ArrayList<>();

        // Rows are identified by id pattern "user-index-participants-{courseId}_r{n}"
        for (Element row : doc.select("tr[id^='user-index-participants-']")) {
            CourseParticipant participant = parseRow(row);
            if (participant != null) {
                result.add(participant);
            }
        }
        return result;
    }

    private CourseParticipant parseRow(Element row) {
        // Name is in <th class="cell c1">
        Element nameCell = row.selectFirst("th.c1");
        if (nameCell == null) return null;

        Element nameLink = nameCell.selectFirst("a[href*='user/view.php']");
        if (nameLink == null) return null;

        String userId = extractParam(nameLink.attr("href"), "id");
        if (userId.isBlank()) return null;

        // ownText() returns only the direct text node, excluding span.userinitials content
        String fullName = nameLink.ownText().trim();

        // Email: c2 plain text
        String email = textOf(row, "td.c2");

        // Roles: c3 â€” text of a.quickeditlink (ownText avoids the pencil icon label)
        String roles = quickEditText(row, "td.c3");

        // Groups: c4 â€” same pattern
        String groups = quickEditText(row, "td.c4");

        // Last access: c5 plain text
        String lastAccess = textOf(row, "td.c5");

        return new CourseParticipant(userId, fullName, email, roles, groups, lastAccess);
    }

    private String textOf(Element row, String selector) {
        Element cell = row.selectFirst(selector);
        return cell != null ? cell.text().trim() : "";
    }

    private String quickEditText(Element row, String cellSelector) {
        Element cell = row.selectFirst(cellSelector);
        if (cell == null) return "";
        Element link = cell.selectFirst("a.quickeditlink");
        return link != null ? link.ownText().trim() : cell.text().trim();
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

