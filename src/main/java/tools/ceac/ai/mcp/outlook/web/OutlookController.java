package tools.ceac.ai.mcp.outlook.web;

import tools.ceac.ai.mcp.outlook.model.AddAttachmentRequest;
import tools.ceac.ai.mcp.outlook.model.AttachmentContent;
import tools.ceac.ai.mcp.outlook.model.AttachmentInfo;
import tools.ceac.ai.mcp.outlook.model.CreateDraftRequest;
import tools.ceac.ai.mcp.outlook.model.MailDraftResponse;
import tools.ceac.ai.mcp.outlook.model.MailMessage;
import tools.ceac.ai.mcp.outlook.model.MessageQuery;
import tools.ceac.ai.mcp.outlook.model.SendMailRequest;
import tools.ceac.ai.mcp.outlook.model.StatusResponse;
import tools.ceac.ai.mcp.outlook.model.UpdateDraftRequest;
import tools.ceac.ai.mcp.outlook.service.OutlookService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/outlook")
public class OutlookController {

    private final OutlookService outlookService;

    public OutlookController(OutlookService outlookService) {
        this.outlookService = outlookService;
    }

    @GetMapping("/messages")
    @Operation(summary = "Lista mensajes desde una carpeta de Outlook")
    public List<MailMessage> listMessages(@Valid @ModelAttribute MessageQuery query) {
        return outlookService.listMessages(query);
    }

    @GetMapping("/messages/{entryId}")
    @Operation(summary = "Obtiene el detalle de un correo por entryId")
    public MailMessage getMessage(@PathVariable String entryId) {
        return outlookService.getMessage(entryId);
    }

    @GetMapping("/messages/{entryId}/attachments")
    @Operation(summary = "Lista adjuntos de un correo")
    public List<AttachmentInfo> listAttachments(@PathVariable String entryId) {
        return outlookService.listAttachments(entryId);
    }

    @GetMapping("/messages/{entryId}/attachments/{attachmentIndex}")
    @Operation(summary = "Obtiene un adjunto de un correo en Base64")
    public AttachmentContent downloadAttachment(@PathVariable String entryId,
                                                @PathVariable int attachmentIndex) {
        return outlookService.downloadAttachment(entryId, attachmentIndex);
    }

    @PostMapping("/drafts")
    @Operation(summary = "Crea un borrador en Outlook")
    public MailDraftResponse createDraft(@Valid @RequestBody CreateDraftRequest request) {
        return outlookService.createDraft(request);
    }

    @PostMapping("/drafts/{entryId}/attachments")
    @Operation(summary = "Adjunta un archivo Base64 a un borrador existente")
    public MailDraftResponse addAttachmentToDraft(@PathVariable String entryId,
                                                  @Valid @RequestBody AddAttachmentRequest request) {
        return outlookService.addAttachmentToDraft(entryId, request.getAttachment());
    }

    @PutMapping("/drafts/{entryId}")
    @Operation(summary = "Actualiza un borrador existente")
    public MailDraftResponse updateDraft(@PathVariable String entryId,
                                         @Valid @RequestBody UpdateDraftRequest request) {
        return outlookService.updateDraft(entryId, request);
    }

    @DeleteMapping("/drafts/{entryId}")
    @Operation(summary = "Descarta un borrador existente")
    public StatusResponse discardDraft(@PathVariable String entryId,
                                       @RequestParam(defaultValue = "false") boolean permanent) {
        return outlookService.discardDraft(entryId, permanent);
    }

    @PostMapping("/drafts/{entryId}/send")
    @Operation(summary = "Envia un borrador existente")
    public StatusResponse sendExistingDraft(@PathVariable String entryId) {
        return outlookService.sendExistingDraft(entryId);
    }

    @PostMapping("/send")
    @Operation(summary = "Envia un correo directamente")
    public StatusResponse sendMail(@Valid @RequestBody SendMailRequest request) {
        return outlookService.sendMail(request);
    }
}
