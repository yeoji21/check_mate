package checkmate.post.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import checkmate.TestEntityFactory;
import checkmate.exception.BusinessException;
import checkmate.exception.code.ErrorCode;
import checkmate.goal.domain.Goal;
import checkmate.mate.domain.Mate;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class PostTest {

    @Test
    @DisplayName("게시글 좋아요 추가")
    void addLikes() throws Exception {
        //given
        Post post = createPost();

        //when
        post.addLikes(2L);
        post.addLikes(3L);

        //then
        List<Likes> likes = post.getLikes();
        assertThat(likes)
            .hasSize(2)
            .allMatch(like -> like.getPost() == post);
    }

    @Test
    @DisplayName("게시글 좋아요 추가 - 수정 기간 초과")
    void addLikes_period() throws Exception {
        //given
        Post post = createPost();
        ReflectionTestUtils.setField(post, "createdDate", LocalDate.now().minusDays(5));

        //when
        BusinessException exception = assertThrows(BusinessException.class,
            () -> post.addLikes(1L));

        //then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.POST_LIKES_UPDATE);
    }

    @Test
    @DisplayName("게시글 좋아요 추가 - 이미 좋아요 누른 유저")
    void addLikes_duplicate() throws Exception {
        //given
        Post post = createPost();
        post.addLikes(1L);

        //when
        BusinessException exception = assertThrows(BusinessException.class,
            () -> post.addLikes(1L));

        //then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.POST_LIKES_UPDATE);
    }

    @Test
    @DisplayName("게시글 좋아요 제거")
    void removeLikes() throws Exception {
        //given
        Post post = createPost();
        post.addLikes(1L);
        post.addLikes(2L);
        post.addLikes(3L);

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
        post.addLikes(1L);
        post.addLikes(2L);

        //when //then
        BusinessException exception = assertThrows(BusinessException.class,
            () -> post.removeLikes(3L));
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.POST_LIKES_UPDATE);
    }

    @Test
    @DisplayName("게시글 좋아요 제거 - 좋아요 누르지 않은 유저")
    void removeLikes_period_exception() throws Exception {
        //given
        Post post = createPost();
        post.addLikes(1L);
        ReflectionTestUtils.setField(post, "createdDate", LocalDate.now().minusDays(5));
        //when
        BusinessException exception = assertThrows(BusinessException.class,
            () -> post.removeLikes(1L));
        //then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.POST_LIKES_UPDATE);
    }

    private Mate createMockMate(Goal goal) {
        Mate mate = mock(Mate.class);
        when(mate.getGoal()).thenReturn(goal);
        return mate;
    }

    private Post createPost() {
        Goal goal = TestEntityFactory.goal(1L, "goal");
        return TestEntityFactory.post(createMockMate(goal));
    }
}





























