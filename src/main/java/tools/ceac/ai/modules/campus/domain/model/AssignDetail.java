package tools.ceac.ai.modules.campus.domain.model;

/**
 * Detalles de una tarea Moodle leídos desde modedit.php.
 * <p>
 * Las fechas son strings ISO-8601 locales ("2026-04-30T23:59") o {@code null} si están desactivadas.
 * </p>
 */
public record AssignDetail(
        String cmid,
        String courseId,
        String name,
        boolean visible,

        // Descripción e instrucciones
        String description,
        String activityInstructions,
        boolean showDescription,
        boolean alwaysShowDescription,

        // Fechas
        String availableFrom,
        String dueAt,
        String cutoffAt,
        String gradingDueAt,

        // Notificaciones
        boolean sendNotifications,
        boolean sendLateNotifications,
        boolean sendStudentNotifications,

        // Calificación
        String maxGrade,
        String gradePass,
        int maxAttempts,
        String attemptReopenMethod
) {}
