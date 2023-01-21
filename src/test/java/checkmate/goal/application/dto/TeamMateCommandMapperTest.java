package checkmate.goal.application.dto;

import checkmate.TestEntityFactory;
import checkmate.goal.application.dto.response.TeamMateAcceptResult;
import checkmate.goal.domain.Goal;
import checkmate.goal.domain.TeamMate;
import checkmate.notification.domain.factory.dto.ExpulsionGoalNotificationDto;
import checkmate.notification.domain.factory.dto.InviteReplyNotificationDto;
import checkmate.notification.domain.factory.dto.TeamMateInviteNotificationDto;
import checkmate.user.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class TeamMateCommandMapperTest {
    private static final TeamMateCommandMapper mapper = TeamMateCommandMapper.INSTANCE;

    @Test
    void teamMateInviteNotificationDto() throws Exception{
        //given
        User inviter = TestEntityFactory.user(1L, "inviter");
        Goal goal = TestEntityFactory.goal(2L, "goal");
        User receiver = TestEntityFactory.user(3L, "receiver");
        TeamMate invitee = goal.join(receiver);
        ReflectionTestUtils.setField(invitee, "id", 4L);

        //when
        TeamMateInviteNotificationDto dto = mapper.toNotificationDto(inviter, invitee);

        //then
        isEqualTo(dto.inviterUserId(), inviter.getId());
        isEqualTo(dto.inviteeTeamMateId(), invitee.getId());
        isEqualTo(dto.inviterNickname(), inviter.getNickname());
        isEqualTo(dto.goalTitle(), goal.getTitle());
        isEqualTo(dto.inviteeUserId(), invitee.getUserId());
    }

    @Test
    void inviteReplyNotificationDto() throws Exception{
        //given
        Goal goal = TestEntityFactory.goal(1L, "goal");
        User receiver = TestEntityFactory.user(2L, "receiver");
        TeamMate invitee = goal.join(receiver);
        long inviterUserId = 3L;
        boolean accept = true;

        //when
        InviteReplyNotificationDto dto = mapper.toNotificationDto(invitee, receiver.getNickname(), inviterUserId, accept);

        //then
        isEqualTo(dto.inviteeUserId(), invitee.getUserId());
        isEqualTo(dto.inviteeNickname(), receiver.getNickname());
        isEqualTo(dto.goalId(), goal.getId());
        isEqualTo(dto.goalTitle(), goal.getTitle());
        isEqualTo(dto.inviterUserId(), inviterUserId);
        isEqualTo(dto.accept(), accept);
    }

    @Test
    void expulsionGoalNotificationDto() throws Exception{
        //given
        Goal goal = TestEntityFactory.goal(1L, "goal");
        TeamMate teamMate = goal.join(TestEntityFactory.user(2L, "receiver"));
        ReflectionTestUtils.setField(teamMate, "id", 3L);

        //when
        ExpulsionGoalNotificationDto dto = mapper.toNotificationDto(teamMate);

        //then
        isEqualTo(dto.userId(), teamMate.getUserId());
        isEqualTo(dto.teamMateId(), teamMate.getId());
        isEqualTo(dto.goalTitle(), goal.getTitle());
    }

    @Test
    void teamMateAcceptResult() throws Exception{
        //given
        Goal goal = TestEntityFactory.goal(1L, "goal");
        TeamMate teamMate = goal.join(TestEntityFactory.user(2L, "receiver"));
        ReflectionTestUtils.setField(teamMate, "id", 3L);

        //when
        TeamMateAcceptResult result = mapper.toResult(teamMate);

        //then
        isEqualTo(result.teamMateId(), teamMate.getId());
        isEqualTo(result.goalId(), goal.getId());
    }

    private void isEqualTo(Object A, Object B) {
        assertThat(A).isEqualTo(B);
    }
}