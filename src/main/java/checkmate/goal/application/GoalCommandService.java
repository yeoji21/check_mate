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
import checkmate.notification.domain.factory.dto.CompleteGoalNotificationDto;
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
    private final TeamMateInitiateManager teamMateInitiateManager;
    private final ApplicationEventPublisher eventPublisher;
    private final CacheTemplate cacheTemplate;
    private final GoalCommandMapper mapper;

    @Transactional
    public long create(GoalCreateCommand command) {
        Goal goal = goalRepository.save(mapper.toEntity(command));
        teamMateRepository.save(joinToGoal(goal, command.userId()));
        return goal.getId();
    }

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
        List<TeamMate> teamMates = teamMateRepository.findTeamMates(overedGoalIds)
                .stream()
                .filter(tm -> tm.getStatus() == TeamMateStatus.ONGOING)
                .toList();

        eventPublisher.publishEvent(new NotPushNotificationCreatedEvent(COMPLETE_GOAL, toDtos(teamMates)));
        cacheTemplate.deleteTMCacheData(teamMates);
    }

    private List<CompleteGoalNotificationDto> toDtos(List<TeamMate> teamMates) {
        return teamMates.stream().map(
                tm -> CompleteGoalNotificationDto.builder()
                        .goalId(tm.getGoal().getId())
                        .goalTitle(tm.getGoal().getTitle())
                        .userId(tm.getUserId())
                        .build()
        ).toList();
    }

    private TeamMate joinToGoal(Goal goal, long userId) {
        User creator = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND, userId));
        TeamMate teamMate = goal.join(creator);
        teamMateInitiateManager.initiate(teamMate);
//        teamMate.initiateGoal(userRepository.countOngoingGoals(userId));
        return teamMate;
    }

    private Goal findGoalForUpdate(long goalId) {
        return goalRepository.findByIdForUpdate(goalId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.GOAL_NOT_FOUND, goalId));
    }
}
