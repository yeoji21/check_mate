package checkmate.goal.presentation;

import checkmate.ControllerTest;
import checkmate.config.WithMockAuthUser;
import checkmate.goal.application.dto.TeamMateCommandMapper;
import checkmate.goal.application.dto.response.TeamMateCalendarInfo;
import checkmate.goal.application.dto.response.TeamMateInviteReplyResult;
import checkmate.goal.presentation.dto.request.TeamMateInviteDto;
import checkmate.goal.presentation.dto.request.TeamMateInviteReplyDto;
import org.junit.jupiter.api.Test;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.payload.RequestFieldsSnippet;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;

import java.time.LocalDate;

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
    private TeamMateCommandMapper commandMapper = TeamMateCommandMapper.INSTANCE;

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
    @Test
    void 팀원_초대_응답_테스트() throws Exception{
        TeamMateInviteReplyDto request = new TeamMateInviteReplyDto(1L, 1L, true);
        TeamMateInviteReplyResult result = commandMapper.toInviteReplyResult(1L);

        given(teamMateCommandService.applyInviteReply(any())).willReturn(result);

        mockMvc.perform(RestDocumentationRequestBuilders.patch("/mate")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(result)))
                .andDo(document("invite-reply",
                        getInviteReplyRequestFieldsSnippet(),
                        getInviteReplyResponseFieldsSnippet()
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
        TeamMateCalendarInfo calendarInfo = TeamMateCalendarInfo.builder()
                .startDate(LocalDate.now())
                .goalCalendar("0111000011100001")
                .teamMateCalendar("0100000000000000")
                .build();

        given(teamMateQueryService.getCalenderInfo(1L)).willReturn(calendarInfo);

        mockMvc.perform(RestDocumentationRequestBuilders.get("/mate/{teamMateId}/calendar", 1L)
                        .with(csrf())
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(calendarInfo)))
                .andDo(document("teamMate-calendar",
                        pathParameters(parameterWithName("teamMateId").description("teamMateId")),
                        getTeamMateCalendarResponseFieldsSnippet()
                ));
    }

    private ResponseFieldsSnippet getTeamMateCalendarResponseFieldsSnippet() {
        return responseFields(
                fieldWithPath("startDate").description("목표 수행 시작일"),
                fieldWithPath("goalCalendar").description("목표 수행 기간"),
                fieldWithPath("teamMateCalendar").description("팀원의 목표 수행 기간")
        );
    }

    private ResponseFieldsSnippet getInviteReplyResponseFieldsSnippet() {
        return responseFields(fieldWithPath("goalId").type(JsonFieldType.NUMBER).description("목표 ID"));
    }

    private RequestFieldsSnippet getInviteReplyRequestFieldsSnippet() {
        return requestFields(
                fieldWithPath("teamMateId").type(JsonFieldType.NUMBER).description("teamMateId"),
                fieldWithPath("notificationId").type(JsonFieldType.NUMBER).description("notificationId"),
                fieldWithPath("accept").type(JsonFieldType.BOOLEAN).description("수락 여부")
        );
    }

    private RequestFieldsSnippet getInviteTeamMateRequest() {
        return requestFields(
                fieldWithPath("goalId").type(JsonFieldType.NUMBER).description("goalId"),
                fieldWithPath("inviteeNickname").type(JsonFieldType.STRING).description("초대할 사람의 닉네임")
        );
    }
}