package tools.ceac.ai.modules.campus.infrastructure.campus;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class MoodleModEditFormFieldExtractor {

    public Element requireModEditForm(String html, String baseUrl, String failureCode) {
        Document document = Jsoup.parse(html, baseUrl);
        Element form = document.selectFirst("form.mform[action*=modedit.php]");
        if (form == null) {
            throw new IllegalStateException(failureCode);
        }
        return form;
    }

    public Map<String, String> extractFormFields(Element form) {
        Map<String, String> fields = new LinkedHashMap<>();
        for (Element element : form.select("input[name], textarea[name], select[name]")) {
            if (element.hasAttr("disabled")) {
                continue;
            }
            String name = element.attr("name");
            if (name == null || name.isBlank()) {
                continue;
            }
            String value = switch (element.tagName()) {
                case "textarea" -> element.text();
                case "select" -> extractSelectValue(element);
                default -> extractInputValue(element);
            };
            if (value != null) {
                fields.put(name, value);
            }
        }
        return fields;
    }

    private String extractSelectValue(Element select) {
        if (select.hasAttr("multiple")) {
            Element selected = select.selectFirst("option[selected]");
            return selected != null ? selected.attr("value") : null;
        }
        Element selected = select.selectFirst("option[selected]");
        if (selected != null) {
            return selected.attr("value");
        }
        Element first = select.selectFirst("option");
        return first != null ? first.attr("value") : "";
    }

    private String extractInputValue(Element input) {
        String type = input.attr("type").toLowerCase();
        if ("file".equals(type) || "submit".equals(type) || "button".equals(type)
                || "reset".equals(type) || "image".equals(type)) {
            return null;
        }
        if ("checkbox".equals(type) || "radio".equals(type)) {
            if (!input.hasAttr("checked")) {
                return null;
            }
            return input.hasAttr("value") ? input.attr("value") : "1";
        }
        return input.attr("value");
    }
}
