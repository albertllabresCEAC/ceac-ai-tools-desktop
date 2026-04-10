package tools.ceac.ai.modules.campus.interfaces.api;

import tools.ceac.ai.modules.campus.application.service.GetDashboardUseCase;
import tools.ceac.ai.modules.campus.domain.model.CourseSummary;
import tools.ceac.ai.modules.campus.domain.model.DashboardSnapshot;
import tools.ceac.ai.modules.campus.interfaces.api.dto.CourseResponse;
import tools.ceac.ai.modules.campus.interfaces.api.dto.DashboardResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class DashboardController {
    private final GetDashboardUseCase getDashboardUseCase;

    public DashboardController(GetDashboardUseCase getDashboardUseCase) {
        this.getDashboardUseCase = getDashboardUseCase;
    }

    @GetMapping("/dashboard")
    public DashboardResponse dashboard() {
        DashboardSnapshot snapshot = getDashboardUseCase.execute();
        List<CourseResponse> courses = snapshot.courses().stream()
                .map(this::toCourse)
                .toList();
        return new DashboardResponse(snapshot.pageTitle(), snapshot.userDisplayName(), courses);
    }

    private CourseResponse toCourse(CourseSummary courseSummary) {
        return new CourseResponse(courseSummary.id(), courseSummary.name(), courseSummary.url());
    }
}


