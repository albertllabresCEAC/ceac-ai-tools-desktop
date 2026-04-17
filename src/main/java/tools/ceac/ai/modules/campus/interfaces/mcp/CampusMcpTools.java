package tools.ceac.ai.modules.campus.interfaces.mcp;

import tools.ceac.ai.modules.campus.application.service.CampusSessionService;
import tools.ceac.ai.modules.campus.application.service.CreateCourseAssignmentUseCase;
import tools.ceac.ai.modules.campus.application.service.CreateCoursePdfResourceUseCase;
import tools.ceac.ai.modules.campus.application.service.CreateQuestionInBankUseCase;
import tools.ceac.ai.modules.campus.application.service.CreateQuizMultichoiceQuestionUseCase;
import tools.ceac.ai.modules.campus.application.service.DeleteQuizSlotUseCase;
import tools.ceac.ai.modules.campus.application.service.DeleteQuizUserOverrideUseCase;
import tools.ceac.ai.modules.campus.application.service.GetAssignDetailUseCase;
import tools.ceac.ai.modules.campus.application.service.GetAssignSubmissionFilesUseCase;
import tools.ceac.ai.modules.campus.application.service.GetAssignSubmissionsUseCase;
import tools.ceac.ai.modules.campus.application.service.GetConversationMessagesUseCase;
import tools.ceac.ai.modules.campus.application.service.GetConversationsUseCase;
import tools.ceac.ai.modules.campus.application.service.GetCourseParticipantsUseCase;
import tools.ceac.ai.modules.campus.application.service.GetCourseUseCase;
import tools.ceac.ai.modules.campus.application.service.GetDashboardUseCase;
import tools.ceac.ai.modules.campus.application.service.GetGradeUseCase;
import tools.ceac.ai.modules.campus.application.service.GetMessageRecipientsUseCase;
import tools.ceac.ai.modules.campus.application.service.GetQuizAttemptReviewUseCase;
import tools.ceac.ai.modules.campus.application.service.GetQuizCommentUseCase;
import tools.ceac.ai.modules.campus.application.service.GetQuizConfigUseCase;
import tools.ceac.ai.modules.campus.application.service.GetQuizQuestionCategoriesUseCase;
import tools.ceac.ai.modules.campus.application.service.GetQuizQuestionUseCase;
import tools.ceac.ai.modules.campus.application.service.GetQuizResultsUseCase;
import tools.ceac.ai.modules.campus.application.service.GetQuizStructureUseCase;
import tools.ceac.ai.modules.campus.application.service.GetQuizUserOverridesUseCase;
import tools.ceac.ai.modules.campus.application.service.GetUserProfileUseCase;
import tools.ceac.ai.modules.campus.application.service.MarkConversationAsReadUseCase;
import tools.ceac.ai.modules.campus.application.service.ReplyToMessageUseCase;
import tools.ceac.ai.modules.campus.application.service.SaveQuizCommentUseCase;
import tools.ceac.ai.modules.campus.application.service.SaveQuizUserOverrideUseCase;
import tools.ceac.ai.modules.campus.application.service.SearchMessageRecipientsUseCase;
import tools.ceac.ai.modules.campus.application.service.SendNewMessageUseCase;
import tools.ceac.ai.modules.campus.application.service.SubmitGradeUseCase;
import tools.ceac.ai.modules.campus.application.service.UpdateQuizQuestionUseCase;
import tools.ceac.ai.modules.campus.domain.model.ConversationDetail;
import tools.ceac.ai.modules.campus.domain.model.CourseDetail;
import tools.ceac.ai.modules.campus.domain.model.DashboardSnapshot;
import tools.ceac.ai.modules.campus.interfaces.api.dto.CreateCourseAssignmentRequest;
import tools.ceac.ai.modules.campus.interfaces.api.dto.CreateCourseAssignmentResponse;
import tools.ceac.ai.modules.campus.interfaces.api.dto.CreateCoursePdfResourceRequest;
import tools.ceac.ai.modules.campus.interfaces.api.dto.CreateCoursePdfResourceResponse;
import tools.ceac.ai.modules.campus.interfaces.api.dto.DeleteQuizSlotResponse;
import tools.ceac.ai.modules.campus.interfaces.api.dto.UpdateQuizMultichoiceQuestionRequest;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CampusMcpTools {

    private final CampusSessionService sessionService;
    private final GetDashboardUseCase getDashboardUseCase;
    private final GetCourseUseCase getCourseUseCase;
    private final GetCourseParticipantsUseCase getCourseParticipantsUseCase;
    private final GetUserProfileUseCase getUserProfileUseCase;
    private final GetAssignDetailUseCase getAssignDetailUseCase;
    private final GetAssignSubmissionsUseCase getAssignSubmissionsUseCase;
    private final GetAssignSubmissionFilesUseCase getAssignSubmissionFilesUseCase;
    private final GetGradeUseCase getGradeUseCase;
    private final SubmitGradeUseCase submitGradeUseCase;
    private final GetQuizResultsUseCase getQuizResultsUseCase;
    private final GetQuizAttemptReviewUseCase getQuizAttemptReviewUseCase;
    private final GetQuizCommentUseCase getQuizCommentUseCase;
    private final SaveQuizCommentUseCase saveQuizCommentUseCase;
    private final GetQuizUserOverridesUseCase getQuizUserOverridesUseCase;
    private final SaveQuizUserOverrideUseCase saveQuizUserOverrideUseCase;
    private final DeleteQuizUserOverrideUseCase deleteQuizUserOverrideUseCase;
    private final GetConversationsUseCase getConversationsUseCase;
    private final GetConversationMessagesUseCase getConversationMessagesUseCase;
    private final MarkConversationAsReadUseCase markConversationAsReadUseCase;
    private final ReplyToMessageUseCase replyToMessageUseCase;
    private final SendNewMessageUseCase sendNewMessageUseCase;
    private final GetMessageRecipientsUseCase getMessageRecipientsUseCase;
    private final SearchMessageRecipientsUseCase searchMessageRecipientsUseCase;
    private final GetQuizConfigUseCase getQuizConfigUseCase;
    private final GetQuizStructureUseCase getQuizStructureUseCase;
    private final GetQuizQuestionCategoriesUseCase getQuizQuestionCategoriesUseCase;
    private final GetQuizQuestionUseCase getQuizQuestionUseCase;
    private final CreateCourseAssignmentUseCase createCourseAssignmentUseCase;
    private final CreateCoursePdfResourceUseCase createCoursePdfResourceUseCase;
    private final CreateQuizMultichoiceQuestionUseCase createQuizMultichoiceQuestionUseCase;
    private final CreateQuestionInBankUseCase createQuestionInBankUseCase;
    private final UpdateQuizQuestionUseCase updateQuizQuestionUseCase;
    private final DeleteQuizSlotUseCase deleteQuizSlotUseCase;

    public CampusMcpTools(
            CampusSessionService sessionService,
            GetDashboardUseCase getDashboardUseCase,
            GetCourseUseCase getCourseUseCase,
            GetCourseParticipantsUseCase getCourseParticipantsUseCase,
            GetUserProfileUseCase getUserProfileUseCase,
            GetAssignDetailUseCase getAssignDetailUseCase,
            GetAssignSubmissionsUseCase getAssignSubmissionsUseCase,
            GetAssignSubmissionFilesUseCase getAssignSubmissionFilesUseCase,
            GetGradeUseCase getGradeUseCase,
            SubmitGradeUseCase submitGradeUseCase,
            GetQuizResultsUseCase getQuizResultsUseCase,
            GetQuizAttemptReviewUseCase getQuizAttemptReviewUseCase,
            GetQuizCommentUseCase getQuizCommentUseCase,
            SaveQuizCommentUseCase saveQuizCommentUseCase,
            GetQuizUserOverridesUseCase getQuizUserOverridesUseCase,
            SaveQuizUserOverrideUseCase saveQuizUserOverrideUseCase,
            DeleteQuizUserOverrideUseCase deleteQuizUserOverrideUseCase,
            GetConversationsUseCase getConversationsUseCase,
            GetConversationMessagesUseCase getConversationMessagesUseCase,
            MarkConversationAsReadUseCase markConversationAsReadUseCase,
            ReplyToMessageUseCase replyToMessageUseCase,
            SendNewMessageUseCase sendNewMessageUseCase,
            GetMessageRecipientsUseCase getMessageRecipientsUseCase,
            SearchMessageRecipientsUseCase searchMessageRecipientsUseCase,
            GetQuizConfigUseCase getQuizConfigUseCase,
            GetQuizStructureUseCase getQuizStructureUseCase,
            GetQuizQuestionCategoriesUseCase getQuizQuestionCategoriesUseCase,
            GetQuizQuestionUseCase getQuizQuestionUseCase,
            CreateCourseAssignmentUseCase createCourseAssignmentUseCase,
            CreateCoursePdfResourceUseCase createCoursePdfResourceUseCase,
            CreateQuizMultichoiceQuestionUseCase createQuizMultichoiceQuestionUseCase,
            CreateQuestionInBankUseCase createQuestionInBankUseCase,
            UpdateQuizQuestionUseCase updateQuizQuestionUseCase,
            DeleteQuizSlotUseCase deleteQuizSlotUseCase) {
        this.sessionService = sessionService;
        this.getDashboardUseCase = getDashboardUseCase;
        this.getCourseUseCase = getCourseUseCase;
        this.getCourseParticipantsUseCase = getCourseParticipantsUseCase;
        this.getUserProfileUseCase = getUserProfileUseCase;
        this.getAssignDetailUseCase = getAssignDetailUseCase;
        this.getAssignSubmissionsUseCase = getAssignSubmissionsUseCase;
        this.getAssignSubmissionFilesUseCase = getAssignSubmissionFilesUseCase;
        this.getGradeUseCase = getGradeUseCase;
        this.submitGradeUseCase = submitGradeUseCase;
        this.getQuizResultsUseCase = getQuizResultsUseCase;
        this.getQuizAttemptReviewUseCase = getQuizAttemptReviewUseCase;
        this.getQuizCommentUseCase = getQuizCommentUseCase;
        this.saveQuizCommentUseCase = saveQuizCommentUseCase;
        this.getQuizUserOverridesUseCase = getQuizUserOverridesUseCase;
        this.saveQuizUserOverrideUseCase = saveQuizUserOverrideUseCase;
        this.deleteQuizUserOverrideUseCase = deleteQuizUserOverrideUseCase;
        this.getConversationsUseCase = getConversationsUseCase;
        this.getConversationMessagesUseCase = getConversationMessagesUseCase;
        this.markConversationAsReadUseCase = markConversationAsReadUseCase;
        this.replyToMessageUseCase = replyToMessageUseCase;
        this.sendNewMessageUseCase = sendNewMessageUseCase;
        this.getMessageRecipientsUseCase = getMessageRecipientsUseCase;
        this.searchMessageRecipientsUseCase = searchMessageRecipientsUseCase;
        this.getQuizConfigUseCase = getQuizConfigUseCase;
        this.getQuizStructureUseCase = getQuizStructureUseCase;
        this.getQuizQuestionCategoriesUseCase = getQuizQuestionCategoriesUseCase;
        this.getQuizQuestionUseCase = getQuizQuestionUseCase;
        this.createCourseAssignmentUseCase = createCourseAssignmentUseCase;
        this.createCoursePdfResourceUseCase = createCoursePdfResourceUseCase;
        this.createQuizMultichoiceQuestionUseCase = createQuizMultichoiceQuestionUseCase;
        this.createQuestionInBankUseCase = createQuestionInBankUseCase;
        this.updateQuizQuestionUseCase = updateQuizQuestionUseCase;
        this.deleteQuizSlotUseCase = deleteQuizSlotUseCase;
    }

    // Ã¢â€â‚¬Ã¢â€â‚¬ SISTEMA Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬

    @Tool(description = "Estado del servidor y si hay sesion activa en el campus")
    public HealthResult getHealth() {
        return new HealthResult("ok", sessionService.isAuthenticated());
    }

    // Ã¢â€â‚¬Ã¢â€â‚¬ PERFIL Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬

    @Tool(description = "Perfil del usuario autenticado: nombre, userId, email, idioma y mensajes no leidos")
    public ProfileResult getMyProfile() {
        DashboardSnapshot s = getDashboardUseCase.execute();
        return new ProfileResult(s.userDisplayName(), s.userId(), s.email(), s.language(), s.unreadMessages());
    }

    // Ã¢â€â‚¬Ã¢â€â‚¬ DASHBOARD Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬

    @Tool(description = "Lista de cursos del usuario autenticado con su id, nombre y url")
    public DashboardSnapshot getDashboard() {
        return getDashboardUseCase.execute();
    }

    // Ã¢â€â‚¬Ã¢â€â‚¬ CURSOS Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬

    @Tool(description = "Estructura de un curso: secciones, actividades y recursos. Requiere el courseId.")
    public CourseDetail getCourse(String courseId) {
        return getCourseUseCase.execute(courseId);
    }

    @Tool(description = "Lista de participantes matriculados en un curso con nombre, email, rol y ultimo acceso. Requiere el courseId.")
    public Object getCourseParticipants(String courseId) {
        return getCourseParticipantsUseCase.execute(courseId);
    }

    // Ã¢â€â‚¬Ã¢â€â‚¬ USUARIOS Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬

    @Tool(description = "Perfil completo de un usuario (alumno) por su userId: nombre, email, pais, cursos matriculados y accesos.")
    public Object getUserProfile(String userId) {
        return getUserProfileUseCase.execute(userId);
    }

    @Tool(description = """
            Crea una nueva tarea en una seccion de un curso sin subir documentos adjuntos.
            Requiere courseId y un request con section y name.
            Opcionales en request: description, activityInstructions, availableFrom, dueAt, cutoffAt, gradingDueAt,
            visible, showDescription, alwaysShowDescription, sendNotifications, sendLateNotifications y sendStudentNotifications.
            Las fechas aceptan formato ISO-8601 local como 2026-04-30T23:59 o con zona horaria.
            ANTES DE EJECUTAR: muestra al usuario un resumen del curso, la seccion, el nombre de la tarea y las fechas, y pide confirmacion mediante un selector Si/No. Solo procede si elige Si.""")
    public CreateCourseAssignmentResponse createAssignment(String courseId, CreateCourseAssignmentRequest request) {
        return createCourseAssignmentUseCase.execute(courseId, request);
    }

    @Tool(description = """
            ATENCION: esta herramienta da problemas por ahora y la subida del recurso puede fallar.
            El flujo sigue siendo experimental y no debe tratarse como estable en produccion.
            Crea un recurso de tipo Archivo subiendo un PDF a una seccion de un curso.
            Requiere courseId, section, name y base64Content.
            fileName es opcional; si no se informa se genera a partir de name con extension .pdf.
            description es opcional y acepta HTML o texto.
            visible y showDescription son opcionales.
            ANTES DE EJECUTAR: muestra al usuario un resumen del curso, la seccion, el nombre del recurso y el nombre del PDF, y pide confirmacion mediante un selector Si/No. Solo procede si elige Si.""")
    public CreateCoursePdfResourceResponse createPdfResource(String courseId,
                                                             Integer section,
                                                             String name,
                                                             String description,
                                                             String fileName,
                                                             String base64Content,
                                                             Boolean visible,
                                                             Boolean showDescription) {
        return createCoursePdfResourceUseCase.execute(courseId, new CreateCoursePdfResourceRequest(
                section,
                name,
                description,
                fileName,
                base64Content,
                visible,
                showDescription
        ));
    }

    // Ã¢â€â‚¬Ã¢â€â‚¬ TAREAS Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬

    @Tool(description = """
            Detalles de configuracion de una tarea Moodle scrapeando modedit.php.
            Devuelve: cmid, courseId, name, visible, description, activityInstructions, \
            showDescription, alwaysShowDescription, \
            availableFrom, dueAt, cutoffAt, gradingDueAt (fechas ISO-8601 locales "yyyy-MM-ddTHH:mm" o null si desactivadas), \
            sendNotifications, sendLateNotifications, sendStudentNotifications, \
            maxGrade, gradePass, maxAttempts (0 = ilimitados), attemptReopenMethod.
            Requiere el cmid del modulo de tarea (no el assignmentId de la API de Moodle).
            """)
    public Object getAssignment(String cmid) {
        return getAssignDetailUseCase.execute(cmid);
    }

    @Tool(description = "Lista de entregas de alumnos para una tarea. Requiere el assignmentId (id del modulo Moodle).")
    public Object getSubmissions(String assignmentId) {
        return getAssignSubmissionsUseCase.execute(assignmentId);
    }

    @Tool(description = "Ficheros adjuntos de la entrega de un alumno con su contenido en base64. Requiere assignmentId y userId.")
    public List<FileInfo> getSubmissionFiles(String assignmentId, String userId) {
        return getAssignSubmissionFilesUseCase.execute(assignmentId, userId)
                .stream()
                .map(f -> new FileInfo(f.filename(), f.mimeType(), f.content()))
                .toList();
    }

    @Tool(description = "Calificacion y feedback actual de la entrega de un alumno. Requiere assignmentId y userId.")
    public Object getGrade(String assignmentId, String userId) {
        return getGradeUseCase.execute(assignmentId, userId);
    }

    @Tool(description = """
            Califica la entrega de un alumno. Requiere assignmentId, userId, grade (entero), feedback (HTML o texto) y sendNotification (true/false).
            ANTES DE EJECUTAR: muestra al usuario un resumen (alumno, nota, feedback) y pide confirmacion mediante un selector Si/No. Solo procede si elige Si.""")
    public String submitGrade(String assignmentId, String userId, int grade, String feedback, boolean sendNotification) {
        submitGradeUseCase.execute(assignmentId, userId, grade, feedback, sendNotification);
        return "ok";
    }

    // Ã¢â€â‚¬Ã¢â€â‚¬ CUESTIONARIOS Ã¢â‚¬â€ RESULTADOS Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬

    @Tool(description = "Resultados de todos los intentos de un cuestionario. Requiere el quizId (id del modulo Moodle).")
    public Object getQuizResults(String quizId) {
        return getQuizResultsUseCase.execute(quizId);
    }

    @Tool(description = "Revision detallada de un intento de cuestionario: preguntas, respuestas, puntuaciones. Requiere attemptId.")
    public Object getQuizAttempt(String attemptId) {
        return getQuizAttemptReviewUseCase.execute(attemptId);
    }

    // Ã¢â€â‚¬Ã¢â€â‚¬ CUESTIONARIOS Ã¢â‚¬â€ CORRECCIÃƒâ€œN Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬

    @Tool(description = "Datos de correccion de una pregunta de un intento (enunciado, respuesta correcta, puntuacion actual, comentario). Requiere attemptId y slot.")
    public Object getQuizComment(String attemptId, String slot) {
        return getQuizCommentUseCase.execute(attemptId, slot);
    }

    @Tool(description = """
            Guarda la puntuacion y comentario de una pregunta de un intento. Requiere attemptId, slot, mark (numero como string) y comentario.
            ANTES DE EJECUTAR: muestra al usuario un resumen (intento, slot, nota, comentario) y pide confirmacion mediante un selector Si/No. Solo procede si elige Si.""")
    public String saveQuizComment(String attemptId, String slot, String mark, String comentario) {
        saveQuizCommentUseCase.execute(attemptId, slot, mark, comentario);
        return "ok";
    }

    // Ã¢â€â‚¬Ã¢â€â‚¬ CUESTIONARIOS Ã¢â‚¬â€ OVERRIDES Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬

    @Tool(description = "Lista de excepciones de usuario configuradas en un cuestionario. Requiere el cmid del modulo.")
    public Object getQuizOverrides(String cmid) {
        return getQuizUserOverridesUseCase.execute(cmid);
    }

    @Tool(description = """
            Crea o actualiza una excepcion de usuario en un cuestionario.
            Requiere: cmid, userId.
            Opcionales: password (String vacio si no se indica), timeopen (ISO: 2026-02-19T00:00), timeclose (ISO), timelimitSeconds, attempts.
            Pasar null en los opcionales que no se quieran modificar.
            ANTES DE EJECUTAR: muestra al usuario un resumen de los cambios y pide confirmacion mediante un selector Si/No. Solo procede si elige Si.""")
    public String saveQuizOverride(String cmid, String userId, String password,
                                   String timeopen, String timeclose,
                                   Long timelimitSeconds, Integer attempts) {
        saveQuizUserOverrideUseCase.execute(cmid, userId, password, timeopen, timeclose, timelimitSeconds, attempts);
        return "ok";
    }

    @Tool(description = """
            Elimina una excepcion de usuario de un cuestionario. Requiere el overrideId.
            ANTES DE EJECUTAR: muestra al usuario los datos de la excepcion y pide confirmacion mediante un selector Si/No. Solo procede si elige Si.""")
    public String deleteQuizOverride(String overrideId) {
        deleteQuizUserOverrideUseCase.execute(overrideId);
        return "ok";
    }

    // Ã¢â€â‚¬Ã¢â€â‚¬ MENSAJERÃƒÂA Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬

    @Tool(description = "Lista de conversaciones del usuario (bandeja de entrada). Requiere userId.")
    public Object getConversations(String userId) {
        return getConversationsUseCase.execute(userId, 1, 51, 0, false, true);
    }

    @Tool(description = "Mensajes de una conversacion con sus miembros. Requiere convId y currentUserId.")
    public ConversationDetail getConversationMessages(String convId, String currentUserId) {
        return getConversationMessagesUseCase.execute(currentUserId, convId, true, 101, 0);
    }

    @Tool(description = """
            Marca una conversacion como leida. Requiere convId y userId.
            ANTES DE EJECUTAR: pide confirmacion mediante un selector Si/No. Solo procede si elige Si.""")
    public String markConversationRead(String convId, String userId) {
        markConversationAsReadUseCase.execute(userId, convId);
        return "ok";
    }

    @Tool(description = """
            Responde a un mensaje existente. Requiere studentUserId, myUserId, messageTimestamp, messageId, subject y content (HTML).
            ANTES DE EJECUTAR: muestra al usuario el destinatario, asunto y contenido del mensaje y pide confirmacion mediante un selector Si/No. Solo procede si elige Si.""")
    public String replyToMessage(String studentUserId, String myUserId, String messageTimestamp,
                                  String messageId, String subject, String content) {
        replyToMessageUseCase.execute(studentUserId, myUserId, messageTimestamp, messageId, subject, content);
        return "ok";
    }

    @Tool(description = """
            Envia un nuevo mensaje a uno o varios destinatarios. Requiere lista de recipientIds (solo el numero, sin 'user-'), subject y content (HTML).
            ANTES DE EJECUTAR: muestra al usuario la lista de destinatarios, asunto y contenido del mensaje y pide confirmacion mediante un selector Si/No. Solo procede si elige Si.""")
    public String sendNewMessage(List<String> recipientIds, String subject, String content) {
        sendNewMessageUseCase.execute(recipientIds, subject, content);
        return "ok";
    }

    @Tool(description = "Lista de todos los alumnos de todos los cursos con su id y nombre completo (posibles destinatarios de mensajes).")
    public Object getMessageRecipients() {
        return getMessageRecipientsUseCase.execute();
    }

    @Tool(description = "Lista de todos los alumnos de todos los cursos con su id y nombre completo. Equivale al listado completo de posibles destinatarios de mensajes.")
    public Object getAllUsers() {
        return getMessageRecipientsUseCase.execute();
    }

    @Tool(description = """
            Busca usuarios por texto usando la busqueda del campus.
            Requiere query no vacia.
            Devuelve coincidencias de usuarios por id y nombre completo.
            """)
    public Object searchUsers(String query) {
        return searchMessageRecipientsUseCase.execute(query);
    }

    // Ã¢â€â‚¬Ã¢â€â‚¬ CUESTIONARIOS Ã¢â‚¬â€ ESTRUCTURA Y EDICIÃƒâ€œN Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬

    @Tool(description = "Configuracion de un cuestionario (fechas, limite de tiempo, intentos, calificacion, contrasena, etc.). Requiere el cmid del modulo.")
    public Object getQuizConfig(String cmid) {
        return getQuizConfigUseCase.execute(cmid);
    }

    @Tool(description = "Estructura de un cuestionario: slots (preguntas) con su numero, pagina, tipo, nombre y puntuacion maxima. Requiere el cmid del modulo.")
    public Object getQuizSlots(String cmid) {
        return getQuizStructureUseCase.execute(cmid);
    }

    @Tool(description = """
            Lista de categorias disponibles del banco de preguntas para un cuestionario.
            Requiere el cmid del modulo.
            El campo 'categoryValue' de cada categoria es el que hay que pasar al crear una pregunta.""")
    public Object getQuizQuestionCategories(String cmid) {
        return getQuizQuestionCategoriesUseCase.execute(cmid);
    }

    @Tool(description = "Datos completos de una pregunta (enunciado, respuestas, feedback, configuracion). Requiere questionId y el cmid del cuestionario.")
    public Object getQuestion(String questionId, String cmid) {
        return getQuizQuestionUseCase.execute(questionId, cmid);
    }

    @Tool(description = """
            Crea una nueva pregunta de tipo opcion multiple en el banco de preguntas de un cuestionario.
            Requiere: cmid y los datos de la pregunta.
            Campos obligatorios en question: name, questionText, defaultMark, single ('1'=una respuesta, '0'=varias), answers (lista con text, fraction y feedback).
            Fracciones: decimales sobre 1 con punto como separador. Ejemplos: 1.0 = correcta, 0.0 = incorrecta sin penalizacion, -0.5 = penalizacion 50%. NUNCA usar porcentajes enteros ni coma como separador decimal.
            ANTES DE EJECUTAR:
            1. Llama a getQuizQuestionCategories(cmid) y presenta al usuario la lista de categorias disponibles (campo name) para que seleccione una mediante un selector. Usa el campo categoryValue de la categoria seleccionada.
            2. Muestra al usuario un resumen de la pregunta y pide confirmacion mediante un selector Si/No. Solo procede si elige Si.""")
    public String createQuizMultichoiceQuestion(String cmid, String categoryValue,
                                                 UpdateQuizMultichoiceQuestionRequest question) {
        createQuizMultichoiceQuestionUseCase.execute(cmid, categoryValue, question);
        return "ok";
    }

    @Tool(description = """
            Esta herramienta SOLO crea la pregunta en el banco, NO la anade al cuestionario ni la hace visible a los alumnos; para crear una pregunta y anadirla directamente al test usa createQuizMultichoiceQuestion.
            Crea una nueva pregunta de tipo opcion multiple directamente en el banco de preguntas (sin anadirla a ningun slot del cuestionario).
            Requiere: cmid y los datos de la pregunta.
            Campos obligatorios en question: name, questionText, defaultMark, single ('1'=una respuesta, '0'=varias), answers (lista con text, fraction y feedback).
            Fracciones: decimales sobre 1 con punto como separador. Ejemplos: 1.0 = correcta, 0.0 = incorrecta sin penalizacion, -0.5 = penalizacion 50%. NUNCA usar porcentajes enteros ni coma como separador decimal.
            ANTES DE EJECUTAR:
            1. Llama a getQuizQuestionCategories(cmid) y presenta al usuario la lista de categorias disponibles (campo name) para que seleccione una mediante un selector. Usa el campo categoryValue de la categoria seleccionada.
            2. Muestra al usuario un resumen de la pregunta y pide confirmacion mediante un selector Si/No. Solo procede si elige Si.""")
    public String createQuestionInBank(String cmid, String categoryValue,
                                        UpdateQuizMultichoiceQuestionRequest question) {
        createQuestionInBankUseCase.execute(cmid, categoryValue, question);
        return "ok";
    }

    @Tool(description = """
            Actualiza una pregunta de tipo opcion multiple existente.
            Requiere: questionId, cmid y los datos actualizados de la pregunta.
            Solo se modifican los campos que se envien; los demas conservan su valor actual en Moodle.
            Fracciones: decimales sobre 1 con punto como separador. Ejemplos: 1.0 = correcta, 0.0 = incorrecta sin penalizacion, -0.5 = penalizacion 50%. NUNCA usar porcentajes enteros ni coma como separador decimal.
            ANTES DE EJECUTAR: muestra al usuario un resumen de los cambios que se van a aplicar y pide confirmacion mediante un selector Si/No. Solo procede si elige Si.""")
    public String updateQuizMultichoiceQuestion(String questionId, String cmid,
                                                 UpdateQuizMultichoiceQuestionRequest question) {
        updateQuizQuestionUseCase.execute(questionId, cmid, question);
        return "ok";
    }

    @Tool(description = """
            Elimina un slot (pregunta) de un cuestionario. Requiere el cmid del modulo y el slotId. Devuelve la nueva suma de puntos y el nuevo numero de preguntas.
            ANTES DE EJECUTAR: muestra al usuario los datos del slot (id, numero, nombre de la pregunta si esta disponible) y pide confirmacion mediante un selector Si/No. Solo procede si elige Si.""")
    public DeleteQuizSlotResponse deleteQuizSlot(String cmid, String slotId) {
        return deleteQuizSlotUseCase.execute(cmid, slotId);
    }

    // Ã¢â€â‚¬Ã¢â€â‚¬ Tipos internos Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬

    record HealthResult(String status, boolean authenticated) {}

    record ProfileResult(String name, String userId, String email, String language, int unreadMessages) {}

    record FileInfo(String filename, String mimeType, String content) {}
}

