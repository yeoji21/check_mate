package checkmate.post.application;

import checkmate.TestEntityFactory;
import checkmate.goal.domain.*;
import checkmate.post.application.dto.request.PostUploadCommand;
import checkmate.post.domain.Likes;
import checkmate.post.domain.Post;
import checkmate.post.domain.PostRepository;
import checkmate.user.domain.UserRepository;
import org.junit.jupiter.api.BeforeEach;
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
    @InjectMocks private PostCommandService postCommandService;
    @Mock private PostRepository postRepository;
    @Mock private UserRepository userRepository;
    @Mock private GoalRepository goalRepository;
    @Mock private TeamMateRepository teamMateRepository;
    @Mock private PostVerificationService postVerificationService;
    @Mock private ApplicationEventPublisher eventPublisher;

    private Goal goal;
    private TeamMate teamMate;
    @BeforeEach
    void setUp() {
        teamMate = TestEntityFactory.teamMate(1L, 1L);
        goal = TestEntityFactory.goal(1L, "자바의 정석 스터디");
        goal.addTeamMate(teamMate);
    }

    @Test
    void 목표인증_저장_테스트() throws Exception{
        //given
        PostUploadCommand dto = getPostRegisterDto();
        given(teamMateRepository.findTeamMate(any(Long.class))).willReturn(Optional.ofNullable(teamMate));
        given(userRepository.findById(any(Long.class))).willReturn(Optional.ofNullable(TestEntityFactory.user(1L, "tester")));

        //when
        postCommandService.upload(dto);

        //then
        verify(postRepository).save(any());
        verify(postVerificationService).verify(any(Post.class), any(List.class));
    }

    @Test
    void 좋아요_테스트() throws Exception{
        //given
        TeamMate teamMate = TestEntityFactory.teamMate(1L, 1L);
        Goal goal = TestEntityFactory.goal(1L, "자바의 정석 스터디");
        goal.addTeamMate(teamMate);
        Post post = Post.builder().teamMate(teamMate).text("post body text").build();
        ReflectionTestUtils.setField(post, "id", 1L);

        given(postRepository.findById(any(Long.class))).willReturn(Optional.of(post));
        given(teamMateRepository.findTeamMate(any(Long.class), any(Long.class))).willReturn(Optional.of(teamMate));

        //when
        postCommandService.like(teamMate.getUserId(), post.getId());

        //then
        assertThat(post.getLikes().size()).isEqualTo(1);
    }

    @Test
    void 좋아요_취소_테스트() throws Exception{
        //given
        TeamMate teamMate = TestEntityFactory.teamMate(1L, 1L);
        Goal goal = TestEntityFactory.goal(1L, "자바의 정석 스터디");
        goal.addTeamMate(teamMate);
        Post post = Post.builder().teamMate(teamMate).text("post body text").build();
        ReflectionTestUtils.setField(post, "id", 1L);
        post.addLikes(new Likes(teamMate.getUserId()));

        given(postRepository.findById(any(Long.class))).willReturn(Optional.of(post));
        given(teamMateRepository.findTeamMate(any(Long.class), any(Long.class))).willReturn(Optional.of(teamMate));

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

    private Post getPost(PostUploadCommand dto, TeamMate teamMate) {
        return Post.builder()
                .teamMate(teamMate)
                .text(dto.getText())
                .build();
    }
}