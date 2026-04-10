package tools.ceac.ai.modules.campus.application.port.out;

import tools.ceac.ai.modules.campus.domain.model.DashboardSnapshot;

/**
 * Parser abstraction for dashboard HTML to domain model.
 */
public interface DashboardParser {
    DashboardSnapshot parse(String html);
}


