package tools.ceac.ai.mcp.campus.interfaces.api;

import tools.ceac.ai.mcp.campus.application.service.GetCourseParticipantsUseCase;
import tools.ceac.ai.mcp.campus.application.service.GetCourseUseCase;
import tools.ceac.ai.mcp.campus.domain.model.ActivitySummary;
import tools.ceac.ai.mcp.campus.domain.model.CourseDetail;
import tools.ceac.ai.mcp.campus.domain.model.CourseParticipant;
import tools.ceac.ai.mcp.campus.domain.model.SectionSummary;
import tools.ceac.ai.mcp.campus.interfaces.api.dto.ActivityResponse;
import tools.ceac.ai.mcp.campus.interfaces.api.dto.CourseDetailResponse;
import tools.ceac.ai.mcp.campus.interfaces.api.dto.ParticipantResponse;
import tools.ceac.ai.mcp.campus.interfaces.api.dto.SectionResponse;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CourseController {

    private final GetCourseUseCase getCourseUseCase;
    private final GetCourseParticipantsUseCase getCourseParticipantsUseCase;

    public CourseController(GetCourseUseCase getCourseUseCase, GetCourseParticipantsUseCase getCourseParticipantsUseCase) {
        this.getCourseUseCase = getCourseUseCase;
        this.getCourseParticipantsUseCase = getCourseParticipantsUseCase;
    }

    @GetMapping("/courses/{id}")
    public CourseDetailResponse course(
            @Parameter(description = "ID del curso Moodle", example = "8823")
            @PathVariable String id) {
        CourseDetail detail = getCourseUseCase.execute(id);
        return new CourseDetailResponse(
                detail.id(),
                detail.numsections(),
                detail.sections().stream().map(this::toSection).toList()
        );
    }

    @GetMapping("/courses/{id}/participants")
    public List<ParticipantResponse> participants(
            @Parameter(description = "ID del curso Moodle", example = "8823")
            @PathVariable String id) {
        return getCourseParticipantsUseCase.execute(id).stream()
                .map(p -> new ParticipantResponse(p.userId(), p.fullName(), p.email(), p.roles(), p.groups(), p.lastAccess()))
                .toList();
    }

    private SectionResponse toSection(SectionSummary s) {
        List<ActivityResponse> activities = s.activities().stream().map(this::toActivity).toList();
        List<SectionResponse> children = s.children().stream().map(this::toSection).toList();
        return new SectionResponse(s.id(), s.title(), s.number(), s.visible(), s.sectionUrl(), activities, children);
    }

    private ActivityResponse toActivity(ActivitySummary a) {
        return new ActivityResponse(a.id(), a.name(), a.type(), a.url(), a.visible());
    }
}
