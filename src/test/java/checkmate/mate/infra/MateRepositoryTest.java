package checkmate.mate.infra;

import checkmate.RepositoryTest;
import checkmate.TestEntityFactory;
import checkmate.exception.NotFoundException;
import checkmate.exception.code.ErrorCode;
import checkmate.goal.domain.Goal;
import checkmate.mate.domain.Mate;
import checkmate.mate.domain.MateStatus;
import checkmate.post.domain.Post;
import checkmate.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static checkmate.mate.domain.QMate.mate;
import static org.assertj.core.api.Assertions.assertThat;

class MateRepositoryTest extends RepositoryTest {
    @Test
    @DisplayName("팀원 조회")
    void find() throws Exception {
        //given
        Goal goal = createGoal();
        Mate mate = createMate(goal);

        em.flush();
        em.clear();

        //when
        Mate foundMate = mateRepository.find(mate.getId()).orElseThrow(IllegalArgumentException::new);

        //then
        assertThat(foundMate).isEqualTo(mate);
    }

    @Test
    @DisplayName("존재하는 팀원인지 여부 조회 - 존재")
    void isExistMate() throws Exception {
        //given
        Goal goal = createGoal();
        Mate mate = createOngoingMate(goal);
        em.flush();
        em.clear();
        //when
        boolean existTeamMate = mateRepository.existOngoingMate(goal.getId(), mate.getUserId());
        //then
        assertThat(existTeamMate).isTrue();
    }

    @Test
    @DisplayName("존재하는 팀원인지 여부 조회 - 존재 x (진행 중이 아님)")
    void isExistMate_not_ongoing() throws Exception {
        //given
        Goal goal = createGoal();
        Mate mate = createMate(goal);

        em.flush();
        em.clear();
        //when
        boolean existTeamMate = mateRepository.existOngoingMate(goal.getId(), mate.getUserId());
        //then
        assertThat(existTeamMate).isFalse();
    }

    @Test
    @DisplayName("존재하는 팀원인지 여부 조회 - 존재 x")
    void isExistMate_not_exist() throws Exception {
        //given
        Goal goal = createGoal();
        em.flush();
        em.clear();
        //when
        boolean existTeamMate = mateRepository.existOngoingMate(goal.getId(), 22L);
        //then
        assertThat(existTeamMate).isFalse();
    }

    @Test
    @DisplayName("팀원 조회 (목표 fetch join)")
    void findMateWithGoal() throws Exception {
        //given
        Goal goal = createGoal();
        Mate mate = createOngoingMate(goal);

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
    @DisplayName("인증하지 않은 팀원들 검사 스케쥴러")
    void updateYesterdayHookyMates() throws Exception {
        //given
        Goal goal = createGoal();
        Mate mate1 = createOngoingMate(goal);
        Mate mate2 = createOngoingMate(goal);
        Mate notUploadedMate = createOngoingMate(goal);
        Mate notCheckedMate = createOngoingMate(goal);

        Post post1 = TestEntityFactory.post(mate1);
        post1.check();
        em.persist(post1);

        Post post2 = TestEntityFactory.post(mate2);
        post2.check();
        em.persist(post2);

        Post post3 = TestEntityFactory.post(notCheckedMate);
        em.persist(post3);

        em.flush();
        em.clear();

        //when
        List<Mate> hookyMates = mateRepository.updateYesterdaySkippedMates();

        //then
        assertThat(hookyMates.size()).isEqualTo(2);
        assertThat(hookyMates).contains(notUploadedMate, notCheckedMate);
    }

    @Test
    @DisplayName("팀원 제거 스케쥴러")
    void eliminateOveredMates() throws Exception {
        //given
        Goal goal = createGoal();
        Mate mate1 = createOngoingMate(goal);
        Mate mate2 = createOngoingMate(goal);
        em.flush();
        em.clear();

        queryFactory.update(mate)
                .where(mate.id.in(mate1.getId(), mate2.getId()))
                .set(mate.progress.skippedDayCount, 50)
                .execute();

        //when
        List<Mate> mates = queryFactory.selectFrom(mate).fetch();
        mateRepository.eliminateOveredMates(mates);

        //then
        assertThat(mates.size()).isEqualTo(2);
        assertThat(mates).contains(mate1, mate2);
    }

    private Mate createMate(Goal goal) {
        User user = TestEntityFactory.user(null, "user");
        em.persist(user);
        Mate mate = goal.join(user);
        em.persist(mate);
        return mate;
    }

    private Goal createGoal() {
        Goal goal = TestEntityFactory.goal(null, "goal");
        em.persist(goal);
        return goal;
    }

    private Mate createOngoingMate(Goal goal) {
        User user = TestEntityFactory.user(null, "user" + Math.random() % 100);
        em.persist(user);
        Mate mate = goal.join(user);
        ReflectionTestUtils.setField(mate, "status", MateStatus.ONGOING);
        em.persist(mate);
        return mate;
    }
}