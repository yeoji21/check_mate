package checkmate.goal.domain;

import static org.assertj.core.api.Assertions.assertThat;

import checkmate.TestEntityFactory;
import checkmate.post.domain.Post;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class LikeCountConditionTest {

    @Test
    void post_that_created_past_than_yesterday_is_not_satisfied() throws Exception {
        //given
        LikeCountCondition sut = createLikeCountCondition(0);

        //when
        boolean satisfied = sut.satisfy(createTwoDaysAgoPost());

        //then
        assertThat(satisfied).isFalse();
    }

    @Test
    void post_with_more_or_equal_likes_than_the_minimum_likes_is_satisfied() throws Exception {
        //given
        LikeCountCondition sut = createLikeCountCondition(3);

        //when
        boolean satisfied = sut.satisfy(createThreeLikesPost());

        //then
        assertThat(satisfied).isTrue();
    }

    private LikeCountCondition createLikeCountCondition(int minimumLikes) {
        return new LikeCountCondition(createGoal(), minimumLikes);
    }

    private Post createThreeLikesPost() {
        Post post = createPost();
        post.addLikes(1L);
        post.addLikes(2L);
        post.addLikes(3L);
        return post;
    }

    private Post createTwoDaysAgoPost() {
        Post post = createPost();
        ReflectionTestUtils.setField(post, "createdDate", LocalDate.now().minusDays(2));
        return post;
    }

    private Post createPost() {
        return TestEntityFactory.post(createGoal().createMate(TestEntityFactory.user(1L, "user")));
    }

    private Goal createGoal() {
        return TestEntityFactory.goal(1L, "goal");
    }
}