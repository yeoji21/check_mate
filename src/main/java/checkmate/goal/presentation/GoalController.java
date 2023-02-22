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
import checkmate.goal.presentation.dto.GoalDtoMapper;
import checkmate.goal.presentation.dto.request.GoalCreateDto;
import checkmate.goal.presentation.dto.request.GoalModifyDto;
import checkmate.goal.presentation.dto.request.LikeCountCreateDto;
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
    public long create(@RequestBody @Valid GoalCreateDto dto,
                       @AuthenticationPrincipal JwtUserDetails details) {
        return goalCommandService.create(mapper.toCommand(dto, details.getUserId()));
    }

    @GoalMember(GoalIdRoute.REQUEST_BODY)
    @PostMapping("/goal/confirm-like")
    public void confirmLikeCondition(@RequestBody @Valid LikeCountCreateDto dto,
                                     @AuthenticationPrincipal JwtUserDetails details) {
        LikeCountCreateCommand command = mapper.toCommand(dto, details.getUserId());
        goalCommandService.addLikeCountCondition(command);
    }

    @GoalMember(GoalIdRoute.PATH_VARIABLE)
    @PatchMapping("/goal/{goalId}")
    public void goalModify(@PathVariable long goalId,
                           @RequestBody GoalModifyDto dto,
                           @AuthenticationPrincipal JwtUserDetails details) {
        goalCommandService.modifyGoal(mapper.toCommand(dto, goalId, details.getUserId()));
    }

    @GetMapping("/goal/{goalId}")
    public GoalDetailInfo goalDetailFind(@PathVariable long goalId,
                                         @AuthenticationPrincipal JwtUserDetails userDetails) {
        return goalQueryService.findGoalDetail(goalId, userDetails.getUserId());
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
