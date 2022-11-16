package checkmate.notification.application;

import checkmate.TestEntityFactory;
import checkmate.goal.domain.Goal;
import checkmate.goal.domain.TeamMate;
import checkmate.notification.application.dto.NotificationQueryMapper;
import checkmate.notification.application.dto.response.NotificationInfo;
import checkmate.notification.domain.Notification;
import checkmate.notification.domain.NotificationReceiver;
import checkmate.notification.domain.NotificationRepository;
import checkmate.notification.domain.NotificationType;
import checkmate.notification.infrastructure.NotificationQueryDao;
import checkmate.user.domain.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
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
        goal = TestEntityFactory.goal(1L, "test goal");
        teamMate = TestEntityFactory.teamMate(1L, user.getId());
        goal.addTeamMate(teamMate);
        teamMate.initiateGoal(0);
    }

    @Test
    void 단건_알림_조회_테스트() throws Exception{
        //given
        NotificationReceiver receiver = new NotificationReceiver(1L);
        Notification notification = Notification.builder()
                .userId(1L)
                .title("title")
                .body("body")
                .type(NotificationType.POST_UPLOAD)
                .receivers(List.of(receiver))
                .build();
        notification.addAttribute("goalId", 1L);

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
}