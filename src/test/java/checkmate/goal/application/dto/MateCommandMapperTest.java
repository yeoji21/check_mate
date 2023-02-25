package checkmate.goal.application.dto;

import checkmate.MapperTest;
import checkmate.TestEntityFactory;
import checkmate.goal.domain.Goal;
import checkmate.mate.application.dto.MateCommandMapper;
import checkmate.mate.application.dto.response.MateAcceptResult;
import checkmate.mate.domain.Mate;
import checkmate.notification.domain.factory.dto.ExpulsionGoalNotificationDto;
import checkmate.notification.domain.factory.dto.InviteAcceptNotificationDto;
import checkmate.notification.domain.factory.dto.InviteRejectNotificationDto;
import checkmate.notification.domain.factory.dto.MateInviteNotificationDto;
import checkmate.user.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class MateCommandMapperTest extends MapperTest {
    private static final MateCommandMapper mapper = MateCommandMapper.INSTANCE;

    @Test
    void toAcceptNotificationDto() throws Exception {
        //given
        User inviter = TestEntityFactory.user(1L, "inviter");
        Goal goal = TestEntityFactory.goal(2L, "goal");
        User receiver = TestEntityFactory.user(3L, "receiver");
        Mate invitee = goal.join(receiver);

        //when
        InviteAcceptNotificationDto dto =
                mapper.toAcceptNotificationDto(invitee, receiver.getNickname(), inviter.getId());

        //then
        isEqualTo(dto.goalId(), goal.getId());
        isEqualTo(dto.inviteeUserId(), invitee.getUserId());
        isEqualTo(dto.inviteeNickname(), receiver.getNickname());
        isEqualTo(dto.goalTitle(), goal.getTitle());
        isEqualTo(dto.inviterUserId(), inviter.getId());
    }

    @Test
    void toRejectNotificationDto() throws Exception {
        //given
        User inviter = TestEntityFactory.user(1L, "inviter");
        Goal goal = TestEntityFactory.goal(2L, "goal");
        User receiver = TestEntityFactory.user(3L, "receiver");
        Mate invitee = goal.join(receiver);

        //when
        InviteRejectNotificationDto dto =
                mapper.toRejectNotificationDto(invitee, receiver.getNickname(), inviter.getId());

        //then
        isEqualTo(dto.goalId(), goal.getId());
        isEqualTo(dto.inviteeUserId(), invitee.getUserId());
        isEqualTo(dto.inviteeNickname(), receiver.getNickname());
        isEqualTo(dto.goalTitle(), goal.getTitle());
        isEqualTo(dto.inviterUserId(), inviter.getId());
    }

    @Test
    void teamMateInviteNotificationDto() throws Exception {
        //given
        User inviter = TestEntityFactory.user(1L, "inviter");
        Goal goal = TestEntityFactory.goal(2L, "goal");
        User receiver = TestEntityFactory.user(3L, "receiver");
        Mate invitee = goal.join(receiver);
        ReflectionTestUtils.setField(invitee, "id", 4L);

        //when
        MateInviteNotificationDto dto = mapper.toNotificationDto(inviter.getId(), inviter.getNickname(), invitee);

        //then
        isEqualTo(dto.inviterUserId(), inviter.getId());
        isEqualTo(dto.inviteeMateId(), invitee.getId());
        isEqualTo(dto.inviterNickname(), inviter.getNickname());
        isEqualTo(dto.goalTitle(), goal.getTitle());
        isEqualTo(dto.inviteeUserId(), invitee.getUserId());
    }

    @Test
    void expulsionGoalNotificationDto() throws Exception {
        //given
        Goal goal = TestEntityFactory.goal(1L, "goal");
        Mate mate = goal.join(TestEntityFactory.user(2L, "receiver"));
        ReflectionTestUtils.setField(mate, "id", 3L);

        //when
        ExpulsionGoalNotificationDto dto = mapper.toNotificationDto(mate);

        //then
        isEqualTo(dto.userId(), mate.getUserId());
        isEqualTo(dto.mateId(), mate.getId());
        isEqualTo(dto.goalTitle(), goal.getTitle());
    }

    @Test
    void teamMateAcceptResult() throws Exception {
        //given
        Goal goal = TestEntityFactory.goal(1L, "goal");
        Mate mate = goal.join(TestEntityFactory.user(2L, "receiver"));
        ReflectionTestUtils.setField(mate, "id", 3L);

        //when
        MateAcceptResult result = mapper.toResult(mate);

        //then
        isEqualTo(result.mateId(), mate.getId());
        isEqualTo(result.goalId(), goal.getId());
    }

}