package tools.ceac.ai.modules.campus.infrastructure.campus;

import tools.ceac.ai.modules.campus.application.port.out.CampusGateway;
import tools.ceac.ai.modules.campus.infrastructure.config.CampusProperties;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Infrastructure gateway for campus HTTP calls.
 */
@Component
public class CampusHttpGateway implements CampusGateway {
    private final CampusProperties properties;
    private final CampusSessionHttpClient sessionHttpClient;

    public CampusHttpGateway(CampusProperties properties, CampusSessionHttpClient sessionHttpClient) {
        this.properties = properties;
        this.sessionHttpClient = sessionHttpClient;
    }

    @Override
    public HttpResponse<String> getDashboard() throws IOException, InterruptedException {
        return sessionHttpClient.get(properties.dashboardUrl());
    }

    @Override
    public HttpResponse<String> getCourseState(String courseId, String sesskey) throws IOException, InterruptedException {
        String url = properties.baseUrl() + "/lib/ajax/service.php?sesskey=" + sesskey + "&info=core_courseformat_get_state";
        String body = "[{\"index\":0,\"methodname\":\"core_courseformat_get_state\",\"args\":{\"courseid\":" + courseId + "}}]";
        return sessionHttpClient.post(url, body);
    }

    @Override
    public HttpResponse<String> getAssignGradingPage(String assignId) throws IOException, InterruptedException {
        String url = properties.baseUrl() + "/mod/assign/view.php?id=" + assignId + "&action=grading";
        return sessionHttpClient.get(url);
    }

    @Override
    public HttpResponse<String> postAssignSaveOptions(String assignId, String contextId, String formUserId, String sesskey) throws IOException, InterruptedException {
        String url = properties.baseUrl() + "/mod/assign/view.php";
        String body = "contextid=" + contextId
                + "&id=" + assignId
                + "&userid=" + formUserId
                + "&action=saveoptions"
                + "&sesskey=" + sesskey
                + "&_qf__mod_assign_grading_options_form=1"
                + "&mform_isexpanded_id_general=1"
                + "&perpage=-1"
                + "&filter="
                + "&showonlyactiveenrol=1"
                + "&downloadasfolders=1";
        return sessionHttpClient.postForm(url, body);
    }

    @Override
    public HttpResponse<byte[]> downloadFile(String url) throws IOException, InterruptedException {
        return sessionHttpClient.getBytes(url);
    }

    @Override
    public HttpResponse<String> getGraderPage(String moduleId, String studentUserId) throws IOException, InterruptedException {
        String url = properties.baseUrl() + "/mod/assign/view.php?id=" + moduleId + "&rownum=0&action=grader&userid=" + studentUserId;
        return sessionHttpClient.get(url);
    }

    @Override
    public HttpResponse<String> getUnusedDraftItemId(String sesskey) throws IOException, InterruptedException {
        String url = properties.baseUrl() + "/lib/ajax/service.php?sesskey=" + sesskey + "&info=core_files_get_unused_draft_itemid";
        String body = "[{\"index\":0,\"methodname\":\"core_files_get_unused_draft_itemid\",\"args\":{}}]";
        return sessionHttpClient.post(url, body);
    }

    @Override
    public HttpResponse<String> submitGrade(String assignmentId, String studentUserId, String jsonFormData, String sesskey) throws IOException, InterruptedException {
        String url = properties.baseUrl() + "/lib/ajax/service.php?sesskey=" + sesskey + "&info=mod_assign_submit_grading_form";
        String escapedFormData = jsonFormData.replace("\\", "\\\\").replace("\"", "\\\"");
        String body = "[{\"index\":0,\"methodname\":\"mod_assign_submit_grading_form\","
                + "\"args\":{\"assignmentid\":\"" + assignmentId + "\","
                + "\"userid\":" + studentUserId + ","
                + "\"jsonformdata\":\"" + escapedFormData + "\"}}]";
        return sessionHttpClient.post(url, body);
    }

