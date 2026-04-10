package tools.ceac.ai.modules.campus.application.service;

import tools.ceac.ai.modules.campus.application.port.out.CampusGateway;
import tools.ceac.ai.modules.campus.domain.exception.AuthenticationRequiredException;
import tools.ceac.ai.modules.campus.domain.model.FileDownload;
import tools.ceac.ai.modules.campus.domain.model.SubmissionSummary;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Downloads all files attached to a specific student's submission.
 * Returns each file as a base64-encoded {@link FileDownload} record,
 * resolving the filename from the final redirected URL.
 */
@Service
public class GetAssignSubmissionFilesUseCase {

    private final CampusGateway campusGateway;
    private final CampusSessionService sessionService;
    private final GetAssignSubmissionsUseCase getAssignSubmissionsUseCase;

    public GetAssignSubmissionFilesUseCase(
            CampusGateway campusGateway,
            CampusSessionService sessionService,
            GetAssignSubmissionsUseCase getAssignSubmissionsUseCase
    ) {
        this.campusGateway = campusGateway;
        this.sessionService = sessionService;
        this.getAssignSubmissionsUseCase = getAssignSubmissionsUseCase;
    }

    public List<FileDownload> execute(String assignId, String userId) {
        if (!sessionService.isAuthenticated()) {
            throw new AuthenticationRequiredException("not_authenticated");
        }

        List<SubmissionSummary> submissions = getAssignSubmissionsUseCase.execute(assignId);
        SubmissionSummary submission = submissions.stream()
                .filter(s -> userId.equals(s.userId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("submission_not_found: " + userId));

        try {
            List<FileDownload> result = new ArrayList<>();
            for (String url : submission.files()) {
                HttpResponse<byte[]> response = campusGateway.downloadFile(url);
                String filename = extractFilename(response.uri() != null ? response.uri().toString() : url);
                String mimeType = response.headers().firstValue("content-type")
                        .map(ct -> ct.split(";")[0].trim())
                        .orElse("application/octet-stream");
                String base64 = Base64.getEncoder().encodeToString(response.body());
                result.add(new FileDownload(filename, mimeType, base64));
            }
            return result;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("file_download_failed", e);
        } catch (IOException e) {
            throw new IllegalStateException("file_download_failed", e);
        }
    }

    private String extractFilename(String url) {
        try {
            String path = URI.create(url).getPath();
            String raw = path.substring(path.lastIndexOf('/') + 1);
            return URLDecoder.decode(raw, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "file";
        }
    }
}


