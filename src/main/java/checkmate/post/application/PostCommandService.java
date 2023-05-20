package checkmate.post.application;

import checkmate.common.cache.CacheKey;
import checkmate.exception.NotFoundException;
import checkmate.exception.RuntimeIOException;
import checkmate.exception.code.ErrorCode;
import checkmate.mate.domain.Mate;
import checkmate.mate.domain.MateRepository;
import checkmate.mate.infra.MateQueryDao;
import checkmate.notification.domain.event.PushNotificationCreatedEvent;
import checkmate.notification.domain.factory.dto.PostUploadNotificationDto;
import checkmate.post.application.dto.request.PostUploadCommand;
import checkmate.post.application.dto.response.PostUploadResult;
import checkmate.post.domain.Post;
import checkmate.post.domain.PostRepository;
import checkmate.post.domain.event.FileUploadedEvent;
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
        Post post = createAndSavePost(command);
        post.updateCheckStatus();
        publishPostUploadEvent(command.mateId());
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

    private Post createAndSavePost(PostUploadCommand command) {
        Post post = createPost(findUploader(command), command.content());
        postRepository.save(post);
        saveImages(post, command.images());
        return post;
    }

    private Mate findUploader(PostUploadCommand command) {
        Mate uploader = findMate(command.mateId());
        uploader.validatePostUploadable();
        return uploader;
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
                throw new RuntimeIOException(e, ErrorCode.IMAGE_PROCESSING_IO);
            }
        });
    }

    private void publishImageUploadEvent(Post post, MultipartFile multipartFile) throws IOException {
        eventPublisher.publishEvent(new FileUploadedEvent(post, multipartFile.getOriginalFilename(), multipartFile.getInputStream()));
    }

    private void publishPostUploadEvent(long mateId) {
        PostUploadNotificationDto notificationDto = findPostUploadNotificationDto(mateId);
        eventPublisher.publishEvent(new PushNotificationCreatedEvent(POST_UPLOAD, notificationDto));
    }

    private PostUploadNotificationDto findPostUploadNotificationDto(long mateId) {
        PostUploadNotificationDto notificationDto = mateQueryDao.findPostUploadNotificationDto(findMate(mateId).getId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.MATE_NOT_FOUND, findMate(mateId).getId()));
        notificationDto.setMateUserIds(findOtherMateUserIds(findMate(mateId)));
        return notificationDto;
    }

    private List<Long> findOtherMateUserIds(Mate uploader) {
        return mateQueryDao.findOngoingUserIds(uploader.getGoal().getId())
                .stream()
                .filter(userId -> !userId.equals(uploader.getUserId()))
                .collect(Collectors.toList());
    }

    private Mate findMate(long mateId) {
        return mateRepository.findWithGoal(mateId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.MATE_NOT_FOUND, mateId));
    }
}
