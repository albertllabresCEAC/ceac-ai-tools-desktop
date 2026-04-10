package tools.ceac.ai.mcp.campus.application.service;

import tools.ceac.ai.mcp.campus.application.port.out.CampusGateway;
import tools.ceac.ai.mcp.campus.application.port.out.DashboardParser;
import tools.ceac.ai.mcp.campus.domain.exception.AuthenticationRequiredException;
import tools.ceac.ai.mcp.campus.domain.model.DashboardSnapshot;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.http.HttpResponse;

/**
 * Use case to fetch and parse authenticated dashboard data.
 */
@Service
public class GetDashboardUseCase {
    private final CampusGateway campusGateway;
    private final DashboardParser dashboardParser;
    private final CampusSessionService sessionService;

    public GetDashboardUseCase(
            CampusGateway campusGateway,
            DashboardParser dashboardParser,
            CampusSessionService sessionService
    ) {
        this.campusGateway = campusGateway;
        this.dashboardParser = dashboardParser;
        this.sessionService = sessionService;
    }

    public DashboardSnapshot execute() {
        if (!sessionService.isAuthenticated()) {
            throw new AuthenticationRequiredException("not_authenticated");
        }

        try {
            HttpResponse<String> response = campusGateway.getDashboard();
            if (!campusGateway.looksAuthenticated(response)) {
                throw new AuthenticationRequiredException("session_expired");
            }
            DashboardSnapshot snapshot = dashboardParser.parse(response.body());
            if (!snapshot.sesskey().isBlank()) {
                sessionService.storeSesskey(snapshot.sesskey());
            }
            return snapshot;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("dashboard_fetch_failed", e);
        } catch (IOException e) {
            throw new IllegalStateException("dashboard_fetch_failed", e);
        }
    }
}
