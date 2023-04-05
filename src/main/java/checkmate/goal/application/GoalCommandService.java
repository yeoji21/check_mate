package checkmate.goal.application;

import checkmate.common.cache.CacheHandler;
import checkmate.common.cache.CacheKey;
import checkmate.exception.NotFoundException;
import checkmate.exception.code.ErrorCode;
import checkmate.goal.application.dto.GoalCommandMapper;
import checkmate.goal.application.dto.request.GoalCreateCommand;
import checkmate.goal.application.dto.request.GoalModifyCommand;
import checkmate.goal.application.dto.request.LikeCountCreateCommand;
import checkmate.goal.domain.Goal;
import checkmate.goal.domain.GoalRepository;
import checkmate.goal.domain.LikeCountCondition;
import checkmate.mate.domain.Mate;
import checkmate.mate.domain.MateInitiateManager;
import checkmate.mate.domain.MateRepository;
import checkmate.notification.domain.event.NotPushNotificationCreatedEvent;
import checkmate.notification.domain.factory.dto.CompleteGoalNotificationDto;
import checkmate.user.domain.User;
import checkmate.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static checkmate.exception.code.ErrorCode.USER_NOT_FOUND;
import static checkmate.notification.domain.NotificationType.COMPLETE_GOAL;

@Slf4j
@RequiredArgsConstructor
@Service
public class GoalCommandService {
    private final GoalRepository goalRepository;
    private final UserRepository userRepository;
    private final MateRepository mateRepository;
    private final MateInitiateManager mateInitiateManager;
    private final ApplicationEventPublisher eventPublisher;
    private final CacheHandler cacheHandler;
    private final GoalCommandMapper mapper;

    @Caching(evict = {
            @CacheEvict(
                    value = CacheKey.ONGOING_GOALS,
                    key = "{#command.userId, T(java.time.LocalDate).now().format(@dateFormatter)}"),
            @CacheEvict(
                    value = CacheKey.TODAY_GOALS,
                    key = "{#command.userId, T(java.time.LocalDate).now().format(@dateFormatter)}")
    })
    @Transactional
    public long create(GoalCreateCommand command) {
        Goal goal = goalRepository.save(mapper.toEntity(command));
        mateRepository.save(joinToGoal(goal, command.userId()));
        return goal.getId();
    }

    @CacheEvict(value = CacheKey.GOAL_PERIOD, key = "{#command.goalId}")
    @Transactional
    public void modifyGoal(GoalModifyCommand command) {
        Goal goal = findGoalForUpdate(command.goalId());
        goal.update(mapper.toGoalModifyRequest(command));
    }

    @Transactional
    public void addLikeCountCondition(LikeCountCreateCommand command) {
        Goal goal = goalRepository.findById(command.goalId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.GOAL_NOT_FOUND, command.goalId()));
        goal.addCondition(new LikeCountCondition(command.likeCount()));
    }

    @Transactional
    public void updateTodayStartGoal() {
        goalRepository.updateTodayStartGoal();
    }

    @Transactional
    public void updateYesterdayOveredGoals() {
        List<Long> overedGoalIds = goalRepository.updateYesterdayOveredGoals();
        List<Mate> mates = mateRepository.findMatesInGoals(overedGoalIds);
        eventPublisher.publishEvent(new NotPushNotificationCreatedEvent(COMPLETE_GOAL, toNotificationDtos(mates)));
        cacheHandler.deleteMateCaches(mates);
    }

    private Mate joinToGoal(Goal goal, long userId) {
        User creator = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND, userId));
        Mate mate = goal.join(creator);
        mate.toWaitingStatus();
        mateInitiateManager.initiate(mate);
        return mate;
    }

    private Goal findGoalForUpdate(long goalId) {
        return goalRepository.findByIdForUpdate(goalId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.GOAL_NOT_FOUND, goalId));
    }

    private List<CompleteGoalNotificationDto> toNotificationDtos(List<Mate> mates) {
        return mates.stream().map(
                tm -> CompleteGoalNotificationDto.builder()
                        .goalId(tm.getGoal().getId())
                        .goalTitle(tm.getGoal().getTitle())
                        .userId(tm.getUserId())
                        .build()
        ).toList();
    }
}
