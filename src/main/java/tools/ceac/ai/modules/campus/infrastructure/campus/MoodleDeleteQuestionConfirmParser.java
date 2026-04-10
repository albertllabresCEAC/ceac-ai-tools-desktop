package tools.ceac.ai.modules.campus.infrastructure.campus;

import tools.ceac.ai.modules.campus.domain.model.DeleteQuestionConfirmData;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

/**
 * Parses the confirmation page returned by:
 * {@code POST /question/bank/deletequestion/delete.php}
 * when called with {@code q{id}=1&deleteselected=Borrar}.
 *
 * <p>Moodle renders a modal dialog with a hidden confirmation form:
 * <pre>
 *   &lt;form method="post" action=".../deletequestion/delete.php"&gt;
 *     &lt;input type="hidden" name="deleteselected" value="3444792,3444791"&gt;
 *     &lt;input type="hidden" name="deleteall"      value="1"&gt;
 *     &lt;input type="hidden" name="confirm"        value="d8498cf23b0cb4a41b36c836150133ed"&gt;
 *     &lt;input type="hidden" name="sesskey"        value="2IB0vB1D8i"&gt;
 *     &lt;input type="hidden" name="returnurl"      value="/question/edit.php?cmid=314207&amp;..."&gt;
 *     &lt;input type="hidden" name="cmid"           value="314207"&gt;
 *     &lt;input type="hidden" name="courseid"       value="0"&gt;
 *     &lt;button type="submit"&gt;Borrar&lt;/button&gt;
 *   &lt;/form&gt;
 * </pre>
 */
@Component
public class MoodleDeleteQuestionConfirmParser {

    public DeleteQuestionConfirmData parse(String html, String baseUrl) {
        Document doc = Jsoup.parse(html, baseUrl);

        // Find the "Borrar" submit form (not the "Cancelar" one which is a GET form)
        Element form = doc.selectFirst(
                "form[method=post][action*=deletequestion]");

        if (form == null) {
            throw new IllegalStateException("delete_confirm_form_not_found");
        }

        String confirm       = valueOf(form, "confirm");
        String deleteSelected = valueOf(form, "deleteselected");
        String sesskey       = valueOf(form, "sesskey");
        String returnUrl     = valueOf(form, "returnurl");
        String cmid          = valueOf(form, "cmid");
        String courseId      = valueOf(form, "courseid");

        if (confirm.isBlank()) {
            throw new IllegalStateException("delete_confirm_token_missing");
        }

        return new DeleteQuestionConfirmData(confirm, deleteSelected, sesskey, returnUrl, cmid, courseId);
    }

    private String valueOf(Element form, String name) {
        Element input = form.selectFirst("input[name=" + name + "]");
        return input != null ? input.attr("value") : "";
    }
}

