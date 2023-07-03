package checkmate.post.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import checkmate.TestEntityFactory;
import checkmate.goal.domain.Goal;
import checkmate.mate.domain.Mate;
import checkmate.mate.domain.MateRepository;
import checkmate.mate.infra.FakeMateRepository;
import checkmate.mate.infra.MateQueryDao;
import checkmate.notification.domain.event.PushNotificationCreatedEvent;
import checkmate.notification.domain.factory.dto.PostUploadNotificationDto;
import checkmate.post.application.dto.request.PostCreateCommand;
import checkmate.post.domain.Post;
import checkmate.post.domain.PostRepository;
import checkmate.post.infrastructure.FakePostRepository;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mock.web.MockMultipartFile;


@ExtendWith(MockitoExtension.class)
class PostCommandServiceTest {

    @InjectMocks
    private PostCommandService postCommandService;
    @Spy
    private PostRepository postRepository = new FakePostRepository();
    @Spy
    private MateRepository mateRepository = new FakeMateRepository();
    @Mock
    private MateQueryDao mateQueryDao;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Test
    @DisplayName("목표 인증 업로드")
    void upload() throws Exception {
        //given
        Mate mate = createAndSaveMate();
        given(mateQueryDao.findPostUploadNotificationDto(any(Long.class))).willReturn(
            createNotificationDto(mate));
        given(mateQueryDao.findOngoingUserIds(any(Long.class))).willReturn(List.of(1L, 2L, 3L));

        //when
        postCommandService.create(createPostUploadCommand(mate));

        //then
        verify(postRepository).save(any());
        verify(eventPublisher).publishEvent(any(PushNotificationCreatedEvent.class));
    }

    @Test
    @DisplayName("좋아요 추가")
    void like() throws Exception {
        //given
        Mate mate = createAndSaveMate();
        Post post = createAndSavePost(mate);

        //when
        postCommandService.like(mate.getUserId(), post.getId());

        //then
        assertThat(post.getLikes().size()).isEqualTo(1);
    }

    @Test
    @DisplayName("좋아요 취소")
    void unlike() throws Exception {
        //given
        Mate mate = createAndSaveMate();
        Post post = createAndSavePost(mate);
        post.addLikes(mate.getUserId());

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

    private Post createAndSavePost(Mate mate) {
        Post post = TestEntityFactory.post(mate);
        postRepository.save(post);
        return post;
    }

    private Mate createAndSaveMate() {
        Goal goal = TestEntityFactory.goal(1L, "자바의 정석 스터디");
        Mate mate = goal.createMate(TestEntityFactory.user(1L, "user"));
        return mateRepository.save(mate);
    }

    private PostCreateCommand createPostUploadCommand(Mate mate) throws IOException {
        return new PostCreateCommand(mate.getUserId(),
            mate.getId(),
            List.of(new MockMultipartFile("filename", InputStream.nullInputStream())),
            "content");
    }
}