    @Override
    public HttpResponse<String> getSubmissionStatus(String assignmentId, String studentUserId, String sesskey) throws IOException, InterruptedException {
        String url = properties.baseUrl() + "/lib/ajax/service.php?sesskey=" + sesskey + "&info=mod_assign_get_submission_status";
        String body = "[{\"index\":0,\"methodname\":\"mod_assign_get_submission_status\","
                + "\"args\":{\"assignid\":" + assignmentId + ",\"userid\":" + studentUserId + ",\"groupid\":0}}]";
        return sessionHttpClient.post(url, body);
    }

    @Override
    public HttpResponse<String> getCoreFragment(String contextId, String userId, String sesskey) throws IOException, InterruptedException {
        String url = properties.baseUrl() + "/lib/ajax/service.php?sesskey=" + sesskey + "&info=core_get_fragment";
        String body = "[{\"index\":0,\"methodname\":\"core_get_fragment\","
                + "\"args\":{\"component\":\"mod_assign\",\"callback\":\"gradingpanel\","
                + "\"contextid\":" + contextId + ","
                + "\"args\":[{\"name\":\"userid\",\"value\":" + userId + "},"
                + "{\"name\":\"attemptnumber\",\"value\":-1},"
                + "{\"name\":\"jsonformdata\",\"value\":\"\\\"\\\"\"}]}}]";
        return sessionHttpClient.post(url, body);
    }

    @Override
    public HttpResponse<String> getQuizResults(String quizId, String sesskey) throws IOException, InterruptedException {
        String url = properties.baseUrl() + "/mod/quiz/report.php";
        String body = "id=" + quizId
                + "&mode=overview"
                + "&sesskey=" + sesskey
                + "&_qf__quiz_overview_settings_form=1"
                + "&attempts=enrolled_any"
                + "&stateinprogress=0&stateinprogress=1"
                + "&stateoverdue=0&stateoverdue=1"
                + "&statefinished=0&statefinished=1"
                + "&stateabandoned=0&stateabandoned=1"
                + "&pagesize=150"
                + "&slotmarks=1"
                + "&submitbutton=Mostrar+informe";
        return sessionHttpClient.postForm(url, body);
    }

    @Override
    public HttpResponse<String> getQuizAttemptReview(String attemptId) throws IOException, InterruptedException {
        String url = properties.baseUrl() + "/mod/quiz/review.php?attempt=" + attemptId;
        return sessionHttpClient.get(url);
    }

    @Override
    public HttpResponse<String> getQuizComment(String attemptId, String slot) throws IOException, InterruptedException {
        String url = properties.baseUrl() + "/mod/quiz/comment.php?attempt=" + attemptId + "&slot=" + slot;
        return sessionHttpClient.get(url);
    }

    @Override
    public HttpResponse<String> postQuizComment(
            String attemptId, String slot, String usageId,
            String sesskey, String sequencecheck,
            String itemid, String commentFormat,
            String mark, String maxMark,
            String minFraction, String maxFraction,
            String comentario) throws IOException, InterruptedException {
        String url = properties.baseUrl() + "/mod/quiz/comment.php";
        String prefix = "q" + usageId + ":" + slot + "_";
        String encodedComentario = URLEncoder.encode(comentario != null ? comentario : "", StandardCharsets.UTF_8);
        String body = prefix + "%3Asequencecheck=" + sequencecheck
                + "&" + prefix + "-comment=" + encodedComentario
                + "&" + prefix + "-comment%3Aitemid=" + itemid
                + "&" + prefix + "-commentformat=" + commentFormat
                + "&" + prefix + "-mark=" + mark
                + "&" + prefix + "-maxmark=" + maxMark
                + "&" + prefix + "%3Aminfraction=" + minFraction
                + "&" + prefix + "%3Amaxfraction=" + maxFraction
                + "&attempt=" + attemptId
                + "&slot=" + slot
                + "&slots=" + slot
                + "&sesskey=" + sesskey
                + "&submit=Guardar";
        return sessionHttpClient.postForm(url, body);
    }

