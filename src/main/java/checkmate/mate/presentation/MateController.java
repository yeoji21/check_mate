package checkmate.mate.presentation;

import checkmate.common.interceptor.GoalIdRoute;
import checkmate.common.interceptor.GoalMember;
import checkmate.config.auth.JwtUserDetails;
import checkmate.mate.application.MateCommandService;
import checkmate.mate.application.MateQueryService;
import checkmate.mate.application.dto.response.GoalDetailResult;
import checkmate.mate.application.dto.response.GoalHistoryInfoResult;
import checkmate.mate.application.dto.response.MateAcceptResult;
import checkmate.mate.application.dto.response.MateScheduleInfo;
import checkmate.mate.presentation.dto.MateDtoMapper;
import checkmate.mate.presentation.dto.MateInviteDto;
import checkmate.mate.presentation.dto.MateInviteReplyDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RequiredArgsConstructor
@Slf4j
@RestController
public class MateController {
    private final MateCommandService mateCommandService;
    private final MateQueryService mateQueryService;
    private final MateDtoMapper mapper;

    @GoalMember(GoalIdRoute.PATH_VARIABLE)
    @GetMapping("/goal/detail/{goalId}")
    public GoalDetailResult goalDetailResultFind(@PathVariable long goalId,
                                                 @AuthenticationPrincipal JwtUserDetails details) {
        return mateQueryService.findGoalDetailResult(goalId, details.getUserId());
    }

    @GetMapping("/goal/history")
    public GoalHistoryInfoResult successGoalHistoryFind(@AuthenticationPrincipal JwtUserDetails details) {
        return mateQueryService.findHistoryGoalInfo(details.getUserId());
    }

    @GoalMember(GoalIdRoute.REQUEST_BODY)
    @PostMapping("/mate")
    public void inviteToGoal(@RequestBody @Valid MateInviteDto inviteDto,
                             @AuthenticationPrincipal JwtUserDetails principal) {
        mateCommandService.inviteMate(mapper.toCommand(inviteDto, principal.getUserId()));
    }

    @PatchMapping("/mate/accept")
    public MateAcceptResult inviteAccept(@RequestBody MateInviteReplyDto dto,
                                         @AuthenticationPrincipal JwtUserDetails principal) {
        return mateCommandService.inviteAccept(mapper.toCommand(dto, principal.getUserId()));
    }

    @PatchMapping("/mate/reject")
    public void inviteReject(@RequestBody MateInviteReplyDto dto,
                             @AuthenticationPrincipal JwtUserDetails principal) {
        mateCommandService.inviteReject(mapper.toCommand(dto, principal.getUserId()));
    }

    @GetMapping("/mate/{mateId}/calendar")
    public MateScheduleInfo teamMateGoalCalender(@PathVariable long mateId) {
        return mateQueryService.getCalenderInfo(mateId);
    }

    @GetMapping("/mate/{mateId}/progress")
    public double progressPercent(@PathVariable Long mateId) {
        return mateQueryService.getProgressPercent(mateId);
    }
}
