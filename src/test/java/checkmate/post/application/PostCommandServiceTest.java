package checkmate.post.application;

import checkmate.TestEntityFactory;
import checkmate.goal.domain.Goal;
import checkmate.goal.domain.GoalRepository;
import checkmate.mate.domain.Mate;
import checkmate.mate.domain.MateRepository;
import checkmate.mate.infra.MateQueryDao;
import checkmate.notification.domain.event.PushNotificationCreatedEvent;
import checkmate.post.application.dto.request.PostUploadCommand;
import checkmate.post.domain.Likes;
import checkmate.post.domain.Post;
import checkmate.post.domain.PostRepository;
import checkmate.user.domain.UserRepository;
import org.junit.jupiter.api.DisplayName;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
class PostCommandServiceTest {
    @InjectMocks
    private PostCommandService postCommandService;
    @Mock
    private PostRepository postRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private GoalRepository goalRepository;
    @Mock
    private MateRepository mateRepository;
    @Mock
    private MateQueryDao mateQueryDao;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Test
    @DisplayName("목표 인증 업로드")
    void upload() throws Exception {
        //given
        Mate mate = createMate();
        given(mateRepository.findMateWithGoal(any(Long.class))).willReturn(Optional.ofNullable(mate));
        given(userRepository.findById(any(Long.class)))
                .willReturn(Optional.ofNullable(TestEntityFactory.user(mate.getUserId(), "tester")));
        given(goalRepository.findWithConditions(any(Long.class))).willReturn(Optional.of(mate.getGoal()));
        given(mateQueryDao.findMateUserIds(any(Long.class))).willReturn(List.of(1L, 2L, 3L));
        
        //when
        postCommandService.upload(createPostUploadCommand(mate));

        //then
        verify(postRepository).save(any());
        verify(eventPublisher).publishEvent(any(PushNotificationCreatedEvent.class));
    }

    @Test
    @DisplayName("좋아요 추가")
    void like() throws Exception {
        //given
        Mate mate = createMate();
        Post post = createPost(mate);

        given(postRepository.findById(any(Long.class))).willReturn(Optional.of(post));
        given(mateRepository.findMateWithGoal(any(Long.class), any(Long.class))).willReturn(Optional.of(mate));
        given(goalRepository.findWithConditions(any(Long.class))).willReturn(Optional.of(mate.getGoal()));

        //when
        postCommandService.like(mate.getUserId(), post.getId());

        //then
        assertThat(post.getLikes().size()).isEqualTo(1);
    }

    @Test
    @DisplayName("좋아요 취소")
    void unlike() throws Exception {
        //given
        Mate mate = createMate();
        Post post = createPost(mate);
        post.addLikes(new Likes(mate.getUserId()));

        given(postRepository.findById(any(Long.class))).willReturn(Optional.of(post));
        given(mateRepository.findMateWithGoal(any(Long.class), any(Long.class))).willReturn(Optional.of(mate));
        given(goalRepository.findWithConditions(any(Long.class))).willReturn(Optional.of(mate.getGoal()));

        //when
        postCommandService.unlike(mate.getUserId(), post.getId());

        //then
        assertThat(post.getLikes().size()).isEqualTo(0);
    }


    private Post createPost(Mate mate) {
        Post post = TestEntityFactory.post(mate);
        ReflectionTestUtils.setField(post, "id", 1L);
        return post;
    }

    private Mate createMate() {
        Goal goal = TestEntityFactory.goal(1L, "자바의 정석 스터디");
        Mate mate = goal.join(TestEntityFactory.user(1L, "user"));
        ReflectionTestUtils.setField(mate, "id", 1L);
        return mate;
    }

    private PostUploadCommand createPostUploadCommand(Mate mate) throws IOException {
        return new PostUploadCommand(mate.getUserId(),
                mate.getId(),
                List.of(new MockMultipartFile("filename", InputStream.nullInputStream())),
                "content");
    }
}