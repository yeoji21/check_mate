package checkmate.notification.presentation;

import checkmate.ControllerTest;
import checkmate.TestEntityFactory;
import checkmate.config.WithMockAuthUser;
import checkmate.goal.domain.Goal;
import checkmate.goal.domain.TeamMate;
import checkmate.notification.application.dto.NotificationQueryMapper;
import checkmate.notification.application.dto.response.NotificationDetails;
import checkmate.notification.application.dto.response.NotificationDetailsResult;
import checkmate.notification.application.dto.response.NotificationInfo;
import checkmate.notification.domain.Notification;
import checkmate.notification.domain.NotificationType;
import checkmate.notification.domain.factory.CompleteGoalNotificationFactory;
import checkmate.notification.domain.factory.dto.CompleteGoalNotificationDto;
import checkmate.notification.presentation.dto.response.NotificationInfosResult;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class NotificationControllerTest extends ControllerTest {
    @WithMockAuthUser
    @Test
    void 목표수행_완료_알림_조회_테스트() throws Exception{
        List<NotificationInfo> notifications = getNotificationDetailResponseList();
        NotificationInfosResult response = new NotificationInfosResult(notifications);

        given(notificationQueryService.findGoalCompleteNotifications(any(Long.class))).willReturn(notifications);

        mockMvc.perform(get("/notification/goal-complete")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)))
                .andDo(document("find-goal-complete",
                        responseFields(
                                fieldWithPath("notifications[].title").type(JsonFieldType.STRING).description("푸쉬 알림 타이틀"),
                                fieldWithPath("notifications[].body").type(JsonFieldType.STRING).description("푸쉬 알림 내용"),
                                fieldWithPath("notifications[].type").type(JsonFieldType.STRING).description("푸쉬 알림 종류"),
                                fieldWithPath("notifications[].attributes").type(JsonFieldType.STRING).description("해당 알림에 필요한 추가 데이터 - JSON 형식").optional()
                        )
                ));
    }

    @WithMockAuthUser
    @Test
    void 유저별_알림_목록_조회_테스트() throws Exception{
        NotificationDetailsResult result = new NotificationDetailsResult(
                List.of(NotificationDetails.builder()
                                .notificationId(1L)
                                .title("title1")
                                .body("body1")
                                .checked(true)
                                .sendAt(LocalDateTime.now())
                                .type("INVITE_GOAL")
                                .build(),
                        NotificationDetails.builder()
                                .notificationId(2L)
                                .title("title2")
                                .body("body2")
                                .checked(true)
                                .sendAt(LocalDateTime.now())
                                .type("INVITE_GOAL")
                                .build()),
                true);

        given(notificationQueryService.findNotificationDetails(any())).willReturn(result);

        mockMvc.perform(get("/notification?cursorId=20")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(result)))
                .andDo(document("find-notifications",
                        requestParameters(
                                parameterWithName("cursorId").description("요청할 다음 cursorID. 여기서는 notificationID를 의미함"),
                                parameterWithName("size").optional().description("조회할 알림 개수")
                        ),
                        responseFields(
                                fieldWithPath("notificationDetails[].notificationId").description("푸쉬 알림의 notificationId"),
                                fieldWithPath("notificationDetails[].title").description("푸쉬 알림 타이틀"),
                                fieldWithPath("notificationDetails[].body").description("푸쉬 알림 내용"),
                                fieldWithPath("notificationDetails[].checked").description("푸쉬 알림 수신 여부"),
                                fieldWithPath("notificationDetails[].sendAt").description("푸쉬 알림 전송 날짜, 시간"),
                                fieldWithPath("notificationDetails[].type").description("푸쉬 알림 종류"),
                                fieldWithPath("hasNext").description("조회할 수 있는 다음 페이지가 있는지 여부")
                        )
                ));
    }

    @WithMockAuthUser
    @Test
    void 단건_알림_조회_테스트() throws Exception{
        Notification notification = TestEntityFactory.notification(1L, 1L, NotificationType.POST_UPLOAD);
        NotificationInfo responseDto = toNotificationInfo(notification);

        given(notificationQueryService.findNotificationInfo(any(Long.class), any(Long.class))).willReturn(responseDto);

        mockMvc.perform(get("/notification/{notificationId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(responseDto)))
                .andDo(document("read-notification",
                        pathParameters(
                                parameterWithName("notificationId").description("notificationId")),
                        responseFields(
                                fieldWithPath("title").type(JsonFieldType.STRING).description("푸쉬 알림 타이틀"),
                                fieldWithPath("body").type(JsonFieldType.STRING).description("푸쉬 알림 내용"),
                                fieldWithPath("type").type(JsonFieldType.STRING).description("푸쉬 알림 종류"),
                                fieldWithPath("attributes").type(JsonFieldType.STRING).description("해당 알림에 필요한 추가 데이터 - JSON 형식").optional()
                        )
                ));
    }

    private List<NotificationInfo> getNotificationDetailResponseList() {
        Goal testGoal = TestEntityFactory.goal(1L, "testGoal");
        TeamMate teamMate = testGoal.join(TestEntityFactory.user(1L, "user"));

        CompleteGoalNotificationDto dto = CompleteGoalNotificationDto.builder()
                .userId(teamMate.getUserId())
                .goalTitle(teamMate.getGoal().getTitle())
                .goalId(teamMate.getGoal().getId())
                .build();

        Notification notification = new CompleteGoalNotificationFactory().generate(dto);
        return List.of(toNotificationInfo(notification), toNotificationInfo(notification));
    }

    private NotificationInfo toNotificationInfo(Notification notification) {
        return NotificationQueryMapper.INSTANCE.toInfo(notification);
    }
}