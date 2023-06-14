package checkmate.post.domain;

import static org.assertj.core.api.Assertions.assertThat;

import checkmate.RepositoryTest;
import checkmate.TestEntityFactory;
import checkmate.goal.domain.Goal;
import checkmate.mate.domain.Mate;
import checkmate.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PostRepositoryTest extends RepositoryTest {

    @Test
    @DisplayName("게시글과 좋아요 목록 조회 - postId")
    void findWithLikes() throws Exception {
        //given
        Mate mate = createMate();
        Post post = createPost(mate);
        setFiveLikes(post);

        //when
        Post findPost = postRepository.findWithLikes(post.getId())
            .orElseThrow(IllegalArgumentException::new);

        //then
        assertThat(post).isEqualTo(findPost);
        assertThat(findPost.getLikes()).hasSize(5);
    }

    private void setFiveLikes(Post post) {
        for (int i = 0; i < 5; i++) {
            post.addLikes(i);
        }
    }

    private Post createPost(Mate mate) {
        Post post = TestEntityFactory.post(mate);
        em.persist(post);
        return post;
    }

    private Mate createMate() {
        Goal goal = createGoal();
        User user = createUser();
        Mate mate = goal.createMate(user);
        em.persist(mate);
        return mate;
    }

    private User createUser() {
        User user = TestEntityFactory.user(null, "user");
        em.persist(user);
        return user;
    }

    private Goal createGoal() {
        Goal goal = TestEntityFactory.goal(null, "title");
        em.persist(goal);
        return goal;
    }
}