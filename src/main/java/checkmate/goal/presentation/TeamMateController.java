package checkmate.goal.presentation;

import checkmate.common.interceptor.GoalIdRoute;
import checkmate.common.interceptor.GoalMember;
import checkmate.config.auth.JwtUserDetails;
import checkmate.goal.application.TeamMateCommandService;
import checkmate.goal.application.TeamMateQueryService;
import checkmate.goal.application.dto.response.GoalDetailResult;
import checkmate.goal.application.dto.response.GoalHistoryInfoResult;
import checkmate.goal.application.dto.response.TeamMateAcceptResult;
import checkmate.goal.application.dto.response.TeamMateScheduleInfo;
import checkmate.goal.presentation.dto.TeamMateDtoMapper;
import checkmate.goal.presentation.dto.request.TeamMateInviteDto;
import checkmate.goal.presentation.dto.request.TeamMateInviteReplyDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RequiredArgsConstructor
@Slf4j
@RestController
public class TeamMateController {
    private final TeamMateCommandService teamMateCommandService;
    private final TeamMateQueryService teamMateQueryService;
    private final TeamMateDtoMapper mapper;

    @GoalMember(GoalIdRoute.PATH_VARIABLE)
    @GetMapping("/goal/detail/{goalId}")
    public GoalDetailResult goalDetailResultFind(@PathVariable long goalId,
                                                 @AuthenticationPrincipal JwtUserDetails details) {
        return teamMateQueryService.findGoalDetailResult(goalId, details.getUserId());
    }

    @GetMapping("/goal/history")
    public GoalHistoryInfoResult successGoalHistoryFind(@AuthenticationPrincipal JwtUserDetails details) {
        return teamMateQueryService.findHistoryGoalInfo(details.getUserId());
    }

    @GoalMember(GoalIdRoute.REQUEST_BODY)
    @PostMapping("/mate")
    public void inviteToGoal(@RequestBody @Valid TeamMateInviteDto inviteDto,
                             @AuthenticationPrincipal JwtUserDetails principal) {
        teamMateCommandService.inviteTeamMate(mapper.toCommand(inviteDto, principal.getUserId()));
    }

    @PatchMapping("/mate/accept")
    public TeamMateAcceptResult inviteAccept(@RequestBody TeamMateInviteReplyDto dto,
                                             @AuthenticationPrincipal JwtUserDetails principal) {
        return teamMateCommandService.inviteAccept(mapper.toCommand(dto, principal.getUserId()));
    }

    @PatchMapping("/mate/reject")
    public void inviteReject(@RequestBody TeamMateInviteReplyDto dto,
                             @AuthenticationPrincipal JwtUserDetails principal) {
        teamMateCommandService.inviteReject(mapper.toCommand(dto, principal.getUserId()));
    }

    @GetMapping("/mate/{teamMateId}/calendar")
    public TeamMateScheduleInfo teamMateGoalCalender(@PathVariable long teamMateId) {
        return teamMateQueryService.getCalenderInfo(teamMateId);
    }

    @GetMapping("/mate/{teamMateId}/progress")
    public double progressPercent(@PathVariable Long teamMateId) {
        return teamMateQueryService.getProgressPercent(teamMateId);
    }
}
