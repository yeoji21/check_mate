package checkmate.mate.application;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;

import checkmate.TestEntityFactory;
import checkmate.common.cache.KeyValueStorage;
import checkmate.goal.domain.Goal;
import checkmate.goal.domain.GoalRepository;
import checkmate.goal.infra.FakeGoalRepository;
import checkmate.mate.application.dto.MateCommandMapper;
import checkmate.mate.domain.Mate;
import checkmate.mate.domain.MateRepository;
import checkmate.mate.infra.FakeMateRepository;
import checkmate.notification.domain.event.NotPushNotificationCreatedEvent;
import checkmate.user.domain.User;
import checkmate.user.domain.UserRepository;
import checkmate.user.infrastructure.FakeUserRepository;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class MateBatchServiceTest {

    @Mock
    private KeyValueStorage keyValueStorage;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Spy
    private GoalRepository goalRepository = new FakeGoalRepository();
    @Spy
    private UserRepository userRepository = new FakeUserRepository();
    @Spy
    private MateCommandMapper commandMapper = MateCommandMapper.INSTANCE;
    @Spy
    private MateRepository mateRepository = new FakeMateRepository();
    @InjectMocks
    private MateBatchService mateBatchService;


    @Test
    @DisplayName("인증일에 인증하지 않은 팀원 업데이트")
    void updateUploadSkippedMate() throws Exception {
        //given
        createUploadSkippedMates();

        //when
        mateBatchService.updateUploadSkippedMates();

        //then
        verify(eventPublisher).publishEvent(any(NotPushNotificationCreatedEvent.class));
        verify(keyValueStorage).deleteAll(anyLong());
    }

    private void createUploadSkippedMates() {
        Goal goal = createAndSaveGoal();
        List<Mate> skippedMates = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            skippedMates.add(createAndSaveMate(goal, createAndSaveUser()));
        }
        ReflectionTestUtils.setField(skippedMates.get(0).getAttendance(), "skippedDayCount", 100);
    }

    private Goal createAndSaveGoal() {
        return goalRepository.save(TestEntityFactory.goal(0L, "goal"));
    }

    private User createAndSaveUser() {
        return userRepository.save(TestEntityFactory.user(0L, "invitee"));
    }

    private Mate createAndSaveMate(Goal goal, User user) {
        return mateRepository.save(goal.createMate(user));
    }
}