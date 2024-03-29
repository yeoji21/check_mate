package checkmate.mate.presentation;

import checkmate.common.interceptor.GoalId;
import checkmate.common.interceptor.GoalMember;
import checkmate.config.auth.JwtUserDetails;
import checkmate.mate.application.MateCommandService;
import checkmate.mate.application.MateQueryService;
import checkmate.mate.application.dto.response.MateAcceptResult;
import checkmate.mate.application.dto.response.MateScheduleInfo;
import checkmate.mate.application.dto.response.SpecifiedGoalDetailInfo;
import checkmate.mate.presentation.dto.MateDtoMapper;
import checkmate.mate.presentation.dto.MateInviteDto;
import checkmate.mate.presentation.dto.MateInviteReplyDto;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@Slf4j
@RestController
public class MateController {

    private final MateCommandService mateCommandService;
    private final MateQueryService mateQueryService;
    private final MateDtoMapper mapper;

    @GoalMember(GoalId.PATH_VARIABLE)
    @GetMapping("/goals/{goalId}/detail")
    public SpecifiedGoalDetailInfo findSpecifiedGoalDetailInfo(@PathVariable long goalId,
        @AuthenticationPrincipal JwtUserDetails details) {
        return mateQueryService.findSpecifiedGoalDetailInfo(goalId, details.getUserId());
    }

    @GoalMember(GoalId.PATH_VARIABLE)
    @PostMapping("/goals/{goalId}/mates")
    public void inviteToGoal(@PathVariable long goalId,
        @RequestBody @Valid MateInviteDto inviteDto,
        @AuthenticationPrincipal JwtUserDetails principal) {
        mateCommandService.sendInvite(mapper.toCommand(goalId, principal.getUserId(), inviteDto));
    }

    @PatchMapping("/mates/accept")
    public MateAcceptResult inviteAccept(@RequestBody MateInviteReplyDto dto,
        @AuthenticationPrincipal JwtUserDetails principal) {
        return mateCommandService.acceptInvite(mapper.toCommand(dto, principal.getUserId()));
    }

    @PatchMapping("/mates/reject")
    public void inviteReject(@RequestBody MateInviteReplyDto dto,
        @AuthenticationPrincipal JwtUserDetails principal) {
        mateCommandService.rejectInvite(mapper.toCommand(dto, principal.getUserId()));
    }

    @GetMapping("/mates/{mateId}/calendar")
    public MateScheduleInfo findMateCalender(@PathVariable long mateId) {
        return mateQueryService.findCalenderInfo(mateId);
    }
}
