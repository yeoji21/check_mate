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
import checkmate.goal.infra.GoalQueryDao;
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
    private final GoalQueryDao goalQueryDao;
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
        Goal goal = saveNewGoal(command);
        creatorJoinToGoal(goal, command.userId());
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
        Goal goal = findGoal(command.goalId());
        goal.addCondition(createLikeCountCondition(command.likeCount()));
    }

    @Transactional
    public void updateTodayStartGoals() {
        goalRepository.updateTodayStartStatus();
    }

    @Transactional
    public void updateYesterdayOveredGoals() {
        List<Long> overedGoalIds = goalQueryDao.findYesterdayOveredGoals();
        goalRepository.updateStatusToOver(overedGoalIds);
        cacheHandler.deleteUserCaches(publishComplateNotifications(overedGoalIds));
    }

    private List<Long> publishComplateNotifications(List<Long> overedGoalIds) {
        List<CompleteGoalNotificationDto> notificationDto = goalQueryDao.findCompleteNotificationDto(overedGoalIds);
        eventPublisher.publishEvent(new NotPushNotificationCreatedEvent(COMPLETE_GOAL, notificationDto));
        return notificationDto.stream().map(dto -> dto.getUserId()).toList();
    }

    private void creatorJoinToGoal(Goal goal, long userId) {
        mateInitiateManager.initiate(saveNewMate(goal, userId));
    }

    private Mate saveNewMate(Goal goal, long userId) {
        Mate mate = goal.join(findUser(userId));
        mate.toWaitingStatus();
        mateRepository.save(mate);
        return mate;
    }

    private LikeCountCondition createLikeCountCondition(int count) {
        return new LikeCountCondition(count);
    }

    private Goal findGoal(long goalId) {
        return goalRepository.findById(goalId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.GOAL_NOT_FOUND, goalId));
    }

    private Goal saveNewGoal(GoalCreateCommand command) {
        return goalRepository.save(mapper.toEntity(command));
    }

    private User findUser(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND, userId));
    }

    private Goal findGoalForUpdate(long goalId) {
        return goalRepository.findByIdForUpdate(goalId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.GOAL_NOT_FOUND, goalId));
    }
}
