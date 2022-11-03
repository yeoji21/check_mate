package checkmate.goal.domain;

import checkmate.post.domain.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

// TODO: 2022/09/13 TEST
@RequiredArgsConstructor
@Component
public class PostVerificationService {
    public void verify(Post post, List<VerificationCondition> conditions) {
        boolean verified = conditions.stream().allMatch(condition -> condition.satisfy(post));
        if(post.isChecked() && !verified) post.uncheck();
        else if(verified) post.check();
    }
}
