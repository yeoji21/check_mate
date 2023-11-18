package checkmate.user.infrastructure;

import static checkmate.goal.domain.QGoal.goal;
import static checkmate.mate.domain.QMate.mate;
import static checkmate.post.domain.QPost.post;
import static checkmate.user.domain.QUser.user;
import static com.querydsl.core.types.ExpressionUtils.count;

import checkmate.goal.domain.Goal;
import checkmate.goal.domain.Goal.GoalStatus;
import checkmate.mate.domain.Mate.MateStatus;
import checkmate.post.domain.Post;
import checkmate.user.application.dto.CheckedGoalInfo;
import checkmate.user.application.dto.DailySchedule;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class UserQueryDao {

    private final JPAQueryFactory queryFactory;

    public int countOngoingGoals(long userId) {
        return queryFactory.select(count(goal.id))
            .from(mate)
            .join(mate.goal, goal)
            .where(mate.userId.eq(userId),
                mate.status.eq(MateStatus.ONGOING),
                goal.status.eq(GoalStatus.ONGOING))
            .fetchOne()
            .intValue();
    }

    public boolean isExistsNickname(String nickname) {
        return queryFactory.select(user.id)
            .from(user)
            .where(user.nickname.eq(nickname))
            .fetchOne() != null;
    }

    public List<DailySchedule> findSchedule(long userId, List<LocalDate> dates) {
        List<Goal> goals = queryFactory.select(mate.goal)
            .from(mate)
            .where(mate.userId.eq(userId))
            .fetch();

        List<Post> posts = queryFactory.select(post)
            .from(post)
            .where(post.mate.userId.eq(userId),
                post.createdDate.in(dates))
            .fetch();

        return dates.stream()
            .map(date -> DailySchedule.builder()
                .date(date)
                .goals(getGoalsBelongToDate(goals, date)
                    .stream()
                    .map(goal -> mapToCheckedGoalInfo(posts, date, goal))
                    .toList())
                .build()
            )
            .toList();
    }

    private CheckedGoalInfo mapToCheckedGoalInfo(List<Post> posts, LocalDate date, Goal goal) {
        return CheckedGoalInfo.builder()
            .goalId(goal.getId())
            .checked(hasCheckedPost(goal.getId(), date, posts))
            .build();
    }

    private boolean hasCheckedPost(Long goalId, LocalDate date, List<Post> posts) {
        return posts.stream()
            .filter(post -> post.getMate().getGoal().getId().equals(goalId))
            .filter(post -> post.getCreatedDate().equals(date))
            .anyMatch(post -> post.isChecked());
    }

    private List<Goal> getGoalsBelongToDate(List<Goal> goals, LocalDate date) {
        return goals.stream()
            .filter(goal -> goal.getPeriod().isBelongToPeriod(date))
            .toList();
    }
}
