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
        boolean satisfy = isSatisfyAllConditions(post);
        // TODO: 2023/08/07 post check 상태는 check 메소드 내부로 캡슐화
        if (!post.isChecked() && satisfy) {
            post.check();
        } else if (post.isChecked() && !satisfy) {
            post.uncheck();
        }
    }

    private boolean isSatisfyAllConditions(Post post) {
        List<VerificationCondition> conditions = goalRepository.findConditions(
            post.getMate().getGoal().getId());
        return conditions.isEmpty() || conditions.stream()
            .allMatch(condition -> condition.satisfy(post));
    }
}