    @Override
    public HttpResponse<String> getQuizUserOverrides(String cmid) throws IOException, InterruptedException {
        String url = properties.baseUrl() + "/mod/quiz/overrides.php?cmid=" + cmid + "&mode=user";
        return sessionHttpClient.get(url);
    }

    @Override
    public HttpResponse<String> getQuizOverrideEditForm(String cmid) throws IOException, InterruptedException {
        String url = properties.baseUrl() + "/mod/quiz/overrideedit.php?action=adduser&cmid=" + cmid;
        return sessionHttpClient.get(url);
    }

    @Override
    public HttpResponse<String> postQuizUserOverride(
            String cmid, String sesskey,
            String userId, String password,
            Integer timeopenDay, Integer timeopenMonth, Integer timeopenYear, Integer timeopenHour, Integer timeopenMinute,
            Integer timecloseDay, Integer timecloseMonth, Integer timecloseYear, Integer timecloseHour, Integer timecloseMinute,
            Long timelimitSeconds,
            Integer attempts) throws IOException, InterruptedException {
        String url = properties.baseUrl() + "/mod/quiz/overrideedit.php";
        StringBuilder body = new StringBuilder();
        body.append("action=adduser")
                .append("&cmid=").append(cmid)
                .append("&sesskey=").append(sesskey)
                .append("&_qf__mod_quiz_form_edit_override_form=1")
                .append("&mform_isexpanded_id_override=1")
                .append("&userid=").append(userId)
                .append("&password=").append(URLEncoder.encode(password != null ? password : "", StandardCharsets.UTF_8));

        if (timeopenDay != null) {
            body.append("&timeopen%5Benabled%5D=1")
                    .append("&timeopen%5Bday%5D=").append(timeopenDay)
                    .append("&timeopen%5Bmonth%5D=").append(timeopenMonth)
                    .append("&timeopen%5Byear%5D=").append(timeopenYear)
                    .append("&timeopen%5Bhour%5D=").append(timeopenHour)
                    .append("&timeopen%5Bminute%5D=").append(timeopenMinute);
        }

        if (timecloseDay != null) {
            body.append("&timeclose%5Benabled%5D=1")
                    .append("&timeclose%5Bday%5D=").append(timecloseDay)
                    .append("&timeclose%5Bmonth%5D=").append(timecloseMonth)
                    .append("&timeclose%5Byear%5D=").append(timecloseYear)
                    .append("&timeclose%5Bhour%5D=").append(timecloseHour)
                    .append("&timeclose%5Bminute%5D=").append(timecloseMinute);
        }

        if (timelimitSeconds != null) {
            body.append("&timelimit%5Bnumber%5D=").append(timelimitSeconds)
                    .append("&timelimit%5Btimeunit%5D=1")
                    .append("&timelimit%5Benabled%5D=1");
        }

        if (attempts != null) {
            body.append("&attempts=").append(attempts);
        }

        body.append("&submitbutton=Guardar");
        return sessionHttpClient.postForm(url, body.toString());
    }

    @Override
    public HttpResponse<String> getQuizOverrideDeleteForm(String overrideId) throws IOException, InterruptedException {
        String url = properties.baseUrl() + "/mod/quiz/overridedelete.php?id=" + overrideId;
        return sessionHttpClient.get(url);
    }

    @Override
    public HttpResponse<String> postQuizOverrideDelete(String overrideId, String sesskey) throws IOException, InterruptedException {
        String url = properties.baseUrl() + "/mod/quiz/overridedelete.php";
        String body = "id=" + overrideId + "&confirm=1&sesskey=" + sesskey;
        return sessionHttpClient.postForm(url, body);
    }

