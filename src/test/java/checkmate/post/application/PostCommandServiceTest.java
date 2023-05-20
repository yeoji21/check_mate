package checkmate.post.application;

import checkmate.TestEntityFactory;
import checkmate.goal.domain.Goal;
import checkmate.mate.domain.Mate;
import checkmate.mate.domain.MateRepository;
import checkmate.mate.infra.MateQueryDao;
import checkmate.notification.domain.event.PushNotificationCreatedEvent;
import checkmate.notification.domain.factory.dto.PostUploadNotificationDto;
import checkmate.post.application.dto.request.PostUploadCommand;
import checkmate.post.domain.Post;
import checkmate.post.domain.PostRepository;
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
        given(mateRepository.findWithGoal(any(Long.class))).willReturn(Optional.ofNullable(mate));
        given(mateQueryDao.findPostUploadNotificationDto(any(Long.class))).willReturn(createNotificationDto(mate));
        given(mateQueryDao.findOngoingUserIds(any(Long.class))).willReturn(List.of(1L, 2L, 3L));

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

        given(postRepository.findWithLikes(any(Long.class))).willReturn(Optional.of(post));

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
        post.addLikes(mate.getUserId());

        given(postRepository.findWithLikes(any(Long.class))).willReturn(Optional.of(post));

        //when
        postCommandService.unlike(mate.getUserId(), post.getId());

        //then
        assertThat(post.getLikes().size()).isEqualTo(0);
    }


    private Optional<PostUploadNotificationDto> createNotificationDto(Mate mate) {
        PostUploadNotificationDto dto = PostUploadNotificationDto.builder()
                .uploaderUserId(mate.getUserId())
                .uploaderNickname("nickname")
                .goalId(mate.getGoal().getId())
                .goalTitle(mate.getGoal().getTitle())
                .build();
        dto.setMateUserIds(List.of(1L, 2L));
        return Optional.of(dto);
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