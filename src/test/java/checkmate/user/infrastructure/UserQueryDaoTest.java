package checkmate.user.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import checkmate.RepositoryTest;
import checkmate.TestEntityFactory;
import checkmate.goal.domain.Goal;
import checkmate.mate.domain.Mate;
import checkmate.mate.domain.Mate.MateStatus;
import checkmate.post.domain.Post;
import checkmate.user.application.dto.DailySchedule;
import checkmate.user.domain.User;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class UserQueryDaoTest extends RepositoryTest {

    @Test
    @DisplayName("유저가 진행 중인 목표 개수")
    void countOngoingGoals() throws Exception {
        //given
        User user = createUser();
        createMate(user, createGoal());
        createMate(user, createGoal());
        createMate(user, createGoal());

        //when
        int count = userQueryDao.countOngoingGoals(user.getId());

        //then
        assertThat(count).isEqualTo(3);
    }

    @Test
    @DisplayName("닉네임 중복 여부 조회")
    void isExistsNickname() throws Exception {
        //given
        User user = createUser();

        //when
        boolean exist = userQueryDao.isExistsNickname(user.getNickname());
        boolean notExist = userQueryDao.isExistsNickname("notExistNickname");

        //then
        assertThat(exist).isTrue();
        assertThat(notExist).isFalse();
    }

    @Test
    void find_schdule_by_userId_and_dates() throws Exception {
        //given
        List<LocalDate> dates = LocalDate.now().datesUntil(LocalDate.now().plusDays(5)).toList();

        User user = createUser();
        createCheckedPost(createMate(user, createGoal()), dates.get(0));
        createCheckedPost(createMate(user, createGoal()), dates.get(1));
        createCheckedPost(createMate(user, createGoal()), dates.get(2));

        //when
        List<DailySchedule> schedule = userQueryDao.findSchedule(user.getId(), dates);

        //then
        assertThat(schedule).hasSize(dates.size());
        assertThat(schedule).allMatch(daily -> daily.getGoals().size() == 3);
        
        assertThat(schedule.get(0).getGoals().get(0).isChecked()).isTrue();
        assertThat(schedule.get(0).getGoals().get(1).isChecked()).isFalse();
        assertThat(schedule.get(0).getGoals().get(2).isChecked()).isFalse();

        assertThat(schedule.get(1).getGoals().get(0).isChecked()).isFalse();
        assertThat(schedule.get(1).getGoals().get(1).isChecked()).isTrue();
        assertThat(schedule.get(1).getGoals().get(2).isChecked()).isFalse();

        assertThat(schedule.get(2).getGoals().get(0).isChecked()).isFalse();
        assertThat(schedule.get(2).getGoals().get(1).isChecked()).isFalse();
        assertThat(schedule.get(2).getGoals().get(2).isChecked()).isTrue();
    }

    private void createCheckedPost(Mate mate, LocalDate date) {
        Post post = Post.builder()
            .mate(mate)
            .content(date.toString())
            .build();
        ReflectionTestUtils.setField(post, "createdDate", date);
        post.check();
        em.persist(post);
    }

    private Mate createMate(User user, Goal goal) {
        Mate mate = goal.createMate(user);
        ReflectionTestUtils.setField(mate, "status", MateStatus.ONGOING);
        em.persist(mate);
        return mate;
    }

    private Goal createGoal() {
        Goal goal = TestEntityFactory.goal(null, UUID.randomUUID().toString().substring(10));
        em.persist(goal);
        return goal;
    }

    private User createUser() {
        User user = TestEntityFactory.user(null, "ongoingTester");
        em.persist(user);
        return user;
    }
}