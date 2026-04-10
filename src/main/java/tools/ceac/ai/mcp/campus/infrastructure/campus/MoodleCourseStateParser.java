package tools.ceac.ai.mcp.campus.infrastructure.campus;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import tools.ceac.ai.mcp.campus.domain.model.ActivitySummary;
import tools.ceac.ai.mcp.campus.domain.model.CourseDetail;
import tools.ceac.ai.mcp.campus.domain.model.SectionSummary;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class MoodleCourseStateParser {

    private final ObjectMapper objectMapper;

    public MoodleCourseStateParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public CourseDetail parse(String json) throws IOException {
        JsonNode root = objectMapper.readTree(json);
        JsonNode firstResult = root.get(0);

        if (firstResult == null || firstResult.path("error").asBoolean(true)) {
            throw new IllegalStateException("course_state_error");
        }

        JsonNode data = objectMapper.readTree(firstResult.get("data").asText());

        String courseId = data.path("course").path("id").asText();
        int numsections = data.path("course").path("numsections").asInt();

        Map<String, ActivitySummary> activitiesById = parseActivities(data.path("cm"));
        Map<String, List<ActivitySummary>> activitiesBySection = groupActivitiesBySection(data.path("cm"), activitiesById);

        List<SectionSummary> topLevel = parseSections(data.path("section"), activitiesBySection);

        return new CourseDetail(courseId, numsections, topLevel);
    }

    private Map<String, ActivitySummary> parseActivities(JsonNode cmArray) {
        Map<String, ActivitySummary> map = new HashMap<>();
        if (cmArray == null || !cmArray.isArray()) return map;
        for (JsonNode cm : cmArray) {
            String id = cm.path("id").asText();
            String name = cm.path("name").asText();
            String type = cm.path("module").asText();
            String url = cm.path("url").asText("");
            boolean visible = cm.path("visible").asBoolean(true);
            map.put(id, new ActivitySummary(id, name, type, url, visible));
        }
        return map;
    }

    private Map<String, List<ActivitySummary>> groupActivitiesBySection(JsonNode cmArray, Map<String, ActivitySummary> activitiesById) {
        Map<String, List<ActivitySummary>> map = new HashMap<>();
        if (cmArray == null || !cmArray.isArray()) return map;
        for (JsonNode cm : cmArray) {
            String sectionId = cm.path("sectionid").asText();
            String cmId = cm.path("id").asText();
            map.computeIfAbsent(sectionId, k -> new ArrayList<>()).add(activitiesById.get(cmId));
        }
        return map;
    }

    private List<SectionSummary> parseSections(JsonNode sectionArray, Map<String, List<ActivitySummary>> activitiesBySection) {
        if (sectionArray == null || !sectionArray.isArray()) return List.of();

        Map<String, SectionSummary> sectionsById = new HashMap<>();
        Map<String, String> parentById = new HashMap<>();

        for (JsonNode s : sectionArray) {
            String id = s.path("id").asText();
            String title = s.path("title").asText();
            int number = s.path("number").asInt();
            boolean visible = s.path("visible").asBoolean(true);
            String sectionUrl = s.path("sectionurl").asText();
            int parent = s.path("parent").asInt(0);

            List<ActivitySummary> activities = activitiesBySection.getOrDefault(id, List.of());
            SectionSummary section = new SectionSummary(id, title, number, visible, sectionUrl, activities, new ArrayList<>());
            sectionsById.put(id, section);
            parentById.put(id, String.valueOf(parent));
        }

        List<SectionSummary> topLevel = new ArrayList<>();
        for (Map.Entry<String, SectionSummary> entry : sectionsById.entrySet()) {
            String id = entry.getKey();
            String parentNumber = parentById.get(id);
            if ("0".equals(parentNumber)) {
                topLevel.add(entry.getValue());
            } else {
                String parentId = findSectionIdByNumber(sectionsById, parentNumber);
                if (parentId != null && sectionsById.containsKey(parentId)) {
                    ((ArrayList<SectionSummary>) sectionsById.get(parentId).children()).add(entry.getValue());
                } else {
                    topLevel.add(entry.getValue());
                }
            }
        }

        topLevel.sort((a, b) -> Integer.compare(a.number(), b.number()));
        topLevel.forEach(s -> s.children().sort((a, b) -> Integer.compare(a.number(), b.number())));

        return topLevel;
    }

    private String findSectionIdByNumber(Map<String, SectionSummary> sectionsById, String number) {
        for (Map.Entry<String, SectionSummary> e : sectionsById.entrySet()) {
            if (String.valueOf(e.getValue().number()).equals(number)) {
                return e.getKey();
            }
        }
        return null;
    }
}
