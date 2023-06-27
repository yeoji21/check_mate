package checkmate.goal.domain;

import static org.assertj.core.api.Assertions.assertThat;

import checkmate.TestEntityFactory;
import checkmate.post.domain.Post;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LikeCountConditionTest {

    @Test
    @DisplayName("좋아요 개수 인증 조건 검사 - 성공")
    void checkLikeConditionsSuccess() throws Exception {
        //given
        Goal goal = createGoal();
        goal.addCondition(new LikeCountCondition(5));

        Post post = createPost(goal);
        for (int i = 0; i < 5; i++) {
            post.addLikes(i);
        }

        //when
        boolean check = goal.checkConditions(post);

        //then
        assertThat(check).isTrue();
    }

    @Test
    @DisplayName("좋아요 개수 인증 조건 검사 - 실패")
    void checkLikeConditionsFailWhenInsufficientLikes() throws Exception {
        //given
        Goal goal = createGoal();
        goal.addCondition(new LikeCountCondition(5));

        Post post = createPost(goal);

        //when
        goal.checkConditions(post);

        //then
        assertThat(post.isChecked()).isFalse();
    }

    private Post createPost(Goal goal) {
        return TestEntityFactory.post(goal.createMate(TestEntityFactory.user(1L, "user")));
    }

    private Goal createGoal() {
        return TestEntityFactory.goal(1L, "goal");
    }
}