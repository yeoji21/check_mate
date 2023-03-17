package checkmate.post.domain;

import checkmate.TestEntityFactory;
import checkmate.exception.BusinessException;
import checkmate.exception.code.ErrorCode;
import checkmate.goal.domain.Goal;
import checkmate.mate.domain.Mate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PostTest {
    @Test
    @DisplayName("게시글 좋아요 추가")
    void addLikes() throws Exception {
        //given
        Post post = createPost();

        //when
        post.addLikes(new Likes(2L));
        post.addLikes(new Likes(3L));

        //then
        List<Likes> likes = post.getLikes();
        assertThat(likes).hasSize(2);
        assertThat(likes).allMatch(like -> like.getPost() == post);
    }

    @Test
    @DisplayName("게시글 좋아요 추가 - 수정 기간 초과")
    void addLikes_period() throws Exception {
        //given
        Post post = createPost();
        ReflectionTestUtils.setField(post, "uploadedDate", LocalDate.now().minusDays(5));

        //when
        BusinessException exception = assertThrows(BusinessException.class,
                () -> post.addLikes(new Likes(2L)));

        //then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.POST_LIKES_UPDATE);
    }

    @Test
    @DisplayName("게시글 좋아요 제거")
    void removeLikes() throws Exception {
        //given
        Post post = createPost();
        post.addLikes(new Likes(1L));
        post.addLikes(new Likes(2L));
        post.addLikes(new Likes(3L));

        //when
        post.removeLikes(1L);

        //then
        List<Likes> likes = post.getLikes();
        assertThat(likes.size()).isEqualTo(2);
        assertThat(likes).allMatch(like -> like.getUserId() != 1L);
    }

    @Test
    @DisplayName("게시글 좋아요 제거 - 좋아요 누르지 않은 유저")
    void removeLikes_not_liked_user() throws Exception {
        //given
        Post post = createPost();
        post.addLikes(new Likes(1L));
        post.addLikes(new Likes(2L));

        //when //then
        assertThrows(IllegalArgumentException.class, () -> post.removeLikes(3L));
    }

    @Test
    @DisplayName("게시글 좋아요 제거 - 좋아요 누르지 않은 유저")
    void removeLikes_period_exception() throws Exception {
        //given
        Post post = createPost();
        post.addLikes(new Likes(1L));
        ReflectionTestUtils.setField(post, "uploadedDate", LocalDate.now().minusDays(5));
        //when
        BusinessException exception = assertThrows(BusinessException.class,
                () -> post.removeLikes(1L));
        //then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.POST_LIKES_UPDATE);
    }

    private Post createPost() {
        Goal goal = TestEntityFactory.goal(1L, "test");
        Mate mate = goal.join(TestEntityFactory.user(1L, "user"));
        return TestEntityFactory.post(mate);
    }
}





























