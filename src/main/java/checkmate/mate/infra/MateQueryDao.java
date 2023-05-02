package checkmate.mate.infra;

import checkmate.mate.application.dto.response.MateScheduleInfo;
import checkmate.mate.application.dto.response.MateUploadInfo;
import checkmate.mate.application.dto.response.QMateScheduleInfo;
import checkmate.mate.application.dto.response.QMateUploadInfo;
import checkmate.mate.domain.MateStatus;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static checkmate.goal.domain.QGoal.goal;
import static checkmate.mate.domain.QMate.mate;
import static checkmate.post.domain.QPost.post;
import static checkmate.user.domain.QUser.user;
import static com.querydsl.core.group.GroupBy.groupBy;
import static com.querydsl.core.group.GroupBy.list;

@RequiredArgsConstructor
@Repository
public class MateQueryDao {
    private final JPAQueryFactory queryFactory;

    public boolean existOngoingMate(long goalId, long userId) {
        Long mateId = queryFactory.select(mate.id)
                .from(mate)
                .where(mate.userId.eq(userId),
                        mate.goal.id.eq(goalId),
                        mate.status.eq(MateStatus.ONGOING))
                .fetchOne();
        return mateId != null;
    }

    public List<Long> findOngoingUserIds(long goalId) {
        return queryFactory
                .select(mate.userId)
                .from(mate)
                .where(mate.goal.id.eq(goalId),
                        mate.status.eq(MateStatus.ONGOING))
                .fetch();
    }

    /**
     * goalId에 해당하는 mate들의 닉네임을 조회
     *
     * @param goalIds
     * @return key: goalId, value: 닉네임 리스트
     */
    public Map<Long, List<String>> findMateNicknames(List<Long> goalIds) {
        return queryFactory
                .from(goal)
                .leftJoin(mate).on(mate.goal.eq(goal))
                .join(user).on(mate.userId.eq(user.id))
                .where(goal.id.in(goalIds))
                .transform(groupBy(goal.id).as(list(user.nickname)));
    }

    public Optional<MateScheduleInfo> findScheduleInfo(long mateId) {
        Map<Long, MateScheduleInfo> scheduleInfoMap = queryFactory
                .from(mate)
                .innerJoin(mate.goal, goal)
                .leftJoin(post).on(post.mate.id.eq(mateId))
                .where(mate.id.eq(mateId))
                .transform(
                        groupBy(mate.id).as(
                                new QMateScheduleInfo(goal.period.startDate, goal.period.endDate,
                                        goal.checkDays.checkDays, list(post.uploadedDate))
                        )
                );
        return Optional.ofNullable(scheduleInfoMap.get(mateId));
    }

    public List<LocalDate> findUploadedDates(long mateId) {
        return queryFactory
                .select(post.uploadedDate)
                .from(post)
                .where(mate.id.eq(mateId))
                .fetch();
    }

    public List<MateUploadInfo> findUploadInfo(long goalId) {
        return queryFactory
                .select(new QMateUploadInfo(mate.id, user.id, mate.lastUploadDate, user.nickname))
                .from(mate)
                .join(user).on(mate.userId.eq(user.id))
                .where(mate.goal.id.eq(goalId),
                        mate.status.eq(MateStatus.ONGOING))
                .fetch();
    }
}
