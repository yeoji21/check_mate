package checkmate.mate.infra;

import static checkmate.mate.domain.QMate.mate;
import static org.assertj.core.api.Assertions.assertThat;

import checkmate.RepositoryTest;
import checkmate.TestEntityFactory;
import checkmate.exception.NotFoundException;
import checkmate.exception.code.ErrorCode;
import checkmate.goal.domain.CheckDaysConverter;
import checkmate.goal.domain.Goal;
import checkmate.goal.domain.Goal.GoalStatus;
import checkmate.goal.domain.GoalCheckDays;
import checkmate.mate.domain.Mate;
import checkmate.mate.domain.MateStatus;
import checkmate.post.domain.Post;
import checkmate.user.domain.User;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class MateRepositoryTest extends RepositoryTest {

    @Test
    @DisplayName("팀원 조회 - mateId로 조회")
    void find() throws Exception {
        //given
        Goal goal = createGoal();
        Mate mate = createMate(goal);

        em.flush();
        em.clear();

        //when
        Mate foundMate = mateRepository.findById(mate.getId())
            .orElseThrow(IllegalArgumentException::new);

        //then
        assertThat(foundMate).isEqualTo(mate);
    }

    @Test
    @DisplayName("팀원 조회 (목표 fetch join) - mateId로 조회")
    void findWithGoal_mateId() throws Exception {
        //given
        Goal goal = createGoal();
        Mate mate = createOngoingMate(goal);

        em.flush();
        em.clear();
        //when
        Mate findMate = mateRepository.findWithGoal(mate.getId())
            .orElseThrow(() -> new NotFoundException(ErrorCode.MATE_NOT_FOUND, mate.getId()));
        //then
        assertThat(findMate.getGoal().getId()).isEqualTo(goal.getId());
        assertThat(findMate.getId()).isEqualTo(mate.getId());
    }

    @Test
    @DisplayName("팀원 조회 (목표 fetch join) - goalId, userId로 조회")
    void findWithGoal_goalId_userId() throws Exception {
        //given
        Goal goal = createGoal();
        Mate mate = createOngoingMate(goal);

        em.flush();
        em.clear();
        //when
        Mate findMate = mateRepository.findWithGoal(goal.getId(), mate.getUserId())
            .orElseThrow(() -> new NotFoundException(ErrorCode.MATE_NOT_FOUND, mate.getId()));
        //then
        assertThat(findMate.getGoal().getId()).isEqualTo(goal.getId());
        assertThat(findMate.getId()).isEqualTo(mate.getId());
    }

    @Test
    @DisplayName("어제가 인증하지 않은 팀원 목록 조회")
    void findYesterdaySkippedMates() throws Exception {
        //given
        Goal yesterDayGoal = createGoal();
        Goal notYesterDayGoal = createGoal();
        ReflectionTestUtils.setField(notYesterDayGoal, "checkDays",
            new GoalCheckDays(Collections.singletonList(LocalDate.now().plusDays(1))));

        createOngoingMate(yesterDayGoal);
        createOngoingMate(yesterDayGoal);
        createOngoingMate(notYesterDayGoal);
        Mate mate = createOngoingMate(yesterDayGoal);
        createYesterDayUploadedPost(mate);

        em.flush();
        em.clear();

        //when
        List<Mate> mates = mateRepository.findYesterdaySkippedMates();

        //then
        assertThat(mates).hasSize(2);
        assertThat(mates)
            .allMatch(m -> CheckDaysConverter.isWorkingDay(m.getGoal().getCheckDays().intValue(),
                LocalDate.now().minusDays(1)))
            .allMatch(m -> m.getLastUploadDate() != LocalDate.now().minusDays(1))
            .allMatch(m -> m.getStatus() == MateStatus.ONGOING)
            .allMatch(m -> m.getGoal().getStatus() == GoalStatus.ONGOING);
    }

    @Test
    @DisplayName("인증하지 않은 팀원들 검사 스케쥴러")
    void increaseSkippedDayCount() throws Exception {
        //given
        Goal goal = createGoal();
        Mate mate1 = createOngoingMate(goal);
        Mate mate2 = createOngoingMate(goal);
        Mate mate3 = createOngoingMate(goal);
        Mate mate4 = createOngoingMate(goal);

        //when
        mateRepository.increaseSkippedDayCount(List.of(mate1, mate2, mate3, mate4));

        //then
        List<Mate> findMates = em.createQuery("select m from Mate m where m.id in :mateIds",
                Mate.class)
            .setParameter("mateIds",
                List.of(mate1.getId(), mate2.getId(), mate3.getId(), mate4.getId()))
            .getResultList();
        assertThat(findMates).allMatch(m -> m.getSkippedDays() == 1);
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
        mateRepository.updateLimitOveredMates(mates);

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
        User user = TestEntityFactory.user(null, UUID.randomUUID().toString());
        em.persist(user);
        Mate mate = goal.join(user);
        ReflectionTestUtils.setField(mate, "status", MateStatus.ONGOING);
        em.persist(mate);
        return mate;
    }

    private void createYesterDayUploadedPost(Mate mate) {
        Post post = TestEntityFactory.post(mate);
        ReflectionTestUtils.setField(post, "checked", true);
        ReflectionTestUtils.setField(post, "createdDate", LocalDate.now().minusDays(1));
        em.persist(post);
    }
}