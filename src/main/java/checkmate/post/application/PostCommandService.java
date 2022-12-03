package checkmate.post.application;

import checkmate.exception.ImageProcessIOException;
import checkmate.exception.TeamMateNotFoundException;
import checkmate.exception.format.ErrorCode;
import checkmate.exception.format.NotFoundException;
import checkmate.goal.domain.Goal;
import checkmate.goal.domain.GoalRepository;
import checkmate.goal.domain.TeamMate;
import checkmate.goal.domain.TeamMateRepository;
import checkmate.notification.domain.event.PushNotificationCreatedEvent;
import checkmate.notification.domain.factory.dto.PostUploadNotificationDto;
import checkmate.post.application.dto.request.PostUploadCommand;
import checkmate.post.domain.Likes;
import checkmate.post.domain.Post;
import checkmate.post.domain.PostRepository;
import checkmate.post.domain.event.FileUploadedEvent;
import checkmate.user.domain.User;
import checkmate.user.domain.UserRepository;
import io.jsonwebtoken.lang.Assert;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final TeamMateRepository teamMateRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public Long upload(PostUploadCommand command) {
        TeamMate uploader = findTeamMate(command.getTeamMateId());
        Post post = save(command, uploader);
        verifyGoalConditions(uploader.getGoal().getId(), post);
        publishNotificationEvent(uploader);
        return post.getId();
    }

    @Transactional
    public void like(long userId, long postId) {
        Post post = postRepository.findById(postId).orElseThrow(IllegalArgumentException::new);
        long goalId = validateUserInGoal(userId, post);
        post.addLikes(new Likes(userId));
        verifyGoalConditions(goalId, post);
    }

    @Transactional
    public void unlike(long userId, long postId) {
        Post post = postRepository.findById(postId).orElseThrow(IllegalArgumentException::new);
        long goalId = validateUserInGoal(userId, post);
        post.removeLikes(userId);
        verifyGoalConditions(goalId, post);
    }

    private void verifyGoalConditions(Long goalId, Post post) {
        Goal goal = goalRepository.findWithConditions(goalId).orElseThrow(IllegalArgumentException::new);
        goal.checkConditions(post);
    }

    private long validateUserInGoal(long userId, Post post) {
        return teamMateRepository.findTeamMateWithGoal(post.getTeamMate().getGoal().getId(), userId)
                .orElseThrow(TeamMateNotFoundException::new)
                .getGoal()
                .getId();
    }

    private void publishNotificationEvent(TeamMate uploader) {
        User user = findUser(uploader);
        PostUploadNotificationDto notificationDto = PostUploadNotificationDto.builder()
                .uploaderUserId(user.getId())
                .uploaderNickname(user.getNickname())
                .goalId(uploader.getGoal().getId())
                .goalTitle(uploader.getGoal().getTitle())
                .teamMateUserIds(getTeamMateUserIds(uploader))
                .build();
        eventPublisher.publishEvent(new PushNotificationCreatedEvent(POST_UPLOAD, notificationDto));
    }

    private List<Long> getTeamMateUserIds(TeamMate uploader) {
        return teamMateRepository.findTeamMateUserIds(uploader.getGoal().getId())
                .stream()
                .filter(userId -> !userId.equals(uploader.getUserId()))
                .collect(Collectors.toList());
    }

    private Post save(PostUploadCommand command, TeamMate uploader) {
        Assert.isTrue(uploader.getUploadable().isUploadable(), "uploadable");

        Post post = Post.builder().teamMate(uploader).content(command.getText()).build();
        postRepository.save(post);
        uploader.updateUploadedDate();
        publishFileUploadedEvent(command, post);
        return post;
    }

    private void publishFileUploadedEvent(PostUploadCommand command, Post post) {
        if(command.getImages().size() > 0) {
            command.getImages().forEach(multipartFile -> {
                try {
                    eventPublisher.publishEvent(new FileUploadedEvent(post, multipartFile.getOriginalFilename(), multipartFile.getInputStream()));
                } catch (IOException e) {
                    throw new ImageProcessIOException(e);
                }
            });
        }
    }

    private User findUser(TeamMate uploader) {
        return userRepository.findById(uploader.getUserId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND, uploader.getUserId()));
    }

    private TeamMate findTeamMate(long teamMateId) {
        return teamMateRepository.findTeamMateWithGoal(teamMateId)
                .orElseThrow(IllegalArgumentException::new);
    }
}
