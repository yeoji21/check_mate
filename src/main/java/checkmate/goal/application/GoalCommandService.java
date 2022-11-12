package checkmate.goal.application;

import checkmate.common.cache.CacheTemplate;
import checkmate.exception.GoalNotFoundException;
import checkmate.exception.UserNotFoundException;
import checkmate.goal.application.dto.GoalCommandMapper;
import checkmate.goal.application.dto.request.GoalCreateCommand;
import checkmate.goal.application.dto.request.GoalModifyCommand;
import checkmate.goal.application.dto.request.LikeCountCreateCommand;
import checkmate.goal.domain.*;
import checkmate.goal.domain.event.GoalCreatedEvent;
import checkmate.notification.domain.event.StaticNotificationCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
        checkUserIsInGoal(command.getGoalId(), command.getUserId());
        GoalUpdater updater = new GoalUpdater(mapper.toGoalModifyRequest(command));
        updater.update(findGoalForUpdate(command.getGoalId()));
    }

    @Transactional
    public void setLikeCountCondition(LikeCountCreateCommand command) {
        checkUserIsInGoal(command.getGoalId(), command.getUserId());
        Goal goal = goalRepository.findById(command.getGoalId()).orElseThrow(IllegalArgumentException::new);
        goal.addCondition(new LikeCountCondition(command.getLikeCount()));
    }

    @Transactional
    public void updateYesterdayOveredGoals() {
        List<Goal> overedGoals = goalRepository.updateYesterdayOveredGoals();

        List<Long> goalIds = overedGoals.stream().map(Goal::getId).toList();
        List<TeamMate> teamMates = teamMateRepository.findTeamMates(goalIds)
                .stream()
                .filter(tm -> tm.getStatus() == TeamMateStatus.ONGOING)
                .toList();

        eventPublisher.publishEvent(new StaticNotificationCreatedEvent(COMPLETE_GOAL, mapper.toGoalCompleteNotificationDtos(teamMates)));
        cacheTemplate.deleteTMCacheData(teamMates);
    }

    private void ongoingGoalCountCheck(long userId) {
        GoalJoiningPolicy.ongoingGoalCount(goalRepository.countOngoingGoals(userId));
    }

    private void checkUserIsInGoal(long goalId, long userId) {
        if (!goalRepository.checkUserIsInGoal(goalId, userId))
            throw new UserNotFoundException();
    }

    private Goal findGoalForUpdate(long goalId) {
        return goalRepository.findByIdForUpdate(goalId).orElseThrow(GoalNotFoundException::new);
    }
}
