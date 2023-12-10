package checkmate.mate.application;

import static checkmate.notification.domain.NotificationType.EXPULSION_GOAL;

import checkmate.common.cache.KeyValueStorage;
import checkmate.mate.application.dto.MateCommandMapper;
import checkmate.mate.domain.Mate;
import checkmate.mate.domain.MateRepository;
import checkmate.notification.domain.event.NotPushNotificationCreatedEvent;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class MateBatchService {

    private final MateRepository mateRepository;
    private final KeyValueStorage keyValueStorage;
    private final ApplicationEventPublisher eventPublisher;
    private final MateCommandMapper mapper;

    @Transactional
    public void updateUploadSkippedMates() {
        List<Mate> skippedMates = updateYesterdaySkippedMates();
        List<Mate> limitOveredMates = filterLimitOveredMates(skippedMates);
        mateRepository.updateLimitOveredMates(limitOveredMates);
        publishExpulsionNotifications(limitOveredMates);
        limitOveredMates.stream().map(Mate::getUserId)
            .forEach(keyValueStorage::deleteAll);
    }

    private List<Mate> updateYesterdaySkippedMates() {
        List<Mate> skippedMates = mateRepository.findYesterdaySkippedMates();
        mateRepository.increaseSkippedDayCount(skippedMates);
        List<Long> mateIds = skippedMates.stream().map(Mate::getId).toList();
        skippedMates = mateRepository.findAllWithGoal(mateIds);
        return skippedMates;
    }

    private void publishExpulsionNotifications(List<Mate> limitOveredMates) {
        eventPublisher.publishEvent(new NotPushNotificationCreatedEvent(EXPULSION_GOAL,
            limitOveredMates.stream().map(mapper::toNotificationDto).toList()));
    }

    private List<Mate> filterLimitOveredMates(List<Mate> hookyMates) {
        return hookyMates.stream()
            .filter(tm -> tm.getSkippedDayCount() >= tm.getGoal().getLimitOfSkippedDay())
            .toList();
    }
}
