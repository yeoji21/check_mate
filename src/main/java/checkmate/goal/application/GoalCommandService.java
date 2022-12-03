package checkmate.goal.application;

import checkmate.common.cache.CacheTemplate;
import checkmate.exception.ErrorCode;
import checkmate.exception.NotFoundException;
import checkmate.goal.application.dto.GoalCommandMapper;
import checkmate.goal.application.dto.request.GoalCreateCommand;
import checkmate.goal.application.dto.request.GoalModifyCommand;
import checkmate.goal.application.dto.request.LikeCountCreateCommand;
import checkmate.goal.domain.*;
import checkmate.goal.domain.event.GoalCreatedEvent;
import checkmate.notification.domain.event.NotPushNotificationCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static checkmate.exception.ErrorCode.USER_NOT_FOUND;
import static checkmate.notification.domain.NotificationType.COMPLETE_GOAL;

@Slf4j
@RequiredArgsConstructor
@Service
public class GoalCommandService {
    private final GoalRepository goalRepository;
    private final TeamMateRepository teamMateRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final CacheTemplate cacheTemplate;
    private final GoalCommandMapper mapper;

    @Transactional
    public long create(GoalCreateCommand command) {
        ongoingGoalCountCheck(command.getUserId());
        Goal goal = mapper.toGoal(command);
        goalRepository.save(goal);
        eventPublisher.publishEvent(new GoalCreatedEvent(goal.getId(), command.getUserId()));
        return goal.getId();
    }

    @Transactional
    public void modifyGoal(GoalModifyCommand command) {
        checkUserIsInGoal(command.goalId(), command.userId());
        GoalUpdater updater = new GoalUpdater(mapper.toGoalModifyRequest(command));
        updater.update(findGoalForUpdate(command.goalId()));
    }

    @Transactional
    public void setLikeCountCondition(LikeCountCreateCommand command) {
        checkUserIsInGoal(command.getGoalId(), command.getUserId());
        Goal goal = goalRepository.findById(command.getGoalId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.GOAL_NOT_FOUND, command.getGoalId()));
        goal.addCondition(new LikeCountCondition(command.getLikeCount()));
    }

    @Transactional
    public void updateTodayStartGoal() {
        goalRepository.updateTodayStartGoal();
    }

    @Transactional
    public void updateYesterdayOveredGoals() {
        List<Long> overedGoalIds = goalRepository.updateYesterdayOveredGoals();
        List<TeamMate> teamMates = teamMateRepository.findTeamMates(overedGoalIds)
                .stream()
                .filter(tm -> tm.getStatus() == TeamMateStatus.ONGOING)
                .toList();

        eventPublisher.publishEvent(new NotPushNotificationCreatedEvent(COMPLETE_GOAL, mapper.toGoalCompleteNotificationDtos(teamMates)));
        cacheTemplate.deleteTMCacheData(teamMates);
    }

    private void ongoingGoalCountCheck(long userId) {
        GoalJoiningPolicy.ongoingGoalCount(goalRepository.countOngoingGoals(userId));
    }

    private void checkUserIsInGoal(long goalId, long userId) {
        if (!goalRepository.checkUserIsInGoal(goalId, userId))
            throw new NotFoundException(USER_NOT_FOUND, userId);
    }

    private Goal findGoalForUpdate(long goalId) {
        return goalRepository.findByIdForUpdate(goalId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.GOAL_NOT_FOUND, goalId));
    }
}
