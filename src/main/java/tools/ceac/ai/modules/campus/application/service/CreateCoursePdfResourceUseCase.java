package tools.ceac.ai.modules.campus.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import tools.ceac.ai.modules.campus.application.port.out.CampusGateway;
import tools.ceac.ai.modules.campus.domain.exception.AuthenticationRequiredException;
import tools.ceac.ai.modules.campus.domain.model.ResourceModEditFormData;
import tools.ceac.ai.modules.campus.infrastructure.campus.MoodleResourceModEditParser;
import tools.ceac.ai.modules.campus.infrastructure.config.CampusProperties;
import tools.ceac.ai.modules.campus.interfaces.api.dto.CreateCoursePdfResourceRequest;
import tools.ceac.ai.modules.campus.interfaces.api.dto.CreateCoursePdfResourceResponse;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class CreateCoursePdfResourceUseCase {

    private static final String DEFAULT_AVAILABILITY_JSON = "{\"op\":\"&\",\"c\":[],\"showc\":[]}";

    private final CampusGateway campusGateway;
    private final CampusSessionService sessionService;
    private final MoodleResourceModEditParser parser;
    private final CampusProperties properties;
    private final ObjectMapper objectMapper;

    public CreateCoursePdfResourceUseCase(CampusGateway campusGateway,
                                          CampusSessionService sessionService,
                                          MoodleResourceModEditParser parser,
                                          CampusProperties properties,
                                          ObjectMapper objectMapper) {
        this.campusGateway = campusGateway;
        this.sessionService = sessionService;
        this.parser = parser;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    public CreateCoursePdfResourceResponse execute(String courseId, CreateCoursePdfResourceRequest request) {
        if (!sessionService.isAuthenticated()) {
            throw new AuthenticationRequiredException("not_authenticated");
        }
        validateRequest(courseId, request);

        try {
            HttpResponse<String> formResponse = campusGateway.getResourceModEditForm(courseId, request.section().toString());
            ensureAuthenticated(formResponse, "resource_form_auth_failed");

            ResourceModEditFormData formData = parser.parse(formResponse.body(), properties.baseUrl());
            sessionService.storeSesskey(formData.formFields().get("sesskey"));

            String resolvedFileName = resolveFileName(request.fileName(), request.name());
            byte[] pdfBytes = decodePdf(request.base64Content());

            HttpResponse<String> uploadResponse = campusGateway.uploadResourceDraftFile(
                    formData.formFields().get("sesskey"),
                    formData.uploadRepositoryId(),
                    formData.filesItemId(),
                    formData.author(),
                    "/",
                    resolvedFileName,
                    formData.contextId(),
                    resolvedFileName,
                    pdfBytes
            );
            ensureAuthenticated(uploadResponse, "resource_upload_auth_failed");
            validateUploadResponse(uploadResponse.body(), formData.filesItemId(), resolvedFileName);

            if (formData.clientId() != null && !formData.clientId().isBlank()) {
                HttpResponse<String> draftListResponse = campusGateway.listDraftFiles(
                        formData.formFields().get("sesskey"),
                        formData.clientId(),
                        "/",
                        formData.filesItemId()
                );
                ensureAuthenticated(draftListResponse, "resource_draft_list_auth_failed");
                verifyDraftListBestEffort(draftListResponse.body(), resolvedFileName);
            }

            Map<String, String> params = buildResourceParams(courseId, request, formData);
            HttpResponse<String> createResponse = campusGateway.postResourceEdit(params);
            ensureAuthenticated(createResponse, "resource_create_auth_failed");
            validateCreateResponse(createResponse);

            String resourceUrl = createResponse.uri() != null
                    ? createResponse.uri().toString()
                    : properties.baseUrl() + "/course/view.php?id=" + courseId;

            return new CreateCoursePdfResourceResponse(
                    courseId,
                    request.section(),
                    request.name(),
                    resolvedFileName,
                    resourceUrl
            );
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("resource_create_failed", exception);
        } catch (IOException exception) {
            throw new IllegalStateException("resource_create_failed: " + exception.getMessage(), exception);
        }
    }

    private void validateRequest(String courseId, CreateCoursePdfResourceRequest request) {
        if (courseId == null || courseId.isBlank()) {
            throw new IllegalArgumentException("course_id_required");
        }
        if (request == null) {
            throw new IllegalArgumentException("request_required");
        }
        if (request.section() == null) {
            throw new IllegalArgumentException("section_required");
        }
        if (request.name() == null || request.name().isBlank()) {
            throw new IllegalArgumentException("resource_name_required");
        }
        if (request.base64Content() == null || request.base64Content().isBlank()) {
            throw new IllegalArgumentException("base64_content_required");
        }
    }

    private void ensureAuthenticated(HttpResponse<String> response, String failureCode) {
        if (!campusGateway.looksAuthenticated(response)) {
            throw new AuthenticationRequiredException(failureCode);
        }
    }

    private void validateUploadResponse(String body, String expectedItemId, String expectedFileName) {
        JsonNode node = readJson(body, "resource_upload_invalid_response");
        String itemId = node.path("id").asText();
        String fileName = node.path("file").asText();
        if (itemId == null || itemId.isBlank() || fileName == null || fileName.isBlank()) {
            throw new IllegalStateException("resource_upload_failed");
        }
        if (!expectedItemId.equals(itemId) || !expectedFileName.equals(fileName)) {
            throw new IllegalStateException("resource_upload_mismatch");
        }
    }

    private void validateDraftListResponse(String body, String expectedFileName) {
        JsonNode node = readJson(body, "resource_draft_list_invalid_response");
        if (node.path("filecount").asInt(0) <= 0) {
            throw new IllegalStateException("resource_draft_verification_failed");
        }
        boolean found = false;
        for (JsonNode file : node.path("list")) {
            if (expectedFileName.equals(file.path("filename").asText())) {
                found = true;
                break;
            }
        }
        if (!found) {
            throw new IllegalStateException("resource_uploaded_file_not_found");
        }
    }

    private void verifyDraftListBestEffort(String body, String expectedFileName) {
        try {
            validateDraftListResponse(body, expectedFileName);
        } catch (IllegalStateException ignored) {
            // The draft listing is only a secondary verification step. Moodle may vary this endpoint
            // without affecting the final modedit submission.
        }
    }

    private void validateCreateResponse(HttpResponse<String> response) {
        if (response.statusCode() >= 400) {
            throw new IllegalStateException("resource_create_failed");
        }
        String finalUrl = response.uri() != null ? response.uri().toString() : "";
        if (finalUrl.contains("/course/modedit.php")) {
            throw new IllegalStateException("resource_create_validation_failed");
        }
    }

    private Map<String, String> buildResourceParams(String courseId,
                                                    CreateCoursePdfResourceRequest request,
                                                    ResourceModEditFormData formData) {
        Map<String, String> params = new LinkedHashMap<>(formData.formFields());
        params.put("completionunlocked", params.getOrDefault("completionunlocked", "1"));
        params.put("course", courseId);
        params.put("section", request.section().toString());
        params.put("module", params.getOrDefault("module", "25"));
        params.put("modulename", params.getOrDefault("modulename", "resource"));
        params.put("add", params.getOrDefault("add", "resource"));
        params.put("update", params.getOrDefault("update", "0"));
        params.put("return", params.getOrDefault("return", "0"));
        params.put("sr", params.getOrDefault("sr", "0"));
        params.put("beforemod", params.getOrDefault("beforemod", "0"));
        params.put("showonly", params.getOrDefault("showonly", ""));
        params.put("revision", params.getOrDefault("revision", "1"));
        params.put("_qf__mod_resource_mod_form", params.getOrDefault("_qf__mod_resource_mod_form", "1"));
        params.put("mform_showmore_id_optionssection", params.getOrDefault("mform_showmore_id_optionssection", "0"));
        params.put("mform_isexpanded_id_general", params.getOrDefault("mform_isexpanded_id_general", "1"));
        params.put("mform_isexpanded_id_optionssection", params.getOrDefault("mform_isexpanded_id_optionssection", "0"));
        params.put("mform_isexpanded_id_modstandardelshdr", params.getOrDefault("mform_isexpanded_id_modstandardelshdr", "0"));
        params.put("mform_isexpanded_id_availabilityconditionsheader", params.getOrDefault("mform_isexpanded_id_availabilityconditionsheader", "0"));
        params.put("mform_isexpanded_id_activitycompletionheader", params.getOrDefault("mform_isexpanded_id_activitycompletionheader", "0"));
        params.put("mform_isexpanded_id_tagshdr", params.getOrDefault("mform_isexpanded_id_tagshdr", "0"));
        params.put("name", request.name());
        params.put("introeditor[text]", request.description() != null ? request.description() : "");
        params.put("introeditor[format]", params.getOrDefault("introeditor[format]", "1"));
        params.put("introeditor[itemid]", params.getOrDefault("introeditor[itemid]", ""));
        params.put("showdescription", booleanToNumeric(request.showDescription(), params.get("showdescription"), "0"));
        params.put("files", formData.filesItemId());
        params.put("display", params.getOrDefault("display", "0"));
        params.put("filterfiles", params.getOrDefault("filterfiles", "0"));
        params.put("printintro", params.getOrDefault("printintro", "1"));
        params.put("visible", booleanToNumeric(request.visible(), params.get("visible"), "1"));
        params.put("cmidnumber", params.getOrDefault("cmidnumber", ""));
        params.put("lang", params.getOrDefault("lang", ""));
        params.put("availabilityconditionsjson", params.getOrDefault("availabilityconditionsjson", DEFAULT_AVAILABILITY_JSON));
        params.put("completion", params.getOrDefault("completion", "0"));
        params.put("tags", params.getOrDefault("tags", "_qf__force_multiselect_submission"));
        params.remove("submitbutton");
        params.put("submitbutton2", "Guardar cambios y regresar al curso");
        return params;
    }

    private String booleanToNumeric(Boolean value, String fallback, String defaultValue) {
        if (value != null) {
            return value ? "1" : "0";
        }
        if (fallback != null && !fallback.isBlank()) {
            return fallback;
        }
        return defaultValue;
    }

    private String resolveFileName(String providedFileName, String resourceName) {
        String candidate = providedFileName != null && !providedFileName.isBlank()
                ? providedFileName.trim()
                : resourceName.trim();
        return candidate.toLowerCase().endsWith(".pdf") ? candidate : candidate + ".pdf";
    }

    private byte[] decodePdf(String rawBase64) {
        String normalized = rawBase64.trim();
        if (normalized.startsWith("data:")) {
            int commaIndex = normalized.indexOf(',');
            if (commaIndex >= 0 && commaIndex + 1 < normalized.length()) {
                normalized = normalized.substring(commaIndex + 1);
            }
        }
        byte[] bytes;
        try {
            bytes = Base64.getDecoder().decode(normalized);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("base64_content_invalid", exception);
        }
        if (!isPdf(bytes)) {
            throw new IllegalArgumentException("base64_content_is_not_pdf");
        }
        return bytes;
    }

    private boolean isPdf(byte[] bytes) {
        return bytes != null
                && bytes.length >= 4
                && bytes[0] == 0x25
                && bytes[1] == 0x50
                && bytes[2] == 0x44
                && bytes[3] == 0x46;
    }

    private JsonNode readJson(String body, String failureCode) {
        try {
            return objectMapper.readTree(body);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException(failureCode + ": " + abbreviate(body), exception);
        }
    }

    private String abbreviate(String body) {
        if (body == null) {
            return "<empty>";
        }
        String normalized = body.replaceAll("\\s+", " ").trim();
        if (normalized.isEmpty()) {
            return "<empty>";
        }
        return normalized.length() <= 160 ? normalized : normalized.substring(0, 160) + "...";
    }
}
