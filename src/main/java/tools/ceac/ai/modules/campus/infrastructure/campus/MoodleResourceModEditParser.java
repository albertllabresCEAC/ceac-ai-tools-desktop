package tools.ceac.ai.modules.campus.infrastructure.campus;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.MissingNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;
import tools.ceac.ai.modules.campus.domain.model.ResourceModEditFormData;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class MoodleResourceModEditParser {

    private static final Pattern CTX_ID_PATTERN = Pattern.compile("[?&]ctx_id=(\\d+)");
    private static final String FILE_MANAGER_INIT_MARKER = "M.form_filemanager.init(Y,";
    private static final String FILE_MANAGER_TARGET = "id_files";
    private static final String DEFAULT_UPLOAD_REPOSITORY_ID = "5";

    private final ObjectMapper objectMapper;
    private final MoodleModEditFormFieldExtractor fieldExtractor;

    public MoodleResourceModEditParser(ObjectMapper objectMapper,
                                       MoodleModEditFormFieldExtractor fieldExtractor) {
        this.objectMapper = objectMapper;
        this.fieldExtractor = fieldExtractor;
    }

    public ResourceModEditFormData parse(String html, String baseUrl) {
        Element form = fieldExtractor.requireModEditForm(html, baseUrl, "resource_modedit_form_not_found");
        Map<String, String> fields = fieldExtractor.extractFormFields(form);
        JsonNode fileManagerConfig = extractFileManagerConfig(html, FILE_MANAGER_TARGET);

        String filesItemId = firstNonBlank(
                fields.get("files"),
                asTextOrNull(fileManagerConfig.get("itemid"))
        );
        String clientId = asTextOrNull(fileManagerConfig.get("client_id"));
        String contextId = firstNonBlank(
                asTextOrNull(fileManagerConfig.path("context").get("id")),
                extractContextIdFromObject(form.ownerDocument())
        );
        String author = firstNonBlank(
                asTextOrNull(fileManagerConfig.get("author")),
                "CEAC AI Tools"
        );
        String uploadRepositoryId = firstNonBlank(
                findUploadRepositoryId(fileManagerConfig.path("filepicker").path("repositories")),
                DEFAULT_UPLOAD_REPOSITORY_ID
        );

        if (filesItemId == null || filesItemId.isBlank()) {
            throw new IllegalStateException("resource_filemanager_itemid_not_found");
        }
        if (contextId == null || contextId.isBlank()) {
            throw new IllegalStateException("resource_context_id_not_found");
        }

        return new ResourceModEditFormData(
                fields,
                filesItemId,
                clientId,
                contextId,
                author,
                uploadRepositoryId
        );
    }

    private JsonNode extractFileManagerConfig(String html, String target) {
        int cursor = 0;
        while (true) {
            int markerIndex = html.indexOf(FILE_MANAGER_INIT_MARKER, cursor);
            if (markerIndex < 0) {
                return MissingNode.getInstance();
            }
            int objectStart = html.indexOf('{', markerIndex);
            if (objectStart < 0) {
                return MissingNode.getInstance();
            }
            int objectEnd = findMatchingBrace(html, objectStart);
            if (objectEnd < 0) {
                return MissingNode.getInstance();
            }
            String json = html.substring(objectStart, objectEnd + 1);
            try {
                JsonNode node = objectMapper.readTree(json);
                if (target.equals(node.path("target").asText())) {
                    return node;
                }
            } catch (IOException ignored) {
            }
            cursor = objectEnd + 1;
        }
    }

    private int findMatchingBrace(String text, int start) {
        int depth = 0;
        boolean inString = false;
        boolean escaped = false;
        for (int index = start; index < text.length(); index++) {
            char current = text.charAt(index);
            if (inString) {
                if (escaped) {
                    escaped = false;
                } else if (current == '\\') {
                    escaped = true;
                } else if (current == '"') {
                    inString = false;
                }
                continue;
            }
            if (current == '"') {
                inString = true;
                continue;
            }
            if (current == '{') {
                depth++;
            } else if (current == '}') {
                depth--;
                if (depth == 0) {
                    return index;
                }
            }
        }
        return -1;
    }

    private String findUploadRepositoryId(JsonNode repositoriesNode) {
        if (repositoriesNode == null || repositoriesNode.isMissingNode() || !repositoriesNode.isObject()) {
            return null;
        }
        Iterator<Map.Entry<String, JsonNode>> iterator = repositoriesNode.fields();
        while (iterator.hasNext()) {
            Map.Entry<String, JsonNode> entry = iterator.next();
            JsonNode repository = entry.getValue();
            if ("upload".equalsIgnoreCase(repository.path("type").asText())) {
                return firstNonBlank(asTextOrNull(repository.get("id")), entry.getKey());
            }
        }
        return null;
    }

    private String extractContextIdFromObject(Document doc) {
        Element object = doc.selectFirst("object[data*=draftfiles_manager.php?env=filemanager]");
        if (object == null) {
            return null;
        }
        Matcher matcher = CTX_ID_PATTERN.matcher(object.attr("data"));
        return matcher.find() ? matcher.group(1) : null;
    }

    private String asTextOrNull(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        String value = node.asText();
        return value == null || value.isBlank() ? null : value;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}
