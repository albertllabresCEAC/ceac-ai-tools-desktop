package tools.ceac.ai.modules.campus.application.port.out;

import java.io.IOException;
import java.net.http.HttpResponse;

/**
 * Outbound gateway to execute campus HTTP operations.
 */
public interface CampusGateway {
    HttpResponse<String> getDashboard() throws IOException, InterruptedException;

    HttpResponse<String> getCourseState(String courseId, String sesskey) throws IOException, InterruptedException;

    boolean looksAuthenticated(HttpResponse<String> response);

    HttpResponse<String> getAssignGradingPage(String assignId) throws IOException, InterruptedException;

    HttpResponse<String> postAssignSaveOptions(String assignId, String contextId, String formUserId, String sesskey) throws IOException, InterruptedException;

    HttpResponse<byte[]> downloadFile(String url) throws IOException, InterruptedException;

    HttpResponse<String> getGraderPage(String moduleId, String studentUserId) throws IOException, InterruptedException;

    HttpResponse<String> getUnusedDraftItemId(String sesskey) throws IOException, InterruptedException;

    HttpResponse<String> submitGrade(String assignmentId, String studentUserId, String jsonFormData, String sesskey) throws IOException, InterruptedException;

    HttpResponse<String> getSubmissionStatus(String assignmentId, String studentUserId, String sesskey) throws IOException, InterruptedException;

    HttpResponse<String> getCoreFragment(String contextId, String userId, String sesskey) throws IOException, InterruptedException;

    HttpResponse<String> getQuizResults(String quizId, String sesskey) throws IOException, InterruptedException;

    HttpResponse<String> getQuizAttemptReview(String attemptId) throws IOException, InterruptedException;

    HttpResponse<String> getQuizComment(String attemptId, String slot) throws IOException, InterruptedException;

    HttpResponse<String> postQuizComment(
            String attemptId, String slot, String usageId,
            String sesskey, String sequencecheck,
            String itemid, String commentFormat,
            String mark, String maxMark,
            String minFraction, String maxFraction,
            String comentario) throws IOException, InterruptedException;

    HttpResponse<String> getQuizUserOverrides(String cmid) throws IOException, InterruptedException;

    HttpResponse<String> getQuizOverrideEditForm(String cmid) throws IOException, InterruptedException;

    HttpResponse<String> postQuizUserOverride(
            String cmid, String sesskey,
            String userId, String password,
            Integer timeopenDay, Integer timeopenMonth, Integer timeopenYear, Integer timeopenHour, Integer timeopenMinute,
            Integer timecloseDay, Integer timecloseMonth, Integer timecloseYear, Integer timecloseHour, Integer timecloseMinute,
            Long timelimitSeconds,
            Integer attempts) throws IOException, InterruptedException;

    HttpResponse<String> getQuizOverrideDeleteForm(String overrideId) throws IOException, InterruptedException;

    HttpResponse<String> postQuizOverrideDelete(String overrideId, String sesskey) throws IOException, InterruptedException;

    HttpResponse<String> getConversations(String userId, String sesskey, int type, int limitNum, int limitFrom,
                                          boolean favourites, boolean mergeSelf) throws IOException, InterruptedException;

    HttpResponse<String> getConversationMessages(String currentUserId, String convId, String sesskey,
                                                  boolean newest, int limitNum, int limitFrom) throws IOException, InterruptedException;

    HttpResponse<String> markConversationAsRead(String userId, String conversationId, String sesskey) throws IOException, InterruptedException;

    HttpResponse<String> getComposeForm(String studentUserId, String myUserId, String messageTimestamp, String messageId) throws IOException, InterruptedException;

    HttpResponse<String> postComposeReply(String sesskey, String course, String replyto,
                                          String itemid, String attachments, String recipients,
                                          String subject, String content) throws IOException, InterruptedException;

    HttpResponse<String> getComposeFormBlank() throws IOException, InterruptedException;

    HttpResponse<String> postComposeAllCourses(String sesskey) throws IOException, InterruptedException;

    HttpResponse<String> postSendMessage(String sesskey, String course,
                                         java.util.List<String> recipientIds,
                                         String itemid, String attachments,
                                         String subject, String content) throws IOException, InterruptedException;

    HttpResponse<String> getCourseParticipants(String courseId) throws IOException, InterruptedException;

    HttpResponse<String> getUserProfile(String userId) throws IOException, InterruptedException;

    HttpResponse<String> getQuizModEdit(String cmid) throws IOException, InterruptedException;

    HttpResponse<String> getQuizEdit(String cmid) throws IOException, InterruptedException;

    HttpResponse<String> getQuestionBank(String cmid) throws IOException, InterruptedException;

    HttpResponse<String> getNewQuestionForm(String cmid, String courseId, String categoryId, String sesskey) throws IOException, InterruptedException;

    HttpResponse<String> getQuestionEditForm(String questionId, String cmid) throws IOException, InterruptedException;

    HttpResponse<String> postQuestionEdit(String questionId, String cmid, java.util.Map<String, String> params) throws IOException, InterruptedException;

    HttpResponse<String> postQuizSlotDelete(String slotId, String sesskey, String courseId, String quizId) throws IOException, InterruptedException;

    HttpResponse<String> postQuestionBankDeleteStep1(String cmid, String sesskey, java.util.List<String> questionIds) throws IOException, InterruptedException;

    HttpResponse<String> postQuestionBankDeleteStep2(String cmid, String sesskey, String confirm, String deleteSelected, String returnUrl, String courseId) throws IOException, InterruptedException;
}


