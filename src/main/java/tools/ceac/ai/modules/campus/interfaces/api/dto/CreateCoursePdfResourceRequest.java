package tools.ceac.ai.modules.campus.interfaces.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record CreateCoursePdfResourceRequest(
        @Schema(description = "Numero de seccion del curso donde se creara el recurso", example = "3", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer section,
        @Schema(description = "Nombre visible del recurso en Moodle", example = "EMPLEABILIDAD", requiredMode = Schema.RequiredMode.REQUIRED)
        String name,
        @Schema(description = "Descripcion del recurso en HTML o texto", example = "<p>Cartel informativo sobre empleabilidad</p>")
        String description,
        @Schema(description = "Nombre del fichero PDF. Si no se informa, se deriva de name y se anade .pdf", example = "CARTEL EMPLEA FP.pdf")
        String fileName,
        @Schema(description = "Contenido del PDF en base64, con o sin prefijo data:application/pdf;base64,", example = "JVBERi0xLjQKJ...", requiredMode = Schema.RequiredMode.REQUIRED)
        String base64Content,
        @Schema(description = "Si el recurso se crea visible para el alumnado", example = "true")
        Boolean visible,
        @Schema(description = "Si la descripcion se muestra en la portada del curso", example = "false")
        Boolean showDescription
) {
}
