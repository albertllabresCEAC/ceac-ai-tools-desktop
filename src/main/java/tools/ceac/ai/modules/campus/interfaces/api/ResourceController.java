package tools.ceac.ai.modules.campus.interfaces.api;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import tools.ceac.ai.modules.campus.application.service.CreateCoursePdfResourceUseCase;
import tools.ceac.ai.modules.campus.interfaces.api.dto.CreateCoursePdfResourceRequest;
import tools.ceac.ai.modules.campus.interfaces.api.dto.CreateCoursePdfResourceResponse;

@RestController
@RequestMapping("/api")
public class ResourceController {

    private final CreateCoursePdfResourceUseCase createCoursePdfResourceUseCase;

    public ResourceController(CreateCoursePdfResourceUseCase createCoursePdfResourceUseCase) {
        this.createCoursePdfResourceUseCase = createCoursePdfResourceUseCase;
    }

    @PostMapping("/courses/{courseId}/resources/pdf")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Crea un recurso PDF en una seccion del curso",
            description = """
                    Funcionalidad experimental.
                    La subida del recurso PDF da problemas por ahora y puede fallar durante la subida al borrador de Moodle
                    o en el envio final del formulario modedit.
                    No debe considerarse un flujo estable de produccion todavia.
                    """)
    public CreateCoursePdfResourceResponse createPdfResource(
            @Parameter(name = "courseId", in = ParameterIn.PATH, required = true,
                    description = "ID del curso Moodle", example = "8202")
            @PathVariable("courseId") String courseId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(
                    schema = @Schema(implementation = CreateCoursePdfResourceRequest.class),
                    examples = @ExampleObject(value = """
                            {
                              "section": 3,
                              "name": "EMPLEABILIDAD",
                              "description": "<p>Cartel informativo sobre empleabilidad</p>",
                              "fileName": "CARTEL EMPLEA FP.pdf",
                              "base64Content": "JVBERi0xLjQKJ...",
                              "visible": true,
                              "showDescription": false
                            }""")))
            @RequestBody CreateCoursePdfResourceRequest request) {
        return createCoursePdfResourceUseCase.execute(courseId, request);
    }
}
