package checkmate.goal.domain.service;

import checkmate.TestEntityFactory;
import checkmate.exception.UserAlreadyInGoalException;
import checkmate.goal.domain.Goal;
import checkmate.goal.domain.TeamMate;
import checkmate.goal.domain.TeamMateStatus;
import checkmate.user.domain.User;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;


class TeamMateInviteServiceTest {
    private TeamMateInviteService teamMateInviteService = new TeamMateInviteService();

    @Test
    void 이미_목표에_속한_유저_초대_테스트() throws Exception{
        //given
        Goal goal = TestEntityFactory.goal(1L, "testGoal");
        TeamMate teamMate = TestEntityFactory.teamMate(1L, 1L);
        goal.addTeamMate(teamMate);

        //when / then
        assertThrows(UserAlreadyInGoalException.class,
                () -> teamMateInviteService.invite(goal, Optional.of(teamMate), 1L));
    }

    @Test
    void 초대_거절했던_유저_초대_테스트() throws Exception{
        //given
        Goal goal = TestEntityFactory.goal(1L, "testGoal");
        User invitee = TestEntityFactory.user(5L, "invitee");
        TeamMate teamMate = TestEntityFactory.teamMate(1L, invitee.getId());
        teamMate.applyInviteReject();
        goal.addTeamMate(teamMate);

        //when
        teamMateInviteService.invite(goal, Optional.of(teamMate), 1L);

        //then
        assertThat(teamMate.getTeamMateStatus()).isEqualTo(TeamMateStatus.WAITING);
    }
}