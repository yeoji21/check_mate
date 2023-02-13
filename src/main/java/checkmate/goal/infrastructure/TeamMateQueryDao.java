package checkmate.goal.infrastructure;

import checkmate.goal.application.dto.response.QTeamMateScheduleInfo;
import checkmate.goal.application.dto.response.QTeamMateUploadInfo;
import checkmate.goal.application.dto.response.TeamMateScheduleInfo;
import checkmate.goal.application.dto.response.TeamMateUploadInfo;
import checkmate.goal.domain.TeamMateStatus;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static checkmate.goal.domain.QGoal.goal;
import static checkmate.goal.domain.QTeamMate.teamMate;
import static checkmate.post.domain.QPost.post;
import static checkmate.user.domain.QUser.user;
import static com.querydsl.core.group.GroupBy.groupBy;
import static com.querydsl.core.group.GroupBy.list;

@RequiredArgsConstructor
@Repository
public class TeamMateQueryDao {
    private final JPAQueryFactory queryFactory;

    public Optional<TeamMateScheduleInfo> getTeamMateCalendar(long teamMateId) {
        Map<Long, TeamMateScheduleInfo> scheduleInfoMap = queryFactory
                .from(teamMate)
                .innerJoin(teamMate.goal, goal)
                .leftJoin(post).on(post.teamMate.id.eq(teamMateId))
                .where(teamMate.id.eq(teamMateId))
                .transform(
                        groupBy(teamMate.id).as(
                                new QTeamMateScheduleInfo(goal.period.startDate, goal.period.endDate,
                                        goal.checkDays.checkDays, list(post.uploadedDate))
                        )
                );
        return Optional.ofNullable(scheduleInfoMap.get(teamMateId));
    }

    public List<LocalDate> findUploadedDates(long teamMateId) {
        return queryFactory
                .select(post.uploadedDate)
                .from(teamMate)
                .leftJoin(post).on(post.teamMate.id.eq(teamMateId))
                .where(teamMate.id.eq(teamMateId))
                .fetch();
    }

    public List<TeamMateUploadInfo> findTeamMateInfo(long goalId) {
        return queryFactory
                .select(new QTeamMateUploadInfo(teamMate.id, user.id, teamMate.lastUploadDate, user.nickname))
                .from(teamMate)
                .join(user).on(teamMate.userId.eq(user.id))
                .where(teamMate.goal.id.eq(goalId),
                        teamMate.status.eq(TeamMateStatus.ONGOING))
                .fetch();
    }
}
