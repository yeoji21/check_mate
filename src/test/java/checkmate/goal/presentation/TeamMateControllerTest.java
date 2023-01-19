package checkmate.goal.presentation;

import checkmate.ControllerTest;
import checkmate.config.WithMockAuthUser;
import checkmate.goal.application.dto.response.TeamMateAcceptResult;
import checkmate.goal.application.dto.response.TeamMateScheduleInfo;
import checkmate.goal.presentation.dto.request.TeamMateInviteReplyDto;
import checkmate.goal.presentation.dto.request.TeamMateInviteDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.payload.RequestFieldsSnippet;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TeamMateControllerTest extends ControllerTest {
    @WithMockAuthUser
    @Test
    void 팀원_초대_테스트() throws Exception {
        TeamMateInviteDto request = new TeamMateInviteDto(1L, "yeoz1");

        mockMvc.perform(RestDocumentationRequestBuilders.post("/mate")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(document("invite-team mate", getInviteTeamMateRequest()));
        verify(teamMateCommandService).inviteTeamMate(any());
    }

    @WithMockAuthUser
    @Test @DisplayName("초대 응답 수락")
    void inviteAccept() throws Exception{
        TeamMateInviteReplyDto dto = new TeamMateInviteReplyDto(1L);
        TeamMateAcceptResult result = new TeamMateAcceptResult(1L, 1L);

        given(teamMateCommandService.inviteAccept(any())).willReturn(result);

        mockMvc.perform(RestDocumentationRequestBuilders.patch("/mate/accept")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(result)))
                .andDo(document("invite-accept",
                        requestFields(
                                fieldWithPath("notificationId").type(JsonFieldType.NUMBER).description("notificationId")
                        ),
                        responseFields(
                                fieldWithPath("goalId").type(JsonFieldType.NUMBER).description("goalId"),
                                fieldWithPath("teamMateId").type(JsonFieldType.NUMBER).description("teamMateId")
                        )));
    }

    @WithMockAuthUser
    @Test @DisplayName("초대 응답 거절")
    void inviteReject() throws Exception{
        TeamMateInviteReplyDto request = new TeamMateInviteReplyDto(1L);

        mockMvc.perform(RestDocumentationRequestBuilders.patch("/mate/reject")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(document("invite-reject",
                        requestFields(
                                fieldWithPath("notificationId").type(JsonFieldType.NUMBER).description("notificationId")
                        )
                ));
    }

    @WithMockAuthUser
    @Test
    void 목표_진행률_조회_테스트() throws Exception{
        double result = 20.0;
        given(teamMateQueryService.getProgressPercent(1L)).willReturn(result);

        mockMvc.perform(RestDocumentationRequestBuilders.get("/mate/{teamMateId}/progress", 1L)
                        .with(csrf())
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(String.valueOf(result)))
                .andDo(document("progress-percent",
                        pathParameters(parameterWithName("teamMateId").description("teamMateId"))
                ));
    }

    @WithMockAuthUser
    @Test
    void 팀원의_목표_수행_캘린더_조회() throws Exception{
        TeamMateScheduleInfo calendarInfo = TeamMateScheduleInfo.builder()
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(10))
                .weekDays(1111111)
                .uploadedDates(List.of(LocalDate.now()))
                .build();

        given(teamMateQueryService.getCalenderInfo(1L)).willReturn(calendarInfo);

        mockMvc.perform(RestDocumentationRequestBuilders.get("/mate/{teamMateId}/calendar", 1L)
                        .with(csrf())
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(calendarInfo)))
                .andDo(document("teamMate-calendar",
                        pathParameters(parameterWithName("teamMateId").description("teamMateId")),
                        teamMateCalendarResponseFieldsSnippet()
                ));
    }

    private ResponseFieldsSnippet teamMateCalendarResponseFieldsSnippet() {
        return responseFields(
                fieldWithPath("startDate").type(JsonFieldType.STRING).description("목표 수행 시작일"),
                fieldWithPath("endDate").type(JsonFieldType.STRING).description("목표 수행 종료일"),
                fieldWithPath("goalSchedule").type(JsonFieldType.STRING).description("목표 수행 일정"),
                fieldWithPath("teamMateSchedule").type(JsonFieldType.STRING).description("팀원의 목표 수행 일정")
        );
    }

    private RequestFieldsSnippet getInviteTeamMateRequest() {
        return requestFields(
                fieldWithPath("goalId").type(JsonFieldType.NUMBER).description("goalId"),
                fieldWithPath("inviteeNickname").type(JsonFieldType.STRING).description("초대할 사람의 닉네임")
        );
    }
}