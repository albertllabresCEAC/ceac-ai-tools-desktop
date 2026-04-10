package tools.ceac.ai.mcp.campus.infrastructure.campus;

import tools.ceac.ai.mcp.campus.domain.model.QuizOverrideSetting;
import tools.ceac.ai.mcp.campus.domain.model.QuizUserOverride;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Parses the Moodle quiz user overrides page (/mod/quiz/overrides.php?cmid=N&mode=user).
 */
@Component
public class MoodleQuizOverridesParser {

    public List<QuizUserOverride> parse(String html, String baseUrl) {
        Document doc = Jsoup.parse(html, baseUrl);
        List<QuizUserOverride> result = new ArrayList<>();

        Element table = doc.selectFirst("table.generaltable");
        if (table == null) return result;

        Elements rows = table.select("tbody tr");
        int i = 0;
        while (i < rows.size()) {
            Element row = rows.get(i);

            // A user's first row has rowspan on the user cells
            Element userLink = row.selectFirst("td a[href*='user/view.php']");
            if (userLink == null) {
                i++;
                continue;
            }

            String fullName = userLink.text().trim();
            String userId = extractParam(userLink.attr("href"), "id");

            // email is the second td with rowspan
            Elements tds = row.select("td");
            String email = tds.size() >= 2 ? tds.get(1).text().trim() : "";

            // overrideId from edit link
            String overrideId = "";
            Element editLink = row.selectFirst("a[href*='overrideedit.php']");
            if (editLink != null) {
                String href = editLink.attr("href");
                if (!href.contains("action=")) {
                    overrideId = extractParam(href, "id");
                }
            }

            // collect settings from all rows belonging to this user (rowspan group)
            List<QuizOverrideSetting> settings = new ArrayList<>();

            // determine how many rows this user spans
            int rowspan = 1;
            Element firstUserTd = row.selectFirst("td[rowspan]");
            if (firstUserTd != null) {
                try {
                    rowspan = Integer.parseInt(firstUserTd.attr("rowspan"));
                } catch (NumberFormatException ignored) {
                }
            }

            for (int j = i; j < i + rowspan && j < rows.size(); j++) {
                Element r = rows.get(j);
                // setting and value are the last two non-rowspan tds (or the only tds in continuation rows)
                Elements allTds = r.select("td");
                // find the two tds that are NOT user/email (i.e., don't have rowspan > 1 for user info)
                List<Element> settingTds = new ArrayList<>();
                for (Element td : allTds) {
                    if (!td.hasAttr("rowspan")) {
                        settingTds.add(td);
                    }
                }
                if (settingTds.size() >= 2) {
                    settings.add(new QuizOverrideSetting(
                            settingTds.get(0).text().trim(),
                            settingTds.get(1).text().trim()
                    ));
                }
            }

            result.add(new QuizUserOverride(overrideId, userId, fullName, email, settings));
            i += rowspan;
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