    @Override
    public HttpResponse<String> getConversations(String userId, String sesskey, int type, int limitNum, int limitFrom,
                                                  boolean favourites, boolean mergeSelf) throws IOException, InterruptedException {
        String url = properties.baseUrl() + "/lib/ajax/service.php?sesskey=" + sesskey
                + "&info=core_message_get_conversations";
        String body = "[{\"index\":0,\"methodname\":\"core_message_get_conversations\","
                + "\"args\":{\"userid\":" + userId
                + ",\"type\":" + type
                + ",\"limitnum\":" + limitNum
                + ",\"limitfrom\":" + limitFrom
                + ",\"favourites\":" + favourites
                + ",\"mergeself\":" + mergeSelf + "}}]";
        return sessionHttpClient.post(url, body);
    }

    @Override
    public HttpResponse<String> getConversationMessages(String currentUserId, String convId, String sesskey,
                                                         boolean newest, int limitNum, int limitFrom) throws IOException, InterruptedException {
        String url = properties.baseUrl() + "/lib/ajax/service.php?sesskey=" + sesskey
                + "&info=core_message_get_conversation_messages";
        String body = "[{\"index\":0,\"methodname\":\"core_message_get_conversation_messages\","
                + "\"args\":{\"currentuserid\":" + currentUserId
                + ",\"convid\":" + convId
                + ",\"newest\":" + newest
                + ",\"limitnum\":" + limitNum
                + ",\"limitfrom\":" + limitFrom + "}}]";
        return sessionHttpClient.post(url, body);
    }

    @Override
    public HttpResponse<String> markConversationAsRead(String userId, String conversationId, String sesskey) throws IOException, InterruptedException {
        String url = properties.baseUrl() + "/lib/ajax/service.php?sesskey=" + sesskey
                + "&info=core_message_mark_all_conversation_messages_as_read";
        String body = "[{\"index\":0,\"methodname\":\"core_message_mark_all_conversation_messages_as_read\","
                + "\"args\":{\"userid\":" + userId
                + ",\"conversationid\":" + conversationId + "}}]";
        return sessionHttpClient.post(url, body);
    }

    @Override
    public HttpResponse<String> getComposeForm(String studentUserId, String myUserId,
                                                String messageTimestamp, String messageId) throws IOException, InterruptedException {
        String url = properties.baseUrl() + "/blocks/itop_mailbox/compose.php"
                + "?message=" + studentUserId + "_" + myUserId + "_" + messageTimestamp
                + "&action=reply&messageid=" + messageId;
        return sessionHttpClient.get(url);
    }

    @Override
    public HttpResponse<String> postComposeReply(String sesskey, String course, String replyto,
                                                  String itemid, String attachments, String recipients,
                                                  String subject, String content) throws IOException, InterruptedException {
        String url = properties.baseUrl() + "/blocks/itop_mailbox/compose.php";
        String encodedSubject = URLEncoder.encode(subject != null ? subject : "", StandardCharsets.UTF_8);
        String encodedContent = URLEncoder.encode(content != null ? content : "", StandardCharsets.UTF_8);
        String body = "replyto=user-" + replyto
                + "&courses%5B" + course + "%5D=" + course
                + "&course=" + course
                + "&filter=1"
                + "&action=reply"
                + "&sesskey=" + sesskey
                + "&_qf__itop_mailbox_compose_form=1"
                + "&mform_isexpanded_id_general=1"
                + "&recipients=" + recipients
                + "&subject=" + encodedSubject
                + "&content%5Btext%5D=" + encodedContent
                + "&content%5Bformat%5D=1"
                + "&content%5Bitemid%5D=" + itemid
                + "&attachments=" + attachments
                + "&submitbutton=Enviar";
        return sessionHttpClient.postForm(url, body);
    }

    @Override
    public HttpResponse<String> getComposeFormBlank() throws IOException, InterruptedException {
        String url = properties.baseUrl() + "/blocks/itop_mailbox/compose.php";
        return sessionHttpClient.get(url);
    }

