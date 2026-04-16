package tools.ceac.ai.modules.campus.interfaces.api.dto;

/**
 * Respuesta de la API REST para el endpoint {@code GET /api/assignments/{id}}.
 *
 * <p>Contiene la configuración completa de una tarea Moodle tal como aparece en la página
 * de edición del módulo ({@code /course/modedit.php?update={cmid}&return=1}).</p>
 *
 * <ul>
 *   <li>Las fechas ({@code availableFrom}, {@code dueAt}, {@code cutoffAt}, {@code gradingDueAt})
 *       son strings ISO-8601 locales con formato {@code yyyy-MM-ddTHH:mm}, o {@code null} si la
 *       fecha correspondiente está desactivada en Moodle.</li>
 *   <li>{@code maxAttempts} vale {@code 0} cuando Moodle lo muestra como "Intentos ilimitados".</li>
 * </ul>
 */
public record AssignDetailResponse(
        String cmid,
        String courseId,
        String name,
        boolean visible,

        String description,
        String activityInstructions,
        boolean showDescription,
        boolean alwaysShowDescription,

        String availableFrom,
        String dueAt,
        String cutoffAt,
        String gradingDueAt,

        boolean sendNotifications,
        boolean sendLateNotifications,
        boolean sendStudentNotifications,

        String maxGrade,
        String gradePass,
        int maxAttempts,
        String attemptReopenMethod
) {}
