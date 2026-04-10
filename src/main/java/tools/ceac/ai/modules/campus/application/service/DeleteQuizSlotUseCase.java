package tools.ceac.ai.modules.campus.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import tools.ceac.ai.modules.campus.application.port.out.CampusGateway;
import tools.ceac.ai.modules.campus.domain.exception.AuthenticationRequiredException;
import tools.ceac.ai.modules.campus.domain.model.QuizEditMetadata;
import tools.ceac.ai.modules.campus.infrastructure.campus.MoodleQuizEditParser;
import tools.ceac.ai.modules.campus.infrastructure.config.CampusProperties;
import tools.ceac.ai.modules.campus.interfaces.api.dto.DeleteQuizSlotResponse;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class DeleteQuizSlotUseCase {

    private final CampusGateway campusGateway;
    private final CampusSessionService sessionService;
    private final MoodleQuizEditParser parser;
    private final CampusProperties properties;
    private final ObjectMapper objectMapper;

    public DeleteQuizSlotUseCase(CampusGateway campusGateway,
                                  CampusSessionService sessionService,
                                  MoodleQuizEditParser parser,
                                  CampusProperties properties,
                                  ObjectMapper objectMapper) {
        this.campusGateway = campusGateway;
        this.sessionService = sessionService;
        this.parser = parser;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    public DeleteQuizSlotResponse execute(String cmid, String slotId) {
        if (!sessionService.isAuthenticated()) {
            throw new AuthenticationRequiredException("not_authenticated");
        }
        try {
            String editPageHtml = campusGateway.getQuizEdit(cmid).body();
            QuizEditMetadata meta = parser.parseEditMetadata(editPageHtml, properties.baseUrl());

            String json = campusGateway.postQuizSlotDelete(
                    slotId, meta.sesskey(), meta.courseId(), meta.quizId()
            ).body();

            JsonNode node = objectMapper.readTree(json);
            String newsummarks    = node.path("newsummarks").asText("");
            boolean deleted       = node.path("deleted").asBoolean(false);
            int newnumquestions   = node.path("newnumquestions").asInt(0);

            return new DeleteQuizSlotResponse(newsummarks, deleted, newnumquestions);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("slot_delete_failed", e);
        } catch (IOException e) {
            throw new IllegalStateException("slot_delete_failed", e);
        }
    }
}


