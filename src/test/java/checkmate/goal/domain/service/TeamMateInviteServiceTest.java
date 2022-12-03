package checkmate.goal.domain.service;

import checkmate.TestEntityFactory;
import checkmate.exception.format.BusinessException;
import checkmate.exception.format.ErrorCode;
import checkmate.goal.domain.Goal;
import checkmate.goal.domain.TeamMate;
import checkmate.goal.domain.TeamMateRepository;
import checkmate.goal.domain.TeamMateStatus;
import checkmate.user.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class TeamMateInviteServiceTest {
    @Mock
    private TeamMateRepository teamMateRepository;
    @InjectMocks
    private TeamMateInviteService teamMateInviteService;

    @Test
    void 이미_목표에_속한_유저_초대_테스트() throws Exception{
        //given
        Goal goal = TestEntityFactory.goal(1L, "testGoal");
        User user = TestEntityFactory.user(1L, "user");
        TeamMate teamMate = goal.join(user);
        ReflectionTestUtils.setField(teamMate, "status", TeamMateStatus.ONGOING);
        
        //when / then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> teamMateInviteService.invite(goal, Optional.of(teamMate), user));
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ALREADY_IN_GOAL);
    }

    @Test
    void 초대_거절했던_유저_초대_테스트() throws Exception{
        //given
        Goal goal = TestEntityFactory.goal(1L, "testGoal");
        User invitee = TestEntityFactory.user(5L, "invitee");
        TeamMate teamMate = goal.join(invitee);
        ReflectionTestUtils.setField(teamMate, "status", TeamMateStatus.REJECT);

        //when
        teamMateInviteService.invite(goal, Optional.of(teamMate), invitee);

        //then
        assertThat(teamMate.getStatus()).isEqualTo(TeamMateStatus.WAITING);
    }
}