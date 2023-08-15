package checkmate.goal.application;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import checkmate.TestEntityFactory;
import checkmate.common.cache.KeyValueStorage;
import checkmate.goal.domain.Goal;
import checkmate.goal.domain.GoalRepository;
import checkmate.goal.infra.FakeGoalRepository;
import checkmate.goal.infra.GoalQueryDao;
import checkmate.notification.domain.event.NotPushNotificationCreatedEvent;
import checkmate.notification.domain.factory.dto.CompleteGoalNotificationDto;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;


@ExtendWith(MockitoExtension.class)
class GoalBatchServiceTest {

    @Spy
    private GoalRepository goalRepository = new FakeGoalRepository();
    @Mock
    private GoalQueryDao goalQueryDao;
    @Mock
    private KeyValueStorage keyValueStorage;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @InjectMocks
    private GoalBatchService goalBatchService;


    @Test
    @DisplayName("성공한 목표 처리 스케쥴러")
    void updateYesterdayOveredGoals() throws Exception {
        //given
        Goal goal1 = createGoal();
        Goal goal2 = createGoal();
        given(goalQueryDao.findYesterdayOveredGoals()).willReturn(
            List.of(goal1.getId(), goal2.getId()));
        given(goalQueryDao.findCompleteNotificationDto(anyList()))
            .willReturn(List.of(new CompleteGoalNotificationDto(1L, 1L, "title")));
        given(goalQueryDao.findOngoingUserIds(anyList())).willReturn(List.of(1L));
        
        //when
        goalBatchService.updateYesterdayOveredGoals();

        //then
        verify(eventPublisher).publishEvent(any(NotPushNotificationCreatedEvent.class));
        verify(keyValueStorage).deleteAll(anyLong());
    }

    private Goal createGoal() {
        return goalRepository.save(TestEntityFactory.goal(0L, "goal"));
    }
}