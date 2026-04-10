package tools.ceac.ai.modules.campus.infrastructure.campus;

import tools.ceac.ai.modules.campus.domain.model.SubmissionSummary;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Parses the Moodle assign grading table HTML (/mod/assign/view.php?action=grading).
 */
@Component
public class MoodleAssignGradingParser {

    public AssignGradingPage parse(String html, String baseUrl) {
        Document doc = Jsoup.parse(html, baseUrl);

        String contextId = attr(doc, "input[name=contextid]", "value");
        String formUserId = attr(doc, "input[name=userid]", "value");
        String sesskey = attr(doc, "input[name=sesskey]", "value");
        int currentPerpage = parsePerpage(doc);

        List<SubmissionSummary> submissions = parseSubmissions(doc, baseUrl);
        return new AssignGradingPage(contextId, formUserId, sesskey, currentPerpage, submissions);
    }

    private int parsePerpage(Document doc) {
        Element selected = doc.selectFirst("select#id_perpage option[selected]");
        if (selected != null) {
            try {
                return Integer.parseInt(selected.val());
            } catch (NumberFormatException ignored) {
            }
        }
        return 10; // Moodle default if not found
    }

    private List<SubmissionSummary> parseSubmissions(Document doc, String baseUrl) {
        List<SubmissionSummary> list = new ArrayList<>();
        Elements rows = doc.select("tr[class^=user]");
        for (Element row : rows) {
            String userId = "";
            Element checkbox = row.selectFirst("input[name^=selectedusers]");
            if (checkbox != null) userId = checkbox.val();

            String fullName = "";
            Element nameLink = row.selectFirst("td.c2 a");
            if (nameLink != null) fullName = nameLink.text().trim();

            String email = "";
            Element emailCell = row.selectFirst("td.c3");
            if (emailCell != null) email = emailCell.text().trim();

            String status = extractStatus(row);
            String submittedAt = extractSubmittedAt(row);
            List<String> files = extractFiles(row);

            list.add(new SubmissionSummary(userId, fullName, email, status, submittedAt, files));
        }
        return list;
    }

    private String extractStatus(Element row) {
        Element el = row.selectFirst(".submissionstatussubmitted");
        if (el == null) el = row.selectFirst(".submissionstatus");
        return el != null ? el.text().trim() : "";
    }

    private String extractSubmittedAt(Element row) {
        Element el = row.selectFirst("td.c7");
        return el != null ? el.text().trim() : "";
    }

    private List<String> extractFiles(Element row) {
        List<String> files = new ArrayList<>();
        for (Element link : row.select(".fileuploadsubmission a[href]")) {
            String href = link.absUrl("href");
            if (!href.isBlank()) files.add(href);
        }
        return files;
    }

    private String attr(Document doc, String selector, String attribute) {
        Element el = doc.selectFirst(selector);
        return el != null ? el.attr(attribute) : "";
    }
}


