package tools.ceac.ai.modules.campus.infrastructure.campus;

import tools.ceac.ai.modules.campus.domain.model.AssignDetail;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

/**
 * Parsea la página de edición de tarea ({@code /course/modedit.php?update=N&return=1})
 * y extrae la configuración del assign desde los campos del formulario.
 */
@Component
public class MoodleAssignModEditParser {

    public AssignDetail parse(String html, String baseUrl) {
        Document doc = Jsoup.parse(html, baseUrl);

        String cmid     = inputValue(doc, "coursemodule");
        String courseId = inputValue(doc, "course");
        String name     = inputValue(doc, "name");
        boolean visible = "1".equals(selectValue(doc, "visible"));

        // Descripción e instrucciones (editor de texto enriquecido → textarea oculta)
        String description          = textareaValue(doc, "introeditor[text]");
        String activityInstructions = textareaValue(doc, "activityeditor[text]");
        boolean showDescription       = isChecked(doc, "showdescription");
        boolean alwaysShowDescription = isChecked(doc, "alwaysshowdescription");

        // Fechas
        String availableFrom = buildDatetime(doc, "allowsubmissionsfromdate",
                isChecked(doc, "allowsubmissionsfromdate[enabled]"));
        String dueAt         = buildDatetime(doc, "duedate",
                isChecked(doc, "duedate[enabled]"));
        String cutoffAt      = buildDatetime(doc, "cutoffdate",
                isChecked(doc, "cutoffdate[enabled]"));
        String gradingDueAt  = buildDatetime(doc, "gradingduedate",
                isChecked(doc, "gradingduedate[enabled]"));

        // Notificaciones
        boolean sendNotifications      = isChecked(doc, "sendnotifications");
        boolean sendLateNotifications  = isChecked(doc, "sendlatenotifications");
        boolean sendStudentNotifications = isChecked(doc, "sendstudentnotifications");

        // Calificación
        String maxGrade            = inputValue(doc, "grade");
        String gradePass           = inputValue(doc, "gradepass");
        int maxAttempts            = parseIntOrZero(selectValue(doc, "maxattempts"));
        String attemptReopenMethod = selectValue(doc, "attemptreopenmethod");

        return new AssignDetail(
                cmid, courseId, name, visible,
                description, activityInstructions, showDescription, alwaysShowDescription,
                availableFrom, dueAt, cutoffAt, gradingDueAt,
                sendNotifications, sendLateNotifications, sendStudentNotifications,
                maxGrade, gradePass, maxAttempts, attemptReopenMethod
        );
    }

    // -------------------------------------------------------------------------

    private String buildDatetime(Document doc, String prefix, boolean enabled) {
        if (!enabled) return null;
        String day    = selectValue(doc, prefix + "[day]");
        String month  = selectValue(doc, prefix + "[month]");
        String year   = selectValue(doc, prefix + "[year]");
        String hour   = selectValue(doc, prefix + "[hour]");
        String minute = selectValue(doc, prefix + "[minute]");
        if (year.isEmpty() || month.isEmpty() || day.isEmpty()) return null;
        return String.format("%s-%02d-%02dT%02d:%02d",
                year,
                Integer.parseInt(month),
                Integer.parseInt(day),
                hour.isEmpty()   ? 0 : Integer.parseInt(hour),
                minute.isEmpty() ? 0 : Integer.parseInt(minute));
    }

    private String selectValue(Document doc, String name) {
        Element opt = doc.selectFirst("select[name=\"" + name + "\"] option[selected]");
        return opt != null ? opt.attr("value") : "";
    }

    private String inputValue(Document doc, String name) {
        Element el = doc.selectFirst("input[name=\"" + name + "\"]");
        return el != null ? el.attr("value") : "";
    }

    private String textareaValue(Document doc, String name) {
        Element el = doc.selectFirst("textarea[name=\"" + name + "\"]");
        return el != null ? el.text() : "";
    }

    private boolean isChecked(Document doc, String name) {
        Element el = doc.selectFirst("input[type=checkbox][name=\"" + name + "\"]");
        return el != null && el.hasAttr("checked");
    }

    private int parseIntOrZero(String s) {
        try {
            return s.isEmpty() ? 0 : Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
