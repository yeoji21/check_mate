package checkmate.post.application;

import checkmate.exception.NotFoundException;
import checkmate.exception.code.ErrorCode;
import checkmate.goal.domain.Goal;
import checkmate.goal.domain.GoalRepository;
import checkmate.post.application.dto.response.PostInfo;
import checkmate.post.application.dto.response.PostInfoResult;
import checkmate.post.infrastructure.PostQueryDao;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class PostQueryService {

    private final GoalRepository goalRepository;
    private final PostQueryDao postQueryDao;

    @Transactional(readOnly = true)
    public PostInfoResult findPostByGoalIdAndDate(long goalId, String date) {
        Goal goal = goalRepository.find(goalId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.GOAL_NOT_FOUND, goalId));
        List<PostInfo> postInfos = postQueryDao.findTimelinePosts(goalId,
            LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyyMMdd")));
        return new PostInfoResult(goal.getTitle(), postInfos);
    }
}
