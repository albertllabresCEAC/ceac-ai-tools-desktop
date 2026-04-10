package tools.ceac.ai.modules.campus.application.service;

import tools.ceac.ai.modules.campus.application.port.out.CampusGateway;
import tools.ceac.ai.modules.campus.domain.exception.AuthenticationRequiredException;
import tools.ceac.ai.modules.campus.infrastructure.campus.MoodleQuizOverrideEditParser;
import tools.ceac.ai.modules.campus.infrastructure.config.CampusProperties;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Creates or updates a user override for a quiz module (open/close dates,
 * time limit, password, number of attempts).
 * <p>
 * Workflow: GET override edit form â†’ extract sesskey â†’ POST the override data.
 * All date/time fields are optional; null values omit the corresponding setting.
 * </p>
 */
@Service
public class SaveQuizUserOverrideUseCase {

    private static final DateTimeFormatter DT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final CampusGateway campusGateway;
    private final CampusSessionService sessionService;
    private final MoodleQuizOverrideEditParser parser;
    private final CampusProperties properties;

    public SaveQuizUserOverrideUseCase(
            CampusGateway campusGateway,
            CampusSessionService sessionService,
            MoodleQuizOverrideEditParser parser,
            CampusProperties properties
    ) {
        this.campusGateway = campusGateway;
        this.sessionService = sessionService;
        this.parser = parser;
        this.properties = properties;
    }

    public void execute(String cmid, String userId, String password,
                        String timeopen, String timeclose,
                        Long timelimitSeconds, Integer attempts) {
        if (!sessionService.isAuthenticated()) {
            throw new AuthenticationRequiredException("not_authenticated");
        }
        try {
            String formHtml = campusGateway.getQuizOverrideEditForm(cmid).body();
            String sesskey = parser.parseSesskey(formHtml, properties.baseUrl());

            Integer[] open = parseDateTime(timeopen);
            Integer[] close = parseDateTime(timeclose);

            campusGateway.postQuizUserOverride(
                    cmid, sesskey, userId, password,
                    open[0], open[1], open[2], open[3], open[4],
                    close[0], close[1], close[2], close[3], close[4],
                    timelimitSeconds, attempts
            );
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("quiz_override_save_failed", e);
        } catch (IOException e) {
            throw new IllegalStateException("quiz_override_save_failed", e);
        }
    }

    /** Returns [day, month, year, hour, minute] or [nullÃ—5] if input is null. */
    private Integer[] parseDateTime(String iso) {
        if (iso == null || iso.isBlank()) return new Integer[]{null, null, null, null, null};
        LocalDateTime dt = LocalDateTime.parse(iso, DT);
        return new Integer[]{dt.getDayOfMonth(), dt.getMonthValue(), dt.getYear(), dt.getHour(), dt.getMinute()};
    }
}

