package checkmate.post.application;

import static checkmate.notification.domain.NotificationType.POST_UPLOAD;

import checkmate.common.cache.CacheKey;
import checkmate.exception.NotFoundException;
import checkmate.exception.RuntimeIOException;
import checkmate.exception.code.ErrorCode;
import checkmate.mate.domain.Mate;
import checkmate.mate.domain.MateRepository;
import checkmate.mate.domain.Uploadable;
import checkmate.mate.infra.MateQueryDao;
import checkmate.notification.domain.event.PushNotificationCreatedEvent;
import checkmate.notification.domain.factory.dto.PostUploadNotificationDto;
import checkmate.post.application.dto.request.PostCreateCommand;
import checkmate.post.application.dto.response.PostCreateResult;
import checkmate.post.domain.Post;
import checkmate.post.domain.PostRepository;
import checkmate.post.domain.event.FileUploadedEvent;
import io.jsonwebtoken.lang.Assert;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RequiredArgsConstructor
@Service
public class PostCommandService {

    private final PostRepository postRepository;
    private final MateRepository mateRepository;
    private final MateQueryDao mateQueryDao;
    private final ApplicationEventPublisher eventPublisher;

    @CacheEvict(
        value = CacheKey.TODAY_GOALS,
        key = "{#command.userId, T(java.time.LocalDate).now().format(@dateFormatter)}"
    )
    @Transactional
    public PostCreateResult create(PostCreateCommand command) {
        Post post = createAndSavePost(command);
        // TODO: 2023/08/07 Mate, Goal, VerificationCondition 지연로딩 문제
        post.updateCheckStatus();
        publishPostUploadEvent(command.mateId());
        return new PostCreateResult(post.getId());
    }

    @Transactional
    public void like(long userId, long postId) {
        updateLikes(postId, post -> post.addLikes(userId));
    }

    @Transactional
    public void unlike(long userId, long postId) {
        updateLikes(postId, post -> post.removeLikes(userId));
    }

    public void updateLikes(long postId, Consumer<Post> action) {
        Post post = findPostWithLikes(postId);
        action.accept(post);
        post.updateCheckStatus();
    }

    private Post findPostWithLikes(long postId) {
        return postRepository.findWithLikes(postId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.POST_NOT_FOUND, postId));
    }

    private Post createAndSavePost(PostCreateCommand command) {
        Post post = createPost(findUploader(command), command.content());
        postRepository.save(post);
        saveImages(post, command.images());
        return post;
    }

    private Mate findUploader(PostCreateCommand command) {
        Mate uploader = findMate(command.mateId());
        validatePostUploadable(uploader);
        return uploader;
    }

    private Post createPost(Mate uploader, String content) {
        return Post.builder()
            .mate(uploader)
            .content(content)
            .build();
    }

    private void saveImages(Post post, List<MultipartFile> images) {
        if (CollectionUtils.isEmpty(images)) {
            return;
        }

        images.forEach(multipartFile -> {
            try {
                publishImageUploadEvent(post, multipartFile);
            } catch (IOException e) {
                throw new RuntimeIOException(e, ErrorCode.IMAGE_PROCESSING_IO);
            }
        });
    }

    private void validatePostUploadable(Mate uploader) {
        Uploadable uploadable = new Uploadable(uploader);
        Assert.isTrue(uploadable.isUploadable(), uploadable.toString());
    }

    private void publishImageUploadEvent(Post post, MultipartFile multipartFile)
        throws IOException {
        eventPublisher.publishEvent(new FileUploadedEvent(post, multipartFile.getOriginalFilename(),
            multipartFile.getInputStream()));
    }

    private void publishPostUploadEvent(long mateId) {
        PostUploadNotificationDto dto = findPostUploadNotificationDto(mateId);
        eventPublisher.publishEvent(new PushNotificationCreatedEvent(POST_UPLOAD, dto));
    }

    private PostUploadNotificationDto findPostUploadNotificationDto(long mateId) {
        return mateQueryDao.findPostUploadNotificationDto(mateId).orElseThrow(
            () -> new NotFoundException(ErrorCode.MATE_NOT_FOUND, findMate(mateId).getId()));
    }

    private Mate findMate(long mateId) {
        return mateRepository.findWithGoal(mateId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.MATE_NOT_FOUND, mateId));
    }
}
