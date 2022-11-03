package checkmate.notification.application;

import checkmate.TestEntityFactory;
import checkmate.goal.domain.Goal;
import checkmate.goal.domain.TeamMate;
import checkmate.notification.application.dto.NotificationQueryMapper;
import checkmate.notification.application.dto.response.NotificationInfo;
import checkmate.notification.domain.*;
import checkmate.notification.domain.factory.GoalCompleteNotificationFactory;
import checkmate.notification.domain.factory.dto.GoalCompleteNotificationDto;
import checkmate.notification.infrastructure.NotificationQueryDao;
import checkmate.user.domain.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class NotificationQueryServiceTest {
    @Mock private NotificationRepository notificationRepository;
    @Mock private NotificationQueryDao notificationQueryDao;
    private NotificationQueryMapper mapper = NotificationQueryMapper.INSTANCE;
    private NotificationQueryService notificationQueryService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private TeamMate teamMate;
    private Goal goal;

    @BeforeEach
    void setUp() {
        notificationQueryService = new NotificationQueryService(notificationRepository, notificationQueryDao, mapper);

        User user = TestEntityFactory.user(1L, "tester");
        teamMate = TestEntityFactory.teamMate(1L, user.getId());
        teamMate.changeToOngoingStatus(0);
        goal = TestEntityFactory.goal(1L, "test goal");
        goal.addTeamMate(teamMate);
    }

    @Test
    void 단건_알림_조회_테스트() throws Exception{
        //given
        Notification notification = TestEntityFactory.notification(1L, 1L, NotificationType.POST_UPLOAD);
        Map<String, String> map = new HashMap<>();
        map.put("goalId", "1L");
        ReflectionTestUtils.setField(notification, "attributes", new NotificationAttributes(map));

        NotificationReceiver receiver = new NotificationReceiver(1L);
        notification.setUpReceivers(List.of(receiver));

        given(notificationRepository.findNotificationReceiver(any(Long.class), any(Long.class))).willReturn(Optional.of(receiver));

        //when
        NotificationInfo response = notificationQueryService.findNotificationInfo(1L, 2L);
        HashMap<String, Long> attributes = objectMapper.readValue(response.getAttributes(), HashMap.class);

        //then
        assertThat(receiver.isChecked()).isTrue();
        assertThat(attributes.containsKey("goalId")).isTrue();
        assertThat(response.getTitle()).isEqualTo(notification.getTitle());
        assertThat(response.getBody()).isEqualTo(notification.getBody());
    }

    private List<Notification> getGoalCompleteNotifications() {
        GoalCompleteNotificationFactory factory = new GoalCompleteNotificationFactory();
        GoalCompleteNotificationDto dto = GoalCompleteNotificationDto.builder()
                .userId(teamMate.getUserId())
                .goalTitle(teamMate.getGoal().getTitle())
                .goalId(teamMate.getGoal().getId())
                .build();
        return List.of(factory.generate(dto), factory.generate(dto));
    }
}