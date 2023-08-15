package checkmate.post.application;

import checkmate.goal.domain.GoalRepository;
import checkmate.goal.domain.VerificationCondition;
import checkmate.post.domain.Post;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class PostCheckService {

    private final GoalRepository goalRepository;

    public void updateCheckStatus(Post post) {
        if (isSatisfyAllConditions(post)) {
            post.check();
        } else {
            post.uncheck();
        }
    }

    private boolean isSatisfyAllConditions(Post post) {
        List<VerificationCondition> conditions = goalRepository.findConditions(getGoalId(post));
        return conditions.isEmpty() || conditions.stream()
            .allMatch(condition -> condition.satisfy(post));
    }

    private Long getGoalId(Post post) {
        return post.getMate().getGoal().getId();
    }
}
