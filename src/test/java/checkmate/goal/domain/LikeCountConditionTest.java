package checkmate.goal.domain;

import checkmate.TestEntityFactory;
import checkmate.post.domain.Post;

class LikeCountConditionTest {

    private Post createPost(Goal goal) {
        return TestEntityFactory.post(goal.createMate(TestEntityFactory.user(1L, "user")));
    }

    private Goal createGoal() {
        return TestEntityFactory.goal(1L, "goal");
    }
}