package checkmate.mate.infra;

import checkmate.RepositoryTest;
import checkmate.TestEntityFactory;
import checkmate.goal.domain.Goal;
import checkmate.mate.application.dto.response.MateScheduleInfo;
import checkmate.mate.application.dto.response.MateUploadInfo;
import checkmate.mate.domain.Mate;
import checkmate.mate.domain.MateStatus;
import checkmate.post.domain.Post;
import checkmate.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class MateQueryDaoTest extends RepositoryTest {
    @Test
    @DisplayName("존재하는 팀원인지 여부 조회 - 존재")
    void isExistMate() throws Exception {
        //given
        Goal goal = createGoal();
        Mate mate = createOngoingMate(goal);

        //when
        boolean existTeamMate = mateQueryDao.existOngoingMate(goal.getId(), mate.getUserId());
        //then
        assertThat(existTeamMate).isTrue();
    }

    @Test
    @DisplayName("존재하는 팀원인지 여부 조회 - 존재 x (진행 중이 아님)")
    void isExistMate_not_ongoing() throws Exception {
        //given
        Goal goal = createGoal();
        Mate mate = createMate(goal, "user");
        ReflectionTestUtils.setField(mate, "status", MateStatus.WAITING);

        //when
        boolean existTeamMate = mateQueryDao.existOngoingMate(goal.getId(), mate.getUserId());

        //then
        assertThat(existTeamMate).isFalse();
    }

    @Test
    @DisplayName("존재하는 팀원인지 여부 조회 - 존재 x")
    void isExistMate_not_exist() throws Exception {
        //given
        Goal goal = createGoal();

        //when
        boolean existTeamMate = mateQueryDao.existOngoingMate(goal.getId(), 22L);

        //then
        assertThat(existTeamMate).isFalse();
    }


    @Test
    @DisplayName("팀원의 목표 캘린더 조회")
    void findScheduleInfo() throws Exception {
        //given
        Goal goal = createGoal();
        Mate mate = createMate(goal, "user");
        createPost(mate);

        em.flush();
        em.clear();

        //when
        MateScheduleInfo info = mateQueryDao.findScheduleInfo(mate.getId())
                .orElseThrow(IllegalArgumentException::new);

        //then
        assertThat(info.getStartDate()).isEqualTo(goal.getStartDate());
        assertThat(info.getGoalSchedule()).isEqualTo(goal.getSchedule());
        assertThat(info.getMateSchedule().length()).isEqualTo(info.getGoalSchedule().length());
    }

    @Test
    @DisplayName("목표에 속한 팀원들의 userId 조회")
    void findMateUserIds() throws Exception {
        //given
        Goal goal = createGoal();
        Mate mate1 = createMate(goal, "user1");
        Mate mate2 = createMate(goal, "user2");
        Mate mate3 = createMate(goal, "user3");

        //when
        List<Long> userIds = mateQueryDao.findOngoingUserIds(goal.getId());

        //then
        assertThat(userIds).contains(mate1.getUserId(), mate2.getUserId(), mate3.getUserId());
    }

    @Test
    @DisplayName("팀원의 목표 인증 날짜 목록 조회")
    void findUploadedDates() throws Exception {
        //given
        Goal goal = createGoal();
        Mate mate = createMate(goal, "user");
        createPost(mate);
        Post yesterDayPost = createPost(mate);
        ReflectionTestUtils.setField(yesterDayPost, "createdDate", LocalDate.now().minusDays(1));

        em.flush();
        em.clear();

        //when
        List<LocalDate> uploadedDates = mateQueryDao.findUploadedDates(mate.getId());

        //then
        assertThat(uploadedDates.size()).isEqualTo(2);
        assertThat(uploadedDates).contains(LocalDate.now().minusDays(1), LocalDate.now());
    }

    @Test
    @DisplayName("팀원의 인증 정보 조회")
    void findTeamMateInfo() throws Exception {
        //given
        Goal goal = createGoal();
        for (int i = 0; i < 10; i++) {
            createMate(goal, "user" + i);
        }
        em.flush();
        em.clear();

        //when
        List<MateUploadInfo> uploadInfo = mateQueryDao.findUploadInfo(goal.getId());

        //then
        assertThat(uploadInfo.size()).isEqualTo(10);
        assertThat(uploadInfo).allMatch(info -> info.getNickname() != null);
    }

    @Test
    @DisplayName("팀원들의 닉네임 조회")
    void findTeamMateNicknames() throws Exception {
        //given
        List<Long> goalIds = createGoals();
        em.flush();
        em.clear();

        //when
        Map<Long, List<String>> mateNicknames = mateQueryDao.findMateNicknames(goalIds);

        //then
        assertThat(mateNicknames.keySet().size()).isEqualTo(3);
        mateNicknames.values().forEach(nicknames -> {
            assertThat(nicknames.size()).isEqualTo(10);
            assertThat(nicknames).allMatch(nickname -> nickname != null);
        });
    }

    private Mate createOngoingMate(Goal goal) {
        User user = TestEntityFactory.user(null, "user" + Math.random() % 100);
        em.persist(user);
        Mate mate = goal.join(user);
        ReflectionTestUtils.setField(mate, "status", MateStatus.ONGOING);
        em.persist(mate);
        return mate;
    }

    private Post createPost(Mate mate) {
        Post post = TestEntityFactory.post(mate);
        em.persist(post);
        return post;
    }

    private List<Long> createGoals() {
        List<Long> goalIds = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Goal goal = createGoal();
            goalIds.add(goal.getId());
            for (int j = 0; j < 10; j++) {
                createMate(goal, i + "user" + j);
            }
        }
        return goalIds;
    }

    private Mate createMate(Goal goal, String nickname) {
        User user = TestEntityFactory.user(null, nickname);
        em.persist(user);
        Mate mate = goal.join(user);
        ReflectionTestUtils.setField(mate, "status", MateStatus.ONGOING);
        em.persist(mate);
        return mate;
    }

    private Goal createGoal() {
        Goal goal = TestEntityFactory.goal(null, "goal");
        em.persist(goal);
        return goal;
    }
}