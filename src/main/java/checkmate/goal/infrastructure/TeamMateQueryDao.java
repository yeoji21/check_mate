package checkmate.goal.infrastructure;

import checkmate.goal.domain.TeamMate;
import checkmate.goal.domain.TeamMateStatus;
import checkmate.mate.application.dto.response.MateScheduleInfo;
import checkmate.mate.application.dto.response.MateUploadInfo;
import checkmate.mate.application.dto.response.QMateScheduleInfo;
import checkmate.mate.application.dto.response.QMateUploadInfo;
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

    public List<TeamMate> findSuccessTeamMates(long userId) {
        return queryFactory.select(teamMate)
                .from(teamMate)
                .join(teamMate.goal, goal).fetchJoin()
                .where(teamMate.userId.eq(userId))
                .fetch();
    }

    public Map<Long, List<String>> findTeamMateNicknames(List<Long> goalIds) {
        return queryFactory
                .from(goal)
                .leftJoin(teamMate).on(teamMate.goal.eq(goal))
                .join(user).on(teamMate.userId.eq(user.id))
                .where(goal.id.in(goalIds))
                .transform(groupBy(goal.id).as(list(user.nickname)));
    }

    public Optional<MateScheduleInfo> getTeamMateCalendar(long teamMateId) {
        Map<Long, MateScheduleInfo> scheduleInfoMap = queryFactory
                .from(teamMate)
                .innerJoin(teamMate.goal, goal)
                .leftJoin(post).on(post.teamMate.id.eq(teamMateId))
                .where(teamMate.id.eq(teamMateId))
                .transform(
                        groupBy(teamMate.id).as(
                                new QMateScheduleInfo(goal.period.startDate, goal.period.endDate,
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

    public List<MateUploadInfo> findTeamMateInfo(long goalId) {
        return queryFactory
                .select(new QMateUploadInfo(teamMate.id, user.id, teamMate.lastUploadDate, user.nickname))
                .from(teamMate)
                .join(user).on(teamMate.userId.eq(user.id))
                .where(teamMate.goal.id.eq(goalId),
                        teamMate.status.eq(TeamMateStatus.ONGOING))
                .fetch();
    }
}
