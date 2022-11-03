package checkmate.goal.infrastructure;

import checkmate.goal.application.dto.response.QTeamMateCalendarInfo;
import checkmate.goal.application.dto.response.TeamMateCalendarInfo;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static checkmate.goal.domain.QGoal.goal;
import static checkmate.goal.domain.QTeamMate.teamMate;
import static checkmate.post.domain.QPost.post;
import static com.querydsl.core.group.GroupBy.groupBy;
import static com.querydsl.core.group.GroupBy.list;

@RequiredArgsConstructor
@Repository
public class TeamMateQueryDao {
    private final JPAQueryFactory queryFactory;

    public Optional<TeamMateCalendarInfo> getTeamMateCalendar(long teamMateId) {
        return Optional.ofNullable(
                queryFactory
                        .from(teamMate)
                        .innerJoin(teamMate.goal, goal)
                        .leftJoin(post).on(post.teamMate.id.eq(teamMateId))
                        .where(teamMate.id.eq(teamMateId))
                        .transform(
                                groupBy(teamMate.id).as(
                                        new QTeamMateCalendarInfo(goal, list(post.uploadedDate))
                                )
                        ).get(teamMateId));
    }
}
