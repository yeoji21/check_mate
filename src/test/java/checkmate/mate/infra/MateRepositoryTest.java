package checkmate.mate.infra;

import static checkmate.mate.domain.QMate.mate;
import static org.assertj.core.api.Assertions.assertThat;

import checkmate.RepositoryTest;
import checkmate.TestEntityFactory;
import checkmate.exception.NotFoundException;
import checkmate.exception.code.ErrorCode;
import checkmate.goal.domain.Goal;
import checkmate.goal.domain.Goal.GoalStatus;
import checkmate.goal.domain.GoalCheckDays;
import checkmate.goal.domain.GoalCheckDays.CheckDaysConverter;
import checkmate.mate.domain.Mate;
import checkmate.mate.domain.Mate.MateStatus;
import checkmate.mate.domain.UninitiatedMate;
import checkmate.post.domain.Post;
import checkmate.user.domain.User;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class MateRepositoryTest extends RepositoryTest {

    @Test
    void select_mate_with_ongoing_goal_count() throws Exception {
        //given
        User user = createUser();
        Mate mate = createMate(user, MateStatus.ONGOING);
        createMate(user, MateStatus.ONGOING);
        createMate(user, MateStatus.OUT);

        //when
        UninitiatedMate uninitiatedMate = mateRepository.findUninitiateMate(mate.getId()).get();

        //then
        assertThat(uninitiatedMate.getOngoingGoalCount()).isEqualTo(2);
    }

    @Test
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
    void findYesterdaySkippedMates() throws Exception {
        //given
        Goal yesterDayGoal = createGoal();
        Goal notYesterDayGoal = createGoal();
        ReflectionTestUtils.setField(notYesterDayGoal, "checkDays", tomorrowCheckDay());

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
            .allMatch(m -> GoalCheckDays.ofKorean(m.getGoal().getCheckDays().toKorean())
                .isCheckDay(LocalDate.now().minusDays(1)))
            .allMatch(m -> m.getLastUploadDate() != LocalDate.now().minusDays(1))
            .allMatch(m -> m.getStatus() == MateStatus.ONGOING)
            .allMatch(m -> m.getGoal().getStatus() == GoalStatus.ONGOING);
    }

    @Test
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
        assertThat(findMates).allMatch(m -> m.getSkippedDayCount() == 1);
    }

    @Test
    void eliminateOveredMates() throws Exception {
        //given
        Goal goal = createGoal();
        Mate mate1 = createOngoingMate(goal);
        Mate mate2 = createOngoingMate(goal);
        em.flush();
        em.clear();

        queryFactory.update(mate)
            .where(mate.id.in(mate1.getId(), mate2.getId()))
            .set(mate.attendance.skippedDayCount, 50)
            .execute();

        //when
        List<Mate> mates = queryFactory.selectFrom(mate).fetch();
        mateRepository.updateLimitOveredMates(mates);

        //then
        assertThat(mates.size()).isEqualTo(2);
        assertThat(mates).contains(mate1, mate2);
    }

    private GoalCheckDays tomorrowCheckDay() {
        return GoalCheckDays.ofKorean(CheckDaysConverter.toKorean(LocalDate.now().plusDays(1)));
    }

    private Mate createMate(Goal goal) {
        Mate mate = goal.createMate(createUser());
        em.persist(mate);
        return mate;
    }

    private Mate createMate(User user, MateStatus status) {
        Mate mate = createGoal().createMate(user);
        ReflectionTestUtils.setField(mate, "status", status);
        em.persist(mate);
        return mate;
    }

    private Goal createGoal() {
        Goal goal = TestEntityFactory.goal(null, "goal");
        em.persist(goal);
        return goal;
    }

    private Mate createOngoingMate(Goal goal) {
        Mate mate = goal.createMate(createUser());
        ReflectionTestUtils.setField(mate, "status", MateStatus.ONGOING);
        em.persist(mate);
        return mate;
    }

    private User createUser() {
        User user = TestEntityFactory.user(null, UUID.randomUUID().toString());
        em.persist(user);
        return user;
    }

    private void createYesterDayUploadedPost(Mate mate) {
        Post post = TestEntityFactory.post(mate);
        ReflectionTestUtils.setField(post, "checked", true);
        ReflectionTestUtils.setField(post, "createdDate", LocalDate.now().minusDays(1));
        em.persist(post);
    }
}