    @Override
    public HttpResponse<String> postSendMessage(String sesskey, String course,
                                                 java.util.List<String> recipientIds,
                                                 String itemid, String attachments,
                                                 String subject, String content) throws IOException, InterruptedException {
        String url = properties.baseUrl() + "/blocks/itop_mailbox/compose.php";
        String encodedSubject = URLEncoder.encode(subject != null ? subject : "", StandardCharsets.UTF_8);
        String encodedContent = URLEncoder.encode(content != null ? content : "", StandardCharsets.UTF_8);

        StringBuilder body = new StringBuilder();
        body.append("courses%5B").append(course).append("%5D=").append(course)
                .append("&course=").append(course)
                .append("&filter=1")
                .append("&sesskey=").append(sesskey)
                .append("&_qf__itop_mailbox_compose_form=1")
                .append("&mform_isexpanded_id_general=1");

        for (String recipientId : recipientIds) {
            body.append("&recipients%5B%5D=user-").append(recipientId);
        }

        body.append("&subject=").append(encodedSubject)
                .append("&content%5Btext%5D=").append(encodedContent)
                .append("&content%5Bformat%5D=1")
                .append("&content%5Bitemid%5D=").append(itemid)
                .append("&attachments=").append(attachments)
                .append("&submitbutton=Enviar");

        return sessionHttpClient.postForm(url, body.toString());
    }

    @Override
    public HttpResponse<String> postComposeAllCourses(String sesskey) throws IOException, InterruptedException {
        String url = properties.baseUrl() + "/blocks/itop_mailbox/compose.php";
        String body = "method=1"
                + "&filter=1"
                + "&sesskey=" + sesskey
                + "&_qf__itop_mailbox_course_form=1"
                + "&allcourses=1"
                + "&submitbutton=Ir";
        return sessionHttpClient.postForm(url, body);
    }

    @Override
    public HttpResponse<String> getCourseParticipants(String courseId) throws IOException, InterruptedException {
        String url = properties.baseUrl() + "/user/index.php?id=" + courseId + "&perpage=5000";
        return sessionHttpClient.get(url);
    }

    @Override
    public HttpResponse<String> getUserProfile(String userId) throws IOException, InterruptedException {
        String url = properties.baseUrl() + "/user/profile.php?id=" + userId;
        return sessionHttpClient.get(url);
    }

    @Override
    public HttpResponse<String> getQuizModEdit(String cmid) throws IOException, InterruptedException {
        String url = properties.baseUrl() + "/course/modedit.php?update=" + cmid + "&return=1";
        return sessionHttpClient.get(url);
    }

    @Override
    public HttpResponse<String> getQuizEdit(String cmid) throws IOException, InterruptedException {
        String url = properties.baseUrl() + "/mod/quiz/edit.php?cmid=" + cmid;
        return sessionHttpClient.get(url);
    }

    @Override
    public HttpResponse<String> getQuestionBank(String cmid) throws IOException, InterruptedException {
        String url = properties.baseUrl() + "/question/edit.php?cmid=" + cmid;
        return sessionHttpClient.get(url);
    }

    @Override
    public HttpResponse<String> getNewQuestionForm(String cmid, String courseId, String categoryId, String sesskey)
            throws IOException, InterruptedException {
        String returnUrl = URLEncoder.encode("/mod/quiz/edit.php?cmid=" + cmid + "&addonpage=0", StandardCharsets.UTF_8);
        String url = properties.baseUrl()
                + "/question/bank/editquestion/question.php"
                + "?courseid=" + courseId
                + "&sesskey=" + sesskey
                + "&qtype=multichoice"
                + "&returnurl=" + returnUrl
                + "&cmid=" + cmid
                + "&category=" + categoryId
                + "&addonpage=0"
                + "&appendqnumstring=addquestion";
        return sessionHttpClient.get(url);
    }

