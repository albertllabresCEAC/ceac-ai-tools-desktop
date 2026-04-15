package tools.ceac.ai.modules.campus.interfaces.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record CreateCourseAssignmentRequest(
        @Schema(description = "Numero de seccion del curso donde se creara la tarea", example = "3", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer section,
        @Schema(description = "Nombre visible de la tarea en Moodle", example = "Actividad final del modulo", requiredMode = Schema.RequiredMode.REQUIRED)
        String name,
        @Schema(description = "Descripcion de la tarea en HTML o texto", example = "<p>Lee las instrucciones y entrega la actividad final.</p>")
        String description,
        @Schema(description = "Instrucciones extendidas de la actividad en HTML o texto. No adjunta documentos.", example = "<p>Entrega un PDF con el desarrollo del caso practico.</p>")
        String activityInstructions,
        @Schema(description = "Fecha y hora desde la que se permiten entregas. ISO-8601 local o con zona.", example = "2026-04-20T08:00")
        String availableFrom,
        @Schema(description = "Fecha y hora de entrega. ISO-8601 local o con zona.", example = "2026-04-30T23:59")
        String dueAt,
        @Schema(description = "Fecha y hora limite de corte. ISO-8601 local o con zona.", example = "2026-05-02T23:59")
        String cutoffAt,
        @Schema(description = "Fecha y hora objetivo para correccion. ISO-8601 local o con zona.", example = "2026-05-07T23:59")
        String gradingDueAt,
        @Schema(description = "Si la actividad se crea visible para el alumnado", example = "true")
        Boolean visible,
        @Schema(description = "Si la descripcion se muestra en la portada del curso", example = "false")
        Boolean showDescription,
        @Schema(description = "Si la descripcion se muestra siempre antes de la fecha de apertura", example = "false")
        Boolean alwaysShowDescription,
        @Schema(description = "Si se notifican nuevas entregas a los docentes", example = "true")
        Boolean sendNotifications,
        @Schema(description = "Si se notifican entregas fuera de plazo", example = "false")
        Boolean sendLateNotifications,
        @Schema(description = "Si se envian notificaciones al alumnado por defecto", example = "true")
        Boolean sendStudentNotifications
) {
}
