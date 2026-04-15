package tools.ceac.ai.modules.campus.infrastructure.campus;

import org.jsoup.nodes.Element;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class MoodleModEditFormFieldExtractorTest {

    private final MoodleModEditFormFieldExtractor extractor = new MoodleModEditFormFieldExtractor();

    @Test
    void extractsRelevantFieldsFromModEditForm() {
        String html = """
                <html>
                  <body>
                    <form class="mform" action="https://campus.ceacfp.es/course/modedit.php">
                      <input type="hidden" name="sesskey" value="abc123">
                      <input type="text" name="name" value="Actividad final">
                      <textarea name="introeditor[text]"><p>Descripcion</p></textarea>
                      <select name="visible">
                        <option value="0">No</option>
                        <option value="1" selected>Si</option>
                      </select>
                      <input type="hidden" name="tags" value="_qf__force_multiselect_submission">
                      <select name="tags[]" multiple></select>
                      <input type="checkbox" name="showdescription" value="1" checked>
                      <input type="checkbox" name="submissionattachments" value="1">
                      <input type="submit" name="submitbutton" value="Guardar">
                    </form>
                  </body>
                </html>
                """;

        Element form = extractor.requireModEditForm(html, "https://campus.ceacfp.es", "form_missing");
        Map<String, String> fields = extractor.extractFormFields(form);

        assertThat(fields)
                .containsEntry("sesskey", "abc123")
                .containsEntry("name", "Actividad final")
                .containsEntry("introeditor[text]", "<p>Descripcion</p>")
                .containsEntry("visible", "1")
                .containsEntry("tags", "_qf__force_multiselect_submission")
                .containsEntry("showdescription", "1")
                .doesNotContainKey("tags[]")
                .doesNotContainKey("submissionattachments")
                .doesNotContainKey("submitbutton");
    }
}
