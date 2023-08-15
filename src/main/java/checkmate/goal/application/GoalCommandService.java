package checkmate.goal.application;

import static checkmate.exception.code.ErrorCode.USER_NOT_FOUND;

import checkmate.common.cache.CacheKeyUtil;
import checkmate.exception.NotFoundException;
import checkmate.exception.code.ErrorCode;
import checkmate.goal.application.dto.GoalCommandMapper;
import checkmate.goal.application.dto.request.GoalCreateCommand;
import checkmate.goal.application.dto.request.GoalModifyCommand;
import checkmate.goal.application.dto.request.LikeCountCreateCommand;
import checkmate.goal.domain.Goal;
import checkmate.goal.domain.GoalRepository;
import checkmate.goal.domain.LikeCountCondition;
import checkmate.mate.domain.Mate;
import checkmate.mate.domain.MateRepository;
import checkmate.user.domain.User;
import checkmate.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class GoalCommandService {

    private final GoalRepository goalRepository;
    private final UserRepository userRepository;
    private final MateRepository mateRepository;
    private final GoalCommandMapper mapper;

    @Caching(evict = {
        @CacheEvict(
            value = CacheKeyUtil.ONGOING_GOALS,
            key = "{#command.userId, T(java.time.LocalDate).now().format(@dateFormatter)}"),
        @CacheEvict(
            value = CacheKeyUtil.TODAY_GOALS,
            key = "{#command.userId, T(java.time.LocalDate).now().format(@dateFormatter)}")
    })
    @Transactional
    public long create(GoalCreateCommand command) {
        Goal goal = createAndSaveGoal(command);
        initateToGoal(createAndSaveMate(goal, command.userId()));
        return goal.getId();
    }

    @CacheEvict(value = CacheKeyUtil.GOAL_PERIOD, key = "{#command.goalId}")
    @Transactional
    public void modify(GoalModifyCommand command) {
        Goal goal = findGoalForUpdate(command.goalId());
        goal.modify(mapper.toModifyEvent(command));
    }

    @Transactional
    public void addLikeCountCondition(LikeCountCreateCommand command) {
        goalRepository.saveCondition(createLikeCountCondition(command));
    }

    private Goal createAndSaveGoal(GoalCreateCommand command) {
        return goalRepository.save(createGoal(command));
    }

    private Goal createGoal(GoalCreateCommand command) {
        return mapper.toEntity(command);
    }

    private Mate createAndSaveMate(Goal goal, long userId) {
        Mate mate = createMate(goal, userId);
        mateRepository.save(mate);
        return mate;
    }

    private void initateToGoal(Mate mate) {
        mateRepository.findUninitiateMate(mate.getId())
            .orElseThrow(() -> new NotFoundException(ErrorCode.MATE_NOT_FOUND, mate.getId()))
            .initiate();
    }

    private Mate createMate(Goal goal, long userId) {
        Mate mate = goal.createMate(findUser(userId));
        mate.receiveInvite();
        return mate;
    }

    private User findUser(long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND, userId));
    }

    private Goal findGoalForUpdate(long goalId) {
        return goalRepository.findForUpdate(goalId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.GOAL_NOT_FOUND, goalId));
    }

    private LikeCountCondition createLikeCountCondition(LikeCountCreateCommand command) {
        return new LikeCountCondition(findGoal(command.goalId()), command.likeCount());
    }

    private Goal findGoal(long goalId) {
        return goalRepository.find(goalId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.GOAL_NOT_FOUND, goalId));
    }
}
