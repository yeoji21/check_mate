package checkmate.goal.application;

import checkmate.common.cache.CacheTemplate;
import checkmate.exception.NotFoundException;
import checkmate.exception.code.ErrorCode;
import checkmate.goal.application.dto.GoalCommandMapper;
import checkmate.goal.application.dto.request.GoalCreateCommand;
import checkmate.goal.application.dto.request.GoalModifyCommand;
import checkmate.goal.application.dto.request.LikeCountCreateCommand;
import checkmate.goal.domain.*;
import checkmate.notification.domain.event.NotPushNotificationCreatedEvent;
import checkmate.user.domain.User;
import checkmate.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final TeamMateRepository teamMateRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final CacheTemplate cacheTemplate;
    private final GoalCommandMapper mapper;

    @Transactional
    public long create(GoalCreateCommand command) {
        Goal goal = goalRepository.save(mapper.toGoal(command));
        TeamMate teamMate = joinToGoal(goal, command.getUserId());
        teamMate.initiateGoal(goalRepository.countOngoingGoals(command.getUserId()));
        teamMateRepository.save(teamMate);
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

    private TeamMate joinToGoal(Goal goal, long userId) {
        User creator = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND, userId));
        return goal.join(creator);
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