    @Override
    public HttpResponse<String> getQuestionEditForm(String questionId, String cmid) throws IOException, InterruptedException {
        String returnUrl = URLEncoder.encode("/mod/quiz/edit.php?cmid=" + cmid, StandardCharsets.UTF_8);
        String url = properties.baseUrl()
                + "/question/bank/editquestion/question.php"
                + "?returnurl=" + returnUrl
                + "&cmid=" + cmid
                + "&id=" + questionId;
        return sessionHttpClient.get(url);
    }

    @Override
    public HttpResponse<String> postQuestionEdit(String questionId, String cmid, Map<String, String> params) throws IOException, InterruptedException {
        String url = properties.baseUrl() + "/question/bank/editquestion/question.php";
        StringBuilder body = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (body.length() > 0) body.append('&');
            body.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8))
                .append('=')
                .append(URLEncoder.encode(entry.getValue() != null ? entry.getValue() : "", StandardCharsets.UTF_8));
        }
        return sessionHttpClient.postForm(url, body.toString());
    }

    @Override
    public HttpResponse<String> postQuizSlotDelete(String slotId, String sesskey, String courseId, String quizId) throws IOException, InterruptedException {
        String url = properties.baseUrl() + "/mod/quiz/edit_rest.php";
        String body = "class=resource"
                + "&action=DELETE"
                + "&id=" + URLEncoder.encode(slotId, StandardCharsets.UTF_8)
                + "&sesskey=" + URLEncoder.encode(sesskey, StandardCharsets.UTF_8)
                + "&courseid=" + URLEncoder.encode(courseId, StandardCharsets.UTF_8)
                + "&quizid=" + URLEncoder.encode(quizId, StandardCharsets.UTF_8);
        return sessionHttpClient.postForm(url, body);
    }

    @Override
    public HttpResponse<String> postQuestionBankDeleteStep1(String cmid, String sesskey, java.util.List<String> questionIds)
            throws IOException, InterruptedException {
        String url = properties.baseUrl() + "/question/bank/deletequestion/delete.php"
                + "?cmid=" + URLEncoder.encode(cmid, StandardCharsets.UTF_8)
                + "&deleteall=1";
        StringBuilder body = new StringBuilder();
        body.append("sesskey=").append(URLEncoder.encode(sesskey, StandardCharsets.UTF_8));
        body.append("&cmid=").append(URLEncoder.encode(cmid, StandardCharsets.UTF_8));
        body.append("&deleteall=1");
        for (String id : questionIds) {
            body.append("&q").append(URLEncoder.encode(id, StandardCharsets.UTF_8)).append("=1");
            body.append("&question_status_dropdown=ready");
        }
        body.append("&deleteselected=Borrar");
        return sessionHttpClient.postForm(url, body.toString());
    }

    @Override
    public HttpResponse<String> postQuestionBankDeleteStep2(String cmid, String sesskey, String confirm,
                                                             String deleteSelected, String returnUrl, String courseId)
            throws IOException, InterruptedException {
        String url = properties.baseUrl() + "/question/bank/deletequestion/delete.php";
        String body = "deleteselected=" + URLEncoder.encode(deleteSelected, StandardCharsets.UTF_8)
                + "&deleteall=1"
                + "&confirm=" + URLEncoder.encode(confirm, StandardCharsets.UTF_8)
                + "&sesskey=" + URLEncoder.encode(sesskey, StandardCharsets.UTF_8)
                + "&returnurl=" + URLEncoder.encode(returnUrl, StandardCharsets.UTF_8)
                + "&cmid=" + URLEncoder.encode(cmid, StandardCharsets.UTF_8)
                + "&courseid=" + URLEncoder.encode(courseId, StandardCharsets.UTF_8);
        return sessionHttpClient.postForm(url, body);
    }

    @Override
    public boolean looksAuthenticated(HttpResponse<String> response) {
        if (response == null || response.uri() == null) {
            return false;
        }
        String finalUrl = response.uri().toString();
        return finalUrl.startsWith(properties.baseUrl())
                && !finalUrl.equalsIgnoreCase(properties.loginUrl())
                && !finalUrl.contains("/login/index.php");
    }
}


