package checkmate.post.application;

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
import checkmate.post.application.dto.request.PostUploadCommand;
import checkmate.post.application.dto.response.PostUploadResult;
import checkmate.post.domain.Post;
import checkmate.post.domain.PostRepository;
import checkmate.post.domain.event.FileUploadedEvent;
import io.jsonwebtoken.lang.Assert;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static checkmate.notification.domain.NotificationType.POST_UPLOAD;

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
    public PostUploadResult upload(PostUploadCommand command) {
        Post post = createNewPost(command);
        post.updateCheckStatus();
        publishNotificationEvent(command.mateId());
        return new PostUploadResult(post.getId());
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

    private PostUploadNotificationDto findPostUploadNotificationDto(long mateId) {
        Mate uploader = findMate(mateId);
        PostUploadNotificationDto notificationDto = mateQueryDao.findPostUploadNotificationDto(uploader.getId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.MATE_NOT_FOUND, uploader.getId()));
        notificationDto.setMateUserIds(findOtherMateUserIds(uploader));
        return notificationDto;
    }

    private List<Long> findOtherMateUserIds(Mate uploader) {
        return mateQueryDao.findOngoingUserIds(uploader.getGoal().getId())
                .stream()
                .filter(userId -> !userId.equals(uploader.getUserId()))
                .collect(Collectors.toList());
    }

    private Post createNewPost(PostUploadCommand command) {
        Mate uploader = findMate(command.mateId());
        checkPostUploadable(uploader);
        return createAndSavePost(uploader, command);
    }

    private void checkPostUploadable(Mate uploader) {
        Uploadable uploadable = uploader.getUploadable();
        Assert.isTrue(uploadable.isUploadable(), uploadable.toString());
    }

    private Post createAndSavePost(Mate uploader, PostUploadCommand command) {
        Post post = createPost(uploader, command.content());
        postRepository.save(post);
        saveImages(post, command.images());
        return post;
    }

    private Post createPost(Mate uploader, String content) {
        return Post.builder()
                .mate(uploader)
                .content(content)
                .build();
    }

    private void saveImages(Post post, List<MultipartFile> images) {
        if (images.size() == 0) return;

        images.forEach(multipartFile -> {
            try {
                publishImageUploadEvent(post, multipartFile);
            } catch (IOException e) {
                throwRuntimeIOException(e);
            }
        });
    }

    private void publishImageUploadEvent(Post post, MultipartFile multipartFile) throws IOException {
        eventPublisher.publishEvent(new FileUploadedEvent(post, multipartFile.getOriginalFilename(), multipartFile.getInputStream()));
    }

    private void throwRuntimeIOException(IOException e) {
        throw new RuntimeIOException(e, ErrorCode.IMAGE_PROCESSING_IO);
    }

    private void publishNotificationEvent(long mateId) {
        PostUploadNotificationDto notificationDto = findPostUploadNotificationDto(mateId);
        eventPublisher.publishEvent(new PushNotificationCreatedEvent(POST_UPLOAD, notificationDto));
    }

    private Mate findMate(long mateId) {
        return mateRepository.findWithGoal(mateId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.MATE_NOT_FOUND, mateId));
    }
}
