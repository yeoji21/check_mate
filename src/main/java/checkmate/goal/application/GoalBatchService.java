package checkmate.goal.application;

import static checkmate.notification.domain.NotificationType.COMPLETE_GOAL;

import checkmate.common.cache.KeyValueStorage;
import checkmate.goal.domain.GoalRepository;
import checkmate.goal.infra.GoalQueryDao;
import checkmate.notification.domain.event.NotPushNotificationCreatedEvent;
import checkmate.notification.domain.factory.dto.CompleteGoalNotificationDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class GoalBatchService {

    private final GoalRepository goalRepository;
    private final GoalQueryDao goalQueryDao;
    private final ApplicationEventPublisher eventPublisher;
    private final KeyValueStorage keyValueStorage;

    @Transactional
    public void updateTodayStartGoals() {
        goalRepository.updateTodayStartGoalsToOngoing();
    }

    @Transactional
    public void updateYesterdayOveredGoals() {
        List<Long> overedGoalIds = goalQueryDao.findYesterdayOveredGoals();
        goalRepository.updateStatusToOver(overedGoalIds);
        publishCompleteGoalEvent(overedGoalIds);
    }

    // TODO: 2023/08/13 추상화 레벨
    private void publishCompleteGoalEvent(List<Long> overedGoalIds) {
        List<CompleteGoalNotificationDto> notificationDtos =
            goalQueryDao.findCompleteNotificationDto(overedGoalIds);
        eventPublisher.publishEvent(
            new NotPushNotificationCreatedEvent(COMPLETE_GOAL, notificationDtos));
        notificationDtos.stream().map(CompleteGoalNotificationDto::getUserId)
            .forEach(keyValueStorage::deleteAll);
    }
}
