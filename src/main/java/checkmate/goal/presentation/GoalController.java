package checkmate.goal.presentation;

import checkmate.common.interceptor.GoalId;
import checkmate.common.interceptor.GoalMember;
import checkmate.config.auth.JwtUserDetails;
import checkmate.goal.application.GoalCommandService;
import checkmate.goal.application.GoalQueryService;
import checkmate.goal.application.dto.request.LikeCountCreateCommand;
import checkmate.goal.application.dto.response.GoalDetailInfo;
import checkmate.goal.application.dto.response.GoalScheduleInfo;
import checkmate.goal.application.dto.response.OngoingGoalInfoResult;
import checkmate.goal.application.dto.response.TodayGoalInfoResult;
import checkmate.goal.presentation.dto.*;
import checkmate.mate.application.dto.response.GoalHistoryInfoResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RequiredArgsConstructor
@Slf4j
@RestController
public class GoalController {
    private final GoalCommandService goalCommandService;
    private final GoalQueryService goalQueryService;
    private final GoalDtoMapper mapper;

    @PostMapping("/goals")
    public GoalCreateResponse create(@RequestBody @Valid GoalCreateDto dto,
                                     @AuthenticationPrincipal JwtUserDetails details) {
        long goalId = goalCommandService.create(mapper.toCommand(dto, details.getUserId()));
        return new GoalCreateResponse(goalId);
    }

    @GoalMember(GoalId.PATH_VARIABLE)
    @PostMapping("/goals/{goalId}/like-condition")
    public void addLikeCondition(@PathVariable long goalId,
                                 @RequestBody @Valid LikeCountCreateDto dto,
                                 @AuthenticationPrincipal JwtUserDetails details) {
        LikeCountCreateCommand command = mapper.toCommand(goalId, dto, details.getUserId());
        goalCommandService.addLikeCountCondition(command);
    }

    @GoalMember(GoalId.PATH_VARIABLE)
    @PatchMapping("/goals/{goalId}")
    public void modify(@PathVariable long goalId,
                       @RequestBody GoalModifyDto dto,
                       @AuthenticationPrincipal JwtUserDetails details) {
        goalCommandService.modifyGoal(mapper.toCommand(dto, goalId, details.getUserId()));
    }

    @GetMapping("/goals/{goalId}")
    public GoalDetailInfo findGoalDetail(@PathVariable long goalId) {
        return goalQueryService.findGoalDetail(goalId);
    }

    @GetMapping("/goals/{goalId}/period")
    public GoalScheduleInfo findGoalPeriod(@PathVariable long goalId) {
        return goalQueryService.findGoalPeriodInfo(goalId);
    }

    @GetMapping("/goals/ongoing")
    public OngoingGoalInfoResult findOngoingInfo(@AuthenticationPrincipal JwtUserDetails details) {
        return goalQueryService.findOngoingGoalInfo(details.getUserId());
    }

    @GetMapping("/goals/today")
    public TodayGoalInfoResult findTodayGoalInfo(@AuthenticationPrincipal JwtUserDetails details) {
        return goalQueryService.findTodayGoalInfo(details.getUserId());
    }

    @GetMapping("/goals/history")
    public GoalHistoryInfoResult findGoalHistoryResult(@AuthenticationPrincipal JwtUserDetails details) {
        return goalQueryService.findGoalHistoryResult(details.getUserId());
    }
}
