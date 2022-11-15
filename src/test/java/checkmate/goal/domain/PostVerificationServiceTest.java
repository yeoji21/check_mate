package checkmate.goal.domain;

import checkmate.TestEntityFactory;
import checkmate.post.domain.Likes;
import checkmate.post.domain.Post;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PostVerificationServiceTest {
    PostVerificationService postVerificationService = new PostVerificationService();

    @Test
    void 조건_검사_성공_테스트() throws Exception{
        //given
        Goal goal = TestEntityFactory.goal(1L, "goal");
        TeamMate uploader = goal.join(TestEntityFactory.user(1L, "user"));
        Post post = TestEntityFactory.post(uploader);
        post.addLikes(new Likes(1L));
        post.addLikes(new Likes(2L));
        post.addLikes(new Likes(3L));

        LikeCountCondition condition = new LikeCountCondition(3);

        //when
        postVerificationService.verify(post, List.of(condition));

        //then
        assertThat(post.isChecked()).isTrue();
        assertThat(uploader.getWorkingDays()).isEqualTo(1);
    }

    @Test
    void 조건_검사_성공_테스트_v2() throws Exception{
        //given
        Goal goal = TestEntityFactory.goal(1L, "goal");
        TeamMate uploader = goal.join(TestEntityFactory.user(1L, "user"));
        Post post = TestEntityFactory.post(uploader);

        //when
        postVerificationService.verify(post, Collections.emptyList());

        //then
        assertThat(post.isChecked()).isTrue();
        assertThat(uploader.getWorkingDays()).isEqualTo(1);
    }

    @Test
    void 조건_검사_실패_테스트() throws Exception{
        //given
        Goal goal = TestEntityFactory.goal(1L, "goal");
        TeamMate uploader = goal.join(TestEntityFactory.user(1L, "user"));
        Post post = TestEntityFactory.post(uploader);
        post.addLikes(new Likes(1L));

        LikeCountCondition condition = new LikeCountCondition(3);

        //when
        postVerificationService.verify(post, List.of(condition));

        //then
        assertThat(post.isChecked()).isFalse();
        assertThat(uploader.getWorkingDays()).isEqualTo(0);
    }
}