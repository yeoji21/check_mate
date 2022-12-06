package checkmate.post.application;

import checkmate.exception.code.ErrorCode;
import checkmate.exception.NotFoundException;
import checkmate.goal.domain.Goal;
import checkmate.goal.domain.GoalRepository;
import checkmate.post.application.dto.response.PostInfo;
import checkmate.post.application.dto.response.PostInfoListResult;
import checkmate.post.infrastructure.PostQueryDao;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RequiredArgsConstructor
@Transactional
@Service
public class PostQueryService {
    private final GoalRepository goalRepository;
    private final PostQueryDao postQueryDao;

    public PostInfoListResult findPostByGoalIdAndDate(long goalId, String date) {
        Goal goal = goalRepository.findById(goalId).orElseThrow(() -> new NotFoundException(ErrorCode.GOAL_NOT_FOUND, goalId));
        List<PostInfo> postInfos = postQueryDao.findTimelinePosts(goalId, LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyyMMdd")));
        return new PostInfoListResult(goal.getTitle(), postInfos);
    }
}
