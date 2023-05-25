package checkmate.goal.presentation;

import checkmate.common.interceptor.GoalId;
import checkmate.common.interceptor.GoalMember;
import checkmate.config.auth.JwtUserDetails;
import checkmate.goal.application.GoalCommandService;
import checkmate.goal.application.GoalQueryService;
import checkmate.goal.application.dto.request.GoalCreateCommand;
import checkmate.goal.application.dto.request.GoalModifyCommand;
import checkmate.goal.application.dto.request.LikeCountCreateCommand;
import checkmate.goal.application.dto.response.GoalDetailInfo;
import checkmate.goal.application.dto.response.GoalScheduleInfo;
import checkmate.goal.application.dto.response.OngoingGoalInfoResult;
import checkmate.goal.application.dto.response.TodayGoalInfoResult;
import checkmate.goal.presentation.dto.GoalCreateDto;
import checkmate.goal.presentation.dto.GoalCreateResponse;
import checkmate.goal.presentation.dto.GoalDtoMapper;
import checkmate.goal.presentation.dto.GoalModifyDto;
import checkmate.goal.presentation.dto.LikeCountCreateDto;
import checkmate.mate.application.dto.response.GoalHistoryInfoResult;
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
public class GoalController {

    private final GoalCommandService goalCommandService;
    private final GoalQueryService goalQueryService;
    private final GoalDtoMapper mapper;

    @PostMapping("/goals")
    public GoalCreateResponse create(
        @RequestBody @Valid GoalCreateDto dto,
        @AuthenticationPrincipal JwtUserDetails details) {
        return new GoalCreateResponse(goalCommandService.create(toCreateCommand(dto, details)));
    }

    @GoalMember(GoalId.PATH_VARIABLE)
    @PostMapping("/goals/{goalId}/like-condition")
    public void addLikeCondition(
        @PathVariable long goalId,
        @RequestBody @Valid LikeCountCreateDto dto,
        @AuthenticationPrincipal JwtUserDetails details) {
        goalCommandService.addLikeCountCondition(toConditionCreateCommand(dto, goalId, details));
    }

    @GoalMember(GoalId.PATH_VARIABLE)
    @PatchMapping("/goals/{goalId}")
    public void modify(
        @PathVariable long goalId,
        @RequestBody GoalModifyDto dto,
        @AuthenticationPrincipal JwtUserDetails details) {
        goalCommandService.modifyGoal(toModifyCommand(goalId, dto, details));
    }

    @GetMapping("/goals/{goalId}")
    public GoalDetailInfo findGoalDetail(@PathVariable long goalId) {
        return goalQueryService.findGoalDetail(goalId);
    }

    @GetMapping("/goals/{goalId}/period")
    public GoalScheduleInfo findGoalSchedule(@PathVariable long goalId) {
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
    public GoalHistoryInfoResult findGoalHistoryResult(
        @AuthenticationPrincipal JwtUserDetails details) {
        return goalQueryService.findGoalHistoryResult(details.getUserId());
    }

    private GoalModifyCommand toModifyCommand(long goalId, GoalModifyDto dto,
        JwtUserDetails details) {
        return mapper.toCommand(dto, goalId, details.getUserId());
    }

    private GoalCreateCommand toCreateCommand(GoalCreateDto dto, JwtUserDetails details) {
        return mapper.toCommand(dto, details.getUserId());
    }

    private LikeCountCreateCommand toConditionCreateCommand(
        LikeCountCreateDto dto,
        long goalId,
        JwtUserDetails details) {
        return mapper.toCommand(goalId, dto, details.getUserId());
    }
}
