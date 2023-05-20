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

import java.io.IOException;
import java.util.List;
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
        Mate uploader = findUploadMate(command.mateId());
        Post post = createNewPost(command, uploader);
        post.updateCheckStatus();
        publishNotificationEvent(uploader);
        return new PostUploadResult(post.getId());
    }

    // TODO: 2023/05/04 컨트롤러 단에서 goalID 받도록 변경 후 mate fetch join 제거
    @Transactional
    public void like(long userId, long postId) {
        Post post = postRepository.findWithLikes(postId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.POST_NOT_FOUND, postId));
        post.addLikes(userId);
        post.updateCheckStatus();
    }

    @Transactional
    public void unlike(long userId, long postId) {
        Post post = postRepository.findWithLikes(postId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.POST_NOT_FOUND, postId));
        post.removeLikes(userId);
        post.updateCheckStatus();
    }

    private void publishNotificationEvent(Mate uploader) {
        PostUploadNotificationDto notificationDto = mateQueryDao.findPostUploadNotificationDto(uploader.getId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.MATE_NOT_FOUND, uploader.getId()));
        notificationDto.setMateUserIds(findOtherMateUserIds(uploader));
        eventPublisher.publishEvent(new PushNotificationCreatedEvent(POST_UPLOAD, notificationDto));
    }

    private List<Long> findOtherMateUserIds(Mate uploader) {
        return mateQueryDao.findOngoingUserIds(uploader.getGoal().getId())
                .stream()
                .filter(userId -> !userId.equals(uploader.getUserId()))
                .collect(Collectors.toList());
    }

    private Post createNewPost(PostUploadCommand command, Mate uploader) {
        Uploadable uploadable = uploader.getUploadable();
        Assert.isTrue(uploadable.isUploadable(), uploadable.toString());

        Post post = Post.builder()
                .mate(uploader)
                .content(command.content())
                .build();
        postRepository.save(post);
        publishFileUploadedEvent(command, post);
        uploader.updateUploadedDate();
        return post;
    }

    private void publishFileUploadedEvent(PostUploadCommand command, Post post) {
        if (command.images().size() > 0) {
            command.images().forEach(multipartFile -> {
                try {
                    eventPublisher.publishEvent(new FileUploadedEvent(post, multipartFile.getOriginalFilename(), multipartFile.getInputStream()));
                } catch (IOException e) {
                    throw new RuntimeIOException(e, ErrorCode.IMAGE_PROCESSING_IO);
                }
            });
        }
    }

    private Mate findUploadMate(long mateId) {
        return mateRepository.findWithGoal(mateId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.MATE_NOT_FOUND, mateId));
    }
}
