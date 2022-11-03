package checkmate.goal.presentation;

import checkmate.config.auth.JwtUserDetails;
import checkmate.config.redis.RedisKey;
import checkmate.goal.application.GoalCommandService;
import checkmate.goal.application.GoalFacadeService;
import checkmate.goal.application.GoalQueryService;
import checkmate.goal.application.dto.request.LikeCountCreateCommand;
import checkmate.goal.application.dto.response.*;
import checkmate.goal.presentation.dto.GoalDtoMapper;
import checkmate.goal.presentation.dto.request.GoalCreateDto;
import checkmate.goal.presentation.dto.request.GoalModifyDto;
import checkmate.goal.presentation.dto.request.LikeCountCreateDto;
import checkmate.goal.presentation.dto.response.GoalListQueryResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
@RestController
public class GoalController {
    private final GoalCommandService goalCommandService;
    private final GoalQueryService goalQueryService;
    private final GoalFacadeService goalFacadeService;
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
    public GoalCreateResult goalSave(@RequestBody @Valid GoalCreateDto dto,
                                     @AuthenticationPrincipal JwtUserDetails details) {
        return goalCommandService.create(mapper.toCreateCommand(dto, details.getUserId()));
    }

    @PostMapping("/goal/confirm-like")
    public void confirmLikeCondition(@RequestBody @Valid LikeCountCreateDto dto,
                                     @AuthenticationPrincipal JwtUserDetails details) {
        LikeCountCreateCommand command = mapper.toLikeCountCreateCommand(dto, details.getUserId());
        goalCommandService.setLikeCountCondition(command);
    }

    @CacheEvict(value = RedisKey.GOAL_PERIOD, key = "{#goalId}")
    @PatchMapping("/goal/{goalId}")
    public void goalModify(@PathVariable long goalId,
                           @RequestBody GoalModifyDto dto,
                           @AuthenticationPrincipal JwtUserDetails details){
        goalCommandService.modifyGoal(mapper.toModifyCommand(dto, goalId, details.getUserId()));
    }

    @GetMapping("/goal/{goalId}")
    public GoalDetailInfo goalDetailFind(@PathVariable long goalId,
                                         @AuthenticationPrincipal JwtUserDetails userDetails) {
        return goalQueryService.findGoalDetail(goalId, userDetails.getUserId());
    }

    @Cacheable(value = RedisKey.GOAL_PERIOD, key = "{#goalId}")
    @GetMapping("/goal/{goalId}/period")
    public GoalPeriodInfo goalPeriodFind(@PathVariable long goalId) {
        return goalQueryService.findGoalPeriodInfo(goalId);
    }

    @Cacheable(value = RedisKey.ONGOING_GOALS, key = "{#details.userId, T(java.time.LocalDate).now()}")
    @GetMapping("/goal/ongoing")
    public GoalListQueryResponse<GoalSimpleInfo> ongoingGoalSimpleInfoFind(@AuthenticationPrincipal JwtUserDetails details) {
        List<GoalSimpleInfo> goalSimpleInfoList = goalQueryService.findOngoingSimpleInfo(details.getUserId());
        return new GoalListQueryResponse<>(goalSimpleInfoList);
    }

    @Cacheable(value = RedisKey.TODAY_GOALS, key = "{#details.userId, T(java.time.LocalDate).now()}")
    @GetMapping("/goal/today")
    public GoalListQueryResponse<TodayGoalInfo> todayGoalFind(@AuthenticationPrincipal JwtUserDetails details) {
        List<TodayGoalInfo> todayGoalInfoList = goalQueryService.findTodayGoalInfo(details.getUserId());
        return new GoalListQueryResponse<>(todayGoalInfoList);
    }

    @Cacheable(value = RedisKey.HISTORY_GOALS, key = "{#details.userId}")
    @GetMapping("/goal/history")
    public GoalListQueryResponse<GoalHistoryInfo> successGoalHistoryFind(@AuthenticationPrincipal JwtUserDetails details) {
        List<GoalHistoryInfo> goalHistoryInfoList = goalQueryService.findHistoryGoalInfo(details.getUserId());
        return new GoalListQueryResponse<>(goalHistoryInfoList);
    }

    @GetMapping("/goal/view/{goalId}")
    public GoalDetailViewResult goalDetailViewFind(@PathVariable long goalId,
                                                   @AuthenticationPrincipal JwtUserDetails details) {
        return goalFacadeService.goalDetailView(goalId, details.getUserId());
    }

}
