package checkmate.goal.domain;

import checkmate.TestEntityFactory;
import checkmate.exception.BusinessException;
import checkmate.exception.code.ErrorCode;
import checkmate.user.domain.User;
import checkmate.user.domain.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class TeamMateInitiateManagerTest {
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private TeamMateInitiateManager teamMateInitiateManager;

    @Test
    @DisplayName("목표 시작 성공")
    void initiate_success() throws Exception {
        //given
        Goal goal = TestEntityFactory.goal(1L, "title");
        User user = TestEntityFactory.user(2L, "user");
        TeamMate teamMate = goal.join(user);
        given(userRepository.countOngoingGoals(any(Long.class))).willReturn(1);

        TeamMateStatus before = teamMate.getStatus();

        //when
        teamMateInitiateManager.initiate(teamMate);

        //then
        TeamMateStatus after = teamMate.getStatus();
        assertThat(before).isEqualTo(TeamMateStatus.WAITING);
        assertThat(after).isEqualTo(TeamMateStatus.ONGOING);
    }

    @Test
    @DisplayName("목표 시작 실패 - 진행 중인 목표 개수 초과")
    void initiate_ongoing_count_fail() throws Exception {
        //given
        Goal goal = TestEntityFactory.goal(1L, "title");
        User user = TestEntityFactory.user(2L, "user");
        TeamMate teamMate = goal.join(user);
        given(userRepository.countOngoingGoals(any(Long.class))).willReturn(10);

        //when then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> teamMateInitiateManager.initiate(teamMate));
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.EXCEED_GOAL_LIMIT);
    }

    @Test
    @DisplayName("목표 시작 실패 - 잘못된 팀원의 상태")
    void initiate_team_mate_status_fail() throws Exception {
        //given
        Goal goal = TestEntityFactory.goal(1L, "title");
        User user = TestEntityFactory.user(2L, "user");
        TeamMate teamMate = goal.join(user);
        ReflectionTestUtils.setField(teamMate, "status", TeamMateStatus.ONGOING);
        given(userRepository.countOngoingGoals(any(Long.class))).willReturn(1);

        //when then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> teamMateInitiateManager.initiate(teamMate));
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_TEAM_MATE_STATUS);
    }
}