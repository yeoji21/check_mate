package checkmate.notification.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import checkmate.ControllerTest;
import checkmate.TestEntityFactory;
import checkmate.config.WithMockAuthUser;
import checkmate.notification.application.dto.response.NotificationAttributeInfo;
import checkmate.notification.application.dto.response.NotificationDetailInfo;
import checkmate.notification.application.dto.response.NotificationDetailResult;
import checkmate.notification.domain.Notification;
import checkmate.notification.domain.NotificationAttributeKey;
import checkmate.notification.domain.NotificationAttributes;
import checkmate.notification.domain.NotificationType;
import checkmate.notification.presentation.dto.NotificationAttributeInfoResult;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;
import org.springframework.restdocs.request.PathParametersSnippet;
import org.springframework.restdocs.request.RequestParametersSnippet;

class NotificationControllerTest extends ControllerTest {

    @WithMockAuthUser
    @Test
    @DisplayName("단건 알림 조회 API")
    void findNotificationInfo() throws Exception {
        NotificationAttributeInfo info = createNotificationAttributeInfo();
        given(notificationQueryService.findNotificationInfo(any(Long.class),
            any(Long.class))).willReturn(info);

        mockMvc.perform(get("/notifications/{notificationId}", 1L)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(objectMapper.writeValueAsString(info)))
            .andDo(document("notification-info",
                notificationIdPathParametersSnippet(),
                notificationInfoResponseFieldsSnippet()
            ));
    }

    @WithMockAuthUser
    @Test
    @DisplayName("목표 수행 완료 알림 조회 API")
    void goalCompleteNotifications() throws Exception {
        NotificationAttributeInfoResult result = new NotificationAttributeInfoResult(
            getNotificationDetailResponseList());
        given(notificationQueryService.findGoalCompleteNotifications(any(Long.class))).willReturn(
            result);

        mockMvc.perform(get("/notifications/goal-complete")
                .contentType(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(objectMapper.writeValueAsString(result)))
            .andDo(document("notification-goal-complete",
                completeGoalNotificationResponseFieldsSnippet()
            ));
    }

    @WithMockAuthUser
    @Test
    @DisplayName("알림 목록 조회 API")
    void findNotifications() throws Exception {
        NotificationDetailResult result = getNotificationDetailsResult();
        given(notificationQueryService.findNotificationDetails(any())).willReturn(result);

        mockMvc.perform(get("/notifications?cursorId=20&size=10")
                .contentType(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(objectMapper.writeValueAsString(result)))
            .andDo(document("notifications",
                notificationsRequestParametersSnippet(),
                notificationsResponseFieldsSnippet()
            ));
    }

    private ResponseFieldsSnippet notificationsResponseFieldsSnippet() {
        return responseFields(
            fieldWithPath("notifications[].notificationId").description("알림 ID"),
            fieldWithPath("notifications[].title").description("알림 타이틀"),
            fieldWithPath("notifications[].content").description("알림 내용"),
            fieldWithPath("notifications[].checked").description("알림 수신 여부"),
            fieldWithPath("notifications[].sendAt").description("알림 전송 날짜, 시간"),
            fieldWithPath("notifications[].type").description("알림 종류"),
            fieldWithPath("hasNext").description("조회할 수 있는 다음 페이지가 있는지 여부")
        );
    }

    private RequestParametersSnippet notificationsRequestParametersSnippet() {
        return requestParameters(
            parameterWithName("cursorId").description("조회를 위한 cursorID, 여기서는 notificationID를 의미함"),
            parameterWithName("size").optional().description("조회할 알림 개수")
        );
    }

    private NotificationDetailResult getNotificationDetailsResult() {
        return new NotificationDetailResult(
            List.of(getNotificationDetails(1L, NotificationType.INVITE_SEND),
                getNotificationDetails(2L, NotificationType.POST_UPLOAD)),
            true);
    }

    private NotificationDetailInfo getNotificationDetails(long notificationId,
        NotificationType type) {
        return NotificationDetailInfo.builder()
            .notificationId(notificationId)
            .title("title")
            .content("body")
            .checked(true)
            .sendAt(LocalDateTime.now())
            .type(type.name())
            .build();
    }

    private ResponseFieldsSnippet completeGoalNotificationResponseFieldsSnippet() {
        return responseFields(
            fieldWithPath("notifications[].title").type(JsonFieldType.STRING).description("알림 타이틀"),
            fieldWithPath("notifications[].content").type(JsonFieldType.STRING)
                .description("알림 내용"),
            fieldWithPath("notifications[].type").type(JsonFieldType.STRING).description("알림 종류"),
            fieldWithPath("notifications[].attributes").type(JsonFieldType.STRING)
                .description("해당 알림에 필요한 추가 데이터 - JSON 형식").optional()
        );
    }

    private ResponseFieldsSnippet notificationInfoResponseFieldsSnippet() {
        return responseFields(
            fieldWithPath("title").type(JsonFieldType.STRING).description("알림 타이틀"),
            fieldWithPath("content").type(JsonFieldType.STRING).description("알림 내용"),
            fieldWithPath("type").type(JsonFieldType.STRING).description("알림 종류"),
            fieldWithPath("attributes").type(JsonFieldType.STRING)
                .description("해당 알림에 필요한 추가 데이터 - JSON 형식").optional()
        );
    }

    private PathParametersSnippet notificationIdPathParametersSnippet() {
        return pathParameters(
            parameterWithName("notificationId").description("알림 ID"));
    }

    private NotificationAttributeInfo createNotificationAttributeInfo() {
        Notification notification = TestEntityFactory.notification(1L, 1L,
            NotificationType.POST_UPLOAD);
        return toNotificationInfo(notification);
    }

    private List<NotificationAttributeInfo> getNotificationDetailResponseList() {
        Notification notification = TestEntityFactory.notification(1L, 1L,
            NotificationType.COMPLETE_GOAL);
        return List.of(toNotificationInfo(notification), toNotificationInfo(notification));
    }

    private NotificationAttributeInfo toNotificationInfo(Notification notification) {
        return NotificationAttributeInfo.builder()
            .title(notification.getTitle())
            .content(notification.getContent())
            .type(notification.getType().toString())
            .attributes(new NotificationAttributes(
                Map.of(NotificationAttributeKey.GOAL_ID.getKey(), "value1",
                    NotificationAttributeKey.MATE_ID.getKey(), "value2")).toString())
            .build();
    }
}