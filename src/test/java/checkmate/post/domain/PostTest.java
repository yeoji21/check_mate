package checkmate.post.domain;

import checkmate.TestEntityFactory;
import checkmate.goal.domain.Goal;
import checkmate.mate.domain.Mate;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PostTest {
    @Test
    void 좋아요_매핑_테스트() throws Exception {
        //given
        Goal goal = TestEntityFactory.goal(1L, "test");
        Mate mate = goal.join(TestEntityFactory.user(1L, "user"));
        Post post = TestEntityFactory.post(mate);

        post.addLikes(new Likes(2L));
        post.addLikes(new Likes(3L));
        //when
        List<Likes> likes = post.getLikes();
        //then
        assertThat(likes).hasSize(2);
        assertThat(likes.get(0).getPost()).isEqualTo(post);
    }

    @Test
    void 좋아요_가능_여부() throws Exception {
        Goal goal = TestEntityFactory.goal(1L, "test");
        Mate mate = goal.join(TestEntityFactory.user(1L, "user"));
        Post post = TestEntityFactory.post(mate);

        ReflectionTestUtils.setField(post, "uploadedDate", LocalDate.now().minusDays(5));
        assertThat(post.isLikeable()).isFalse();

        ReflectionTestUtils.setField(post, "uploadedDate", LocalDate.now().minusDays(2));
        assertThat(post.isLikeable()).isFalse();

        ReflectionTestUtils.setField(post, "uploadedDate", LocalDate.now());
        assertThat(post.isLikeable()).isTrue();

        ReflectionTestUtils.setField(post, "uploadedDate", LocalDate.now().minusDays(1));
        assertThat(post.isLikeable()).isTrue();
    }

    @Test
    void 좋아요_제거_테스트() throws Exception {
        //given
        Goal goal = TestEntityFactory.goal(1L, "test");
        Mate mate = goal.join(TestEntityFactory.user(1L, "user"));
        Post post = TestEntityFactory.post(mate);

        post.addLikes(new Likes(1L));
        post.addLikes(new Likes(2L));
        post.addLikes(new Likes(3L));

        //when
        post.removeLikes(1L);

        //then
        assertThat(post.getLikes().size()).isEqualTo(2);
        post.getLikes().forEach(like -> assertThat(like.getUserId()).isNotEqualTo(1L));
    }

    @Test
    void 좋아요_제거_실패_테스트() throws Exception {
        //given
        Goal goal = TestEntityFactory.goal(1L, "test");
        Mate mate = goal.join(TestEntityFactory.user(1L, "user"));
        Post post = TestEntityFactory.post(mate);

        post.addLikes(new Likes(1L));
        post.addLikes(new Likes(2L));
        post.addLikes(new Likes(3L));

        //when //then
        assertThrows(IllegalArgumentException.class, () -> post.removeLikes(5L));
    }
}





























