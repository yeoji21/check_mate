package checkmate.goal.presentation;

import checkmate.common.interceptor.GoalIdRoute;
import checkmate.common.interceptor.GoalMember;
import checkmate.config.auth.JwtUserDetails;
import checkmate.goal.application.GoalCommandService;
import checkmate.goal.application.GoalQueryService;
import checkmate.goal.application.dto.request.LikeCountCreateCommand;
import checkmate.goal.application.dto.response.GoalDetailInfo;
import checkmate.goal.application.dto.response.GoalScheduleInfo;
import checkmate.goal.application.dto.response.GoalSimpleInfoResult;
import checkmate.goal.application.dto.response.TodayGoalInfoResult;
import checkmate.goal.presentation.dto.*;
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

    @PostMapping("/goal")
    public GoalCreateResponse create(@RequestBody @Valid GoalCreateDto dto,
                                     @AuthenticationPrincipal JwtUserDetails details) {
        long goalId = goalCommandService.create(mapper.toCommand(dto, details.getUserId()));
        return new GoalCreateResponse(goalId);
    }

    @GoalMember(GoalIdRoute.REQUEST_BODY)
    @PostMapping("/goal/like-condition")
    public void addLikeCondition(@RequestBody @Valid LikeCountCreateDto dto,
                                 @AuthenticationPrincipal JwtUserDetails details) {
        LikeCountCreateCommand command = mapper.toCommand(dto, details.getUserId());
        goalCommandService.addLikeCountCondition(command);
    }

    @GoalMember(GoalIdRoute.PATH_VARIABLE)
    @PatchMapping("/goal/{goalId}")
    public void modify(@PathVariable long goalId,
                       @RequestBody GoalModifyDto dto,
                       @AuthenticationPrincipal JwtUserDetails details) {
        goalCommandService.modifyGoal(mapper.toCommand(dto, goalId, details.getUserId()));
    }

    @GetMapping("/goal/{goalId}")
    public GoalDetailInfo findGoalDetail(@PathVariable long goalId) {
        return goalQueryService.findGoalDetail(goalId);
    }

    @GetMapping("/goal/{goalId}/period")
    public GoalScheduleInfo goalPeriodFind(@PathVariable long goalId) {
        return goalQueryService.findGoalPeriodInfo(goalId);
    }

    @GetMapping("/goal/ongoing")
    public GoalSimpleInfoResult ongoingGoalSimpleInfoFind(@AuthenticationPrincipal JwtUserDetails details) {
        return goalQueryService.findOngoingSimpleInfo(details.getUserId());
    }

    @GetMapping("/goal/today")
    public TodayGoalInfoResult todayGoalFind(@AuthenticationPrincipal JwtUserDetails details) {
        return goalQueryService.findTodayGoalInfo(details.getUserId());
    }
}
