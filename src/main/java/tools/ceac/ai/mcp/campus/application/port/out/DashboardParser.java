package tools.ceac.ai.mcp.campus.application.port.out;

import tools.ceac.ai.mcp.campus.domain.model.DashboardSnapshot;

/**
 * Parser abstraction for dashboard HTML to domain model.
 */
public interface DashboardParser {
    DashboardSnapshot parse(String html);
}
