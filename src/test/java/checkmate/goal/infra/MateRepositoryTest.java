package checkmate.goal.infra;

import checkmate.RepositoryTest;
import checkmate.TestEntityFactory;
import checkmate.exception.NotFoundException;
import checkmate.exception.code.ErrorCode;
import checkmate.goal.domain.Goal;
import checkmate.mate.domain.Mate;
import checkmate.mate.domain.MateStatus;
import checkmate.post.domain.Post;
import checkmate.user.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static checkmate.mate.domain.QMate.mate;
import static org.assertj.core.api.Assertions.assertThat;

class MateRepositoryTest extends RepositoryTest {
    @Test
    void isExistTeamMate() throws Exception {
        //given
        Goal goal = TestEntityFactory.goal(null, "testGoal");
        em.persist(goal);
        User user = TestEntityFactory.user(null, "tester");
        em.persist(user);

        Mate mate = goal.join(user);
        ReflectionTestUtils.setField(mate, "status", MateStatus.ONGOING);
        em.persist(mate);

        em.flush();
        em.clear();

        //when
        boolean existTeamMate = mateRepository.isExistMate(goal.getId(), user.getId());

        //then
        assertThat(existTeamMate).isTrue();
    }

    @Test
    void isExistTeamMate_not_ongoing() throws Exception {
        //given
        Goal goal = TestEntityFactory.goal(null, "testGoal");
        em.persist(goal);
        User user = TestEntityFactory.user(null, "tester");
        em.persist(user);

        Mate mate = goal.join(user);
        em.persist(mate);

        em.flush();
        em.clear();

        //when
        boolean existTeamMate = mateRepository.isExistMate(goal.getId(), user.getId());

        //then
        assertThat(existTeamMate).isFalse();
    }

    @Test
    void isExistTeamMate_not_exist() throws Exception {
        //given
        Goal goal = TestEntityFactory.goal(null, "testGoal");
        em.persist(goal);

        em.flush();
        em.clear();

        //when
        boolean existTeamMate = mateRepository.isExistMate(goal.getId(), 22L);

        //then
        assertThat(existTeamMate).isFalse();
    }

    @Test
    void findByTeamMateId() throws Exception {
        //given
        Goal goal = TestEntityFactory.goal(null, "testGoal");
        em.persist(goal);
        User user = TestEntityFactory.user(null, "tester");
        em.persist(user);

        Mate mate = goal.join(user);
        em.persist(mate);

        em.flush();
        em.clear();

        //when
        Mate findMate = mateRepository.findMateWithGoal(mate.getId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.MATE_NOT_FOUND, mate.getId()));

        //then
        assertThat(findMate.getGoal().getId()).isEqualTo(goal.getId());
        assertThat(findMate.getId()).isEqualTo(mate.getId());
    }

    @Test
    void updateYesterdayHookyTMs() throws Exception {
        //given
        Goal goal1 = TestEntityFactory.goal(null, "goal1");
        em.persist(goal1);
        Goal goal2 = TestEntityFactory.goal(null, "goal2");
        em.persist(goal2);

        User user1 = TestEntityFactory.user(null, "user1");
        em.persist(user1);
        Mate mate1 = goal1.join(user1);
        ReflectionTestUtils.setField(mate1, "status", MateStatus.ONGOING);
        em.persist(mate1);

        User user2 = TestEntityFactory.user(null, "user2");
        em.persist(user2);
        Mate mate2 = goal1.join(user2);
        ReflectionTestUtils.setField(mate2, "status", MateStatus.ONGOING);
        em.persist(mate2);

        User user3 = TestEntityFactory.user(null, "user3");
        em.persist(user3);
        Mate notUploadedTm = goal1.join(user3);
        ReflectionTestUtils.setField(notUploadedTm, "status", MateStatus.ONGOING);
        em.persist(notUploadedTm);

        User user4 = TestEntityFactory.user(null, "user4");
        em.persist(user4);
        Mate notCheckedTm = goal1.join(user4);
        ReflectionTestUtils.setField(notCheckedTm, "status", MateStatus.ONGOING);
        em.persist(notCheckedTm);

        Post post1 = TestEntityFactory.post(mate1);
        post1.check();
        em.persist(post1);

        Post post2 = TestEntityFactory.post(mate2);
        post2.check();
        em.persist(post2);

        Post post3 = TestEntityFactory.post(notCheckedTm);
        em.persist(post3);

        em.flush();
        em.clear();

        //when
        List<Mate> hookyMates = mateRepository.updateYesterdayHookyMates();

        //then
        assertThat(hookyMates.size()).isEqualTo(2);
        assertThat(hookyMates).contains(notUploadedTm, notCheckedTm);
    }

    @Test
    void eliminateOveredTMs() throws Exception {
        //given
        Goal goal = TestEntityFactory.goal(null, "goal1");
        em.persist(goal);

        User user1 = TestEntityFactory.user(null, "user1");
        em.persist(user1);
        Mate mate1 = goal.join(user1);
        ReflectionTestUtils.setField(mate1, "status", MateStatus.ONGOING);
        em.persist(mate1);

        User user2 = TestEntityFactory.user(null, "user2");
        em.persist(user2);
        Mate mate2 = goal.join(user2);
        ReflectionTestUtils.setField(mate2, "status", MateStatus.ONGOING);
        em.persist(mate2);

        User user3 = TestEntityFactory.user(null, "user3");
        em.persist(user3);
        Mate mate3 = goal.join(user3);
        ReflectionTestUtils.setField(mate3, "status", MateStatus.ONGOING);
        em.persist(mate3);

        User user4 = TestEntityFactory.user(null, "user4");
        em.persist(user4);
        Mate mate4 = goal.join(user4);
        ReflectionTestUtils.setField(mate4, "status", MateStatus.ONGOING);
        em.persist(mate4);

        em.flush();
        em.clear();

        queryFactory.update(mate)
                .where(mate.id.in(mate1.getId(), mate2.getId()))
                .set(mate.progress.skippedDayCount, 50)
                .execute();

        //when
        List<Mate> eliminators = mateRepository.eliminateOveredMates(queryFactory.selectFrom(mate).fetch());

        //then
        assertThat(eliminators.size()).isEqualTo(2);
        assertThat(eliminators).contains(mate1, mate2);
    }
}