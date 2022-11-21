package checkmate.post.application;

import checkmate.TestEntityFactory;
import checkmate.goal.domain.*;
import checkmate.notification.domain.event.PushNotificationCreatedEvent;
import checkmate.post.application.dto.request.PostUploadCommand;
import checkmate.post.domain.Likes;
import checkmate.post.domain.Post;
import checkmate.post.domain.PostRepository;
import checkmate.user.domain.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
class PostCommandServiceTest {
    @InjectMocks private PostCommandService postCommandService;
    @Mock private PostRepository postRepository;
    @Mock private UserRepository userRepository;
    @Mock private GoalRepository goalRepository;
    @Mock private TeamMateRepository teamMateRepository;
    @Mock private PostVerificationService postVerificationService;
    @Mock private ApplicationEventPublisher eventPublisher;

    @Test
    void 목표인증_저장_테스트() throws Exception{
        //given
        PostUploadCommand dto = getPostRegisterDto();
        Goal goal = TestEntityFactory.goal(1L, "자바의 정석 스터디");
        TeamMate teamMate = goal.join(TestEntityFactory.user(1L, "user"));

        given(teamMateRepository.findTeamMateWithGoal(any(Long.class))).willReturn(Optional.ofNullable(teamMate));
        given(userRepository.findById(any(Long.class))).willReturn(Optional.ofNullable(TestEntityFactory.user(1L, "tester")));
        given(goalRepository.findConditions(any(Long.class))).willReturn(Collections.EMPTY_LIST);

        //when
        postCommandService.upload(dto);

        //then
        verify(postRepository).save(any());
        verify(postVerificationService).verify(any(Post.class), any(List.class));
        verify(eventPublisher).publishEvent(any(PushNotificationCreatedEvent.class));
    }

    @Test
    void 좋아요_테스트() throws Exception{
        //given
        Goal goal = TestEntityFactory.goal(1L, "자바의 정석 스터디");
        TeamMate teamMate = goal.join(TestEntityFactory.user(1L, "user"));
        Post post = Post.builder().teamMate(teamMate).content("post body text").build();
        ReflectionTestUtils.setField(post, "id", 1L);

        given(postRepository.findById(any(Long.class))).willReturn(Optional.of(post));
        given(teamMateRepository.findTeamMateWithGoal(any(Long.class), any(Long.class))).willReturn(Optional.of(teamMate));

        //when
        postCommandService.like(teamMate.getUserId(), post.getId());

        //then
        assertThat(post.getLikes().size()).isEqualTo(1);
    }

    @Test
    void 좋아요_취소_테스트() throws Exception{
        //given
        Goal goal = TestEntityFactory.goal(1L, "자바의 정석 스터디");
        TeamMate teamMate = goal.join(TestEntityFactory.user(1L, "user"));
        Post post = Post.builder().teamMate(teamMate).content("post body text").build();
        ReflectionTestUtils.setField(post, "id", 1L);
        post.addLikes(new Likes(teamMate.getUserId()));

        given(postRepository.findById(any(Long.class))).willReturn(Optional.of(post));
        given(teamMateRepository.findTeamMateWithGoal(any(Long.class), any(Long.class))).willReturn(Optional.of(teamMate));

        //when
        postCommandService.unlike(teamMate.getUserId(), post.getId());

        //then
        assertThat(post.getLikes().size()).isEqualTo(0);
    }

    private PostUploadCommand getPostRegisterDto() throws IOException {
        return new PostUploadCommand(1L,
                List.of(new MockMultipartFile("originalName", InputStream.nullInputStream())),
                "posting text");
    }
}