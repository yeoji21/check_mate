package checkmate.post.application;

import checkmate.common.cache.CacheKey;
import checkmate.exception.NotFoundException;
import checkmate.exception.RuntimeIOException;
import checkmate.exception.code.ErrorCode;
import checkmate.goal.domain.Goal;
import checkmate.goal.domain.GoalRepository;
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
import checkmate.user.domain.User;
import checkmate.user.domain.UserRepository;
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
    private final GoalRepository goalRepository;
    private final UserRepository userRepository;
    private final MateRepository mateRepository;
    private final MateQueryDao mateQueryDao;
    private final ApplicationEventPublisher eventPublisher;

    @CacheEvict(
            value = CacheKey.TODAY_GOALS,
            key = "{#command.userId, T(java.time.LocalDate).now().format(@dateFormatter)}"
    )
    @Transactional
    public PostUploadResult upload(PostUploadCommand command) {
        Mate uploader = findMate(command.mateId());
        Post post = create(command, uploader);
        verifyGoalConditions(uploader.getGoal().getId(), post);
        publishNotificationEvent(uploader);
        return new PostUploadResult(post.getId());
    }

    // TODO: 2023/04/11 메소드 내 중복되는 쿼리 제거
    @Transactional
    public void like(long userId, long postId) {
        Post post = postRepository.findWithLikes(postId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.POST_NOT_FOUND, postId));
        long goalId = validateUserInGoal(userId, post);
        post.addLikes(userId);
        verifyGoalConditions(goalId, post);
    }

    @Transactional
    public void unlike(long userId, long postId) {
        Post post = postRepository.findWithLikes(postId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.POST_NOT_FOUND, postId));
        long goalId = validateUserInGoal(userId, post);
        post.removeLikes(userId);
        verifyGoalConditions(goalId, post);
    }

    // TODO: 2023/02/23 공통 관심사
    private void verifyGoalConditions(Long goalId, Post post) {
        Goal goal = goalRepository.findWithConditions(goalId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.GOAL_NOT_FOUND, goalId));
        goal.checkConditions(post);
    }

    private long validateUserInGoal(long userId, Post post) {
        return mateRepository.findWithGoal(post.getMate().getGoal().getId(), userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.MATE_NOT_FOUND))
                .getGoal()
                .getId();
    }

    private void publishNotificationEvent(Mate uploader) {
        User user = findUser(uploader);
        PostUploadNotificationDto dto = PostUploadNotificationDto.builder()
                .uploaderUserId(user.getId())
                .uploaderNickname(user.getNickname())
                .goalId(uploader.getGoal().getId())
                .goalTitle(uploader.getGoal().getTitle())
                .mateUserIds(getMateUserIds(uploader))
                .build();
        eventPublisher.publishEvent(new PushNotificationCreatedEvent(POST_UPLOAD, dto));
    }

    private List<Long> getMateUserIds(Mate uploader) {
        return mateQueryDao.findOngoingUserIds(uploader.getGoal().getId())
                .stream()
                .filter(userId -> !userId.equals(uploader.getUserId()))
                .collect(Collectors.toList());
    }

    private Post create(PostUploadCommand command, Mate uploader) {
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

    private User findUser(Mate uploader) {
        return userRepository.findById(uploader.getUserId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND, uploader.getUserId()));
    }

    private Mate findMate(long mateId) {
        return mateRepository.findWithGoal(mateId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.MATE_NOT_FOUND, mateId));
    }
}
