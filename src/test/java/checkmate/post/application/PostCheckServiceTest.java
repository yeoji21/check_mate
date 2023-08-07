package checkmate.post.application;

import static org.assertj.core.api.Assertions.assertThat;

import checkmate.TestEntityFactory;
import checkmate.goal.domain.Goal;
import checkmate.goal.domain.LikeCountCondition;
import checkmate.goal.infra.FakeGoalRepository;
import checkmate.mate.domain.Mate;
import checkmate.post.domain.Post;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PostCheckServiceTest {

    private static final FakeGoalRepository goalRepository = new FakeGoalRepository();
    private static final PostCheckService sut = new PostCheckService(goalRepository);

    @Test
    void all_conditions_are_met_check_post() throws Exception {
        //given
        Goal goal = createLikeConditionGoal();
        Post post = createPost(goal);
        post.addLikes(1L);

        //when
        sut.updateCheckStatus(post);

        //then
        assertThat(post.isChecked()).isTrue();
    }

    @Test
    void any_of_the_condition_is_not_satisfied_uncheck_post() throws Exception {
        //given
        Goal goal = createLikeConditionGoal();
        Post post = createPost(goal);

        //when
        sut.updateCheckStatus(post);

        //then
        assertThat(post.isChecked()).isFalse();
    }

    private Post createPost(Goal goal) {
        return TestEntityFactory.post(createMate(goal));
    }

    private Mate createMate(Goal goal) {
        return goal.createMate(TestEntityFactory.user(1L, "user"));
    }

    private Goal createLikeConditionGoal() {
        Goal goal = TestEntityFactory.goal(null, "title");
        goal.addCondition(new LikeCountCondition(1));
        goalRepository.save(goal);
        return goal;
    }
}