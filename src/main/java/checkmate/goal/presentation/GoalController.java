package checkmate.goal.presentation;

import checkmate.config.auth.JwtUserDetails;
import checkmate.config.redis.RedisKey;
import checkmate.goal.application.GoalCommandService;
import checkmate.goal.application.GoalQueryService;
import checkmate.goal.application.dto.request.LikeCountCreateCommand;
import checkmate.goal.application.dto.response.*;
import checkmate.goal.presentation.dto.GoalDtoMapper;
import checkmate.goal.presentation.dto.request.GoalCreateDto;
import checkmate.goal.presentation.dto.request.GoalModifyDto;
import checkmate.goal.presentation.dto.request.LikeCountCreateDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
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

    @Caching(evict = {
            @CacheEvict(
                    value = RedisKey.ONGOING_GOALS,
                    key = "{#details.userId, T(java.time.LocalDate).now()}"),
            @CacheEvict(
                    value = RedisKey.TODAY_GOALS,
                    key = "{#details.userId, T(java.time.LocalDate).now()}")
    })
    @PostMapping("/goal")
    public long save(@RequestBody @Valid GoalCreateDto dto,
                     @AuthenticationPrincipal JwtUserDetails details) {
        return goalCommandService.create(mapper.toCommand(dto, details.getUserId()));
    }

    @PostMapping("/goal/confirm-like")
    public void confirmLikeCondition(@RequestBody @Valid LikeCountCreateDto dto,
                                     @AuthenticationPrincipal JwtUserDetails details) {
        LikeCountCreateCommand command = mapper.toCommand(dto, details.getUserId());
        goalCommandService.setLikeCountCondition(command);
    }

    @CacheEvict(value = RedisKey.GOAL_PERIOD, key = "{#goalId}")
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

    @Cacheable(value = RedisKey.GOAL_PERIOD, key = "{#goalId}")
    @GetMapping("/goal/{goalId}/period")
    public GoalScheduleInfo goalPeriodFind(@PathVariable long goalId) {
        return goalQueryService.findGoalPeriodInfo(goalId);
    }

    @Cacheable(value = RedisKey.ONGOING_GOALS, key = "{#details.userId, T(java.time.LocalDate).now()}")
    @GetMapping("/goal/ongoing")
    public GoalSimpleInfoResult ongoingGoalSimpleInfoFind(@AuthenticationPrincipal JwtUserDetails details) {
        return goalQueryService.findOngoingSimpleInfo(details.getUserId());
    }

    @Cacheable(value = RedisKey.TODAY_GOALS, key = "{#details.userId, T(java.time.LocalDate).now()}")
    @GetMapping("/goal/today")
    public TodayGoalInfoResult todayGoalFind(@AuthenticationPrincipal JwtUserDetails details) {
        return goalQueryService.findTodayGoalInfo(details.getUserId());
    }

    @Cacheable(value = RedisKey.HISTORY_GOALS, key = "{#details.userId}")
    @GetMapping("/goal/history")
    public GoalHistoryInfoResult successGoalHistoryFind(@AuthenticationPrincipal JwtUserDetails details) {
        return goalQueryService.findHistoryGoalInfo(details.getUserId());
    }
}
