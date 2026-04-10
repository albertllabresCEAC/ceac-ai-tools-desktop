package tools.ceac.ai.modules.campus.infrastructure.campus;

import tools.ceac.ai.modules.campus.domain.model.GraderInfo;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

/**
 * Parses the Moodle assign grader page HTML (/mod/assign/view.php?action=grader).
 */
@Component
public class MoodleGraderPageParser {

    public GraderInfo parse(String html) {
        Document doc = Jsoup.parse(html);

        Element gradePanel = doc.selectFirst("[data-region=grade]");
        String assignmentId = gradePanel != null ? gradePanel.attr("data-assignmentid") : "";
        String contextId    = gradePanel != null ? gradePanel.attr("data-contextid")    : "";

        Element userInfo = doc.selectFirst("[data-region=user-info]");
        String courseId  = userInfo != null ? userInfo.attr("data-courseid") : "";

        return new GraderInfo(assignmentId, contextId, courseId);
    }
}


