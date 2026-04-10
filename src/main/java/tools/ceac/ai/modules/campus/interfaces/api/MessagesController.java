package tools.ceac.ai.modules.campus.interfaces.api;

import tools.ceac.ai.modules.campus.application.service.GetConversationMessagesUseCase;
import tools.ceac.ai.modules.campus.application.service.GetConversationsUseCase;
import tools.ceac.ai.modules.campus.application.service.GetMessageRecipientsUseCase;
import tools.ceac.ai.modules.campus.application.service.MarkConversationAsReadUseCase;
import tools.ceac.ai.modules.campus.application.service.ReplyToMessageUseCase;
import tools.ceac.ai.modules.campus.application.service.SendNewMessageUseCase;
import tools.ceac.ai.modules.campus.domain.model.Conversation;
import tools.ceac.ai.modules.campus.domain.model.ConversationDetail;
import tools.ceac.ai.modules.campus.interfaces.api.dto.ConversationDetailResponse;
import tools.ceac.ai.modules.campus.interfaces.api.dto.ConversationMemberResponse;
import tools.ceac.ai.modules.campus.interfaces.api.dto.ConversationMessageResponse;
import tools.ceac.ai.modules.campus.interfaces.api.dto.ConversationResponse;
import tools.ceac.ai.modules.campus.interfaces.api.dto.MessageRecipientResponse;
import tools.ceac.ai.modules.campus.interfaces.api.dto.ReplyToMessageRequest;
import tools.ceac.ai.modules.campus.interfaces.api.dto.SendNewMessageRequest;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class MessagesController {

    private final GetConversationsUseCase getConversationsUseCase;
    private final GetConversationMessagesUseCase getConversationMessagesUseCase;
    private final MarkConversationAsReadUseCase markConversationAsReadUseCase;
    private final ReplyToMessageUseCase replyToMessageUseCase;
    private final SendNewMessageUseCase sendNewMessageUseCase;
    private final GetMessageRecipientsUseCase getMessageRecipientsUseCase;

    public MessagesController(GetConversationsUseCase getConversationsUseCase,
                               GetConversationMessagesUseCase getConversationMessagesUseCase,
                               MarkConversationAsReadUseCase markConversationAsReadUseCase,
                               ReplyToMessageUseCase replyToMessageUseCase,
                               SendNewMessageUseCase sendNewMessageUseCase,
                               GetMessageRecipientsUseCase getMessageRecipientsUseCase) {
        this.getConversationsUseCase = getConversationsUseCase;
        this.getConversationMessagesUseCase = getConversationMessagesUseCase;
        this.markConversationAsReadUseCase = markConversationAsReadUseCase;
        this.replyToMessageUseCase = replyToMessageUseCase;
        this.sendNewMessageUseCase = sendNewMessageUseCase;
        this.getMessageRecipientsUseCase = getMessageRecipientsUseCase;
    }

    @GetMapping("/messages/conversations")
    public List<ConversationResponse> conversations(
            @Parameter(description = "ID del usuario Moodle", example = "7281")
            @RequestParam String userId,
            @Parameter(description = "1 = privadas, 2 = grupales", example = "1")
            @RequestParam(defaultValue = "1") int type,
            @Parameter(description = "MÃ¡ximo de conversaciones a devolver", example = "51")
            @RequestParam(defaultValue = "51") int limitNum,
            @Parameter(description = "Offset para paginaciÃ³n", example = "0")
            @RequestParam(defaultValue = "0") int limitFrom,
            @Parameter(description = "Solo favoritas", example = "false")
            @RequestParam(defaultValue = "false") boolean favourites,
            @Parameter(description = "Agrupa conversaciones con uno mismo", example = "true")
            @RequestParam(defaultValue = "true") boolean mergeSelf) {
        return getConversationsUseCase.execute(userId, type, limitNum, limitFrom, favourites, mergeSelf)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/messages/conversations/{convId}")
    public ConversationDetailResponse conversationMessages(
            @Parameter(description = "ID de la conversaciÃ³n", example = "39618")
            @PathVariable String convId,
            @Parameter(description = "ID del usuario actual", example = "7281")
            @RequestParam String currentUserId,
            @Parameter(description = "MÃ¡s recientes primero", example = "true")
            @RequestParam(defaultValue = "true") boolean newest,
            @Parameter(description = "MÃ¡ximo de mensajes a devolver", example = "101")
            @RequestParam(defaultValue = "101") int limitNum,
            @Parameter(description = "Offset para paginaciÃ³n", example = "0")
            @RequestParam(defaultValue = "0") int limitFrom) {
        ConversationDetail detail = getConversationMessagesUseCase.execute(
                currentUserId, convId, newest, limitNum, limitFrom);
        return toDetailResponse(detail);
    }

    @PostMapping("/messages/reply")
    public ResponseEntity<Void> replyToMessage(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos para enviar el mensaje de respuesta",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                                    value = "{\"studentUserId\":\"15084\",\"myUserId\":\"7281\",\"messageTimestamp\":\"1774607984\",\"messageId\":\"321451\",\"subject\":\"Re: Consulta\",\"content\":\"Hola, te respondo.\"}"
                            )
                    )
            )
            @RequestBody ReplyToMessageRequest req) {
        replyToMessageUseCase.execute(
                req.studentUserId(), req.myUserId(), req.messageTimestamp(),
                req.messageId(), req.subject(), req.content());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/messages/new")
    public ResponseEntity<Void> sendNewMessage(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos para enviar un nuevo mensaje",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                                    value = "{\"recipientIds\":[\"13372\",\"15084\"],\"subject\":\"Tutoria de proyecto\",\"content\":\"<p>Buenos dias,</p>\"}"
                            )
                    )
            )
            @RequestBody SendNewMessageRequest req) {
        sendNewMessageUseCase.execute(req.recipientIds(), req.subject(), req.content());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/messages/conversations/{convId}/read")
    public ResponseEntity<Void> markAsRead(
            @Parameter(description = "ID de la conversaciÃ³n", example = "39618")
            @PathVariable String convId,
            @Parameter(description = "ID del usuario", example = "7281")
            @RequestParam String userId) {
        markConversationAsReadUseCase.execute(userId, convId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/messages/recipients")
    public List<MessageRecipientResponse> recipients() {
        return getMessageRecipientsUseCase.execute()
                .stream()
                .map(r -> new MessageRecipientResponse(r.id(), r.fullName()))
                .toList();
    }

    private ConversationDetailResponse toDetailResponse(ConversationDetail d) {
        List<ConversationMemberResponse> members = d.members().stream()
                .map(m -> new ConversationMemberResponse(
                        m.id(), m.fullName(), m.profileImageUrl(),
                        m.isOnline(), m.isBlocked(), m.isContact()))
                .toList();
        List<ConversationMessageResponse> messages = d.messages().stream()
                .map(m -> new ConversationMessageResponse(m.id(), m.userIdFrom(), m.text(), m.timeCreated()))
                .toList();
        return new ConversationDetailResponse(d.id(), members, messages);
    }

    private ConversationResponse toResponse(Conversation c) {
        List<ConversationMemberResponse> members = c.members().stream()
                .map(m -> new ConversationMemberResponse(
                        m.id(), m.fullName(), m.profileImageUrl(),
                        m.isOnline(), m.isBlocked(), m.isContact()))
                .toList();
        List<ConversationMessageResponse> messages = c.messages().stream()
                .map(m -> new ConversationMessageResponse(m.id(), m.userIdFrom(), m.text(), m.timeCreated()))
                .toList();
        return new ConversationResponse(
                c.id(), c.name(), c.type(), c.unreadCount(),
                c.isRead(), c.isFavourite(), c.isMuted(),
                members, messages);
    }
}

