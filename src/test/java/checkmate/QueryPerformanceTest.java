package checkmate;

import checkmate.config.WebSecurityConfig;
import checkmate.goal.application.dto.response.QTodayGoalInfo;
import checkmate.goal.application.dto.response.TodayGoalInfo;
import checkmate.goal.domain.GoalStatus;
import checkmate.goal.domain.TeamMateStatus;
import checkmate.notification.domain.NotificationType;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.util.StopWatch;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;

import static checkmate.goal.domain.QGoal.goal;
import static checkmate.goal.domain.QTeamMate.teamMate;
import static checkmate.notification.domain.QNotification.notification;
import static checkmate.notification.domain.QNotificationReceiver.notificationReceiver;


@Disabled
@Import(WebSecurityConfig.class)
@SpringBootTest
@Transactional
public class QueryPerformanceTest {
    @Autowired private JPAQueryFactory queryFactory;
    private StopWatch stopWatch;

    @BeforeEach
    void setUp() {
        stopWatch = new StopWatch();
    }

    @Test
    void findTodayGoalInfoDtoList() throws Exception{
//        stopWatch.start();
//
//        List<TodayGoalInfo> result = queryFactory.select(new QTodayGoalInfo(goal.id, goal.category, goal.title, goal.checkDays,
//                        new CaseBuilder().when(teamMate.lastUploadDay.eq(LocalDate.now())).then(true).otherwise(false)))
//                .from(teamMate)
//                .join(teamMate.goal, goal).on(goal.checkDays.checkDays.divide(WeekDayConverter.localDateToValue(LocalDate.now()))
//                        .floor().mod(10).eq(1), goal.status.eq(GoalStatus.ONGOING))
//                .where(teamMate.userId.eq(11L),
//                        teamMate.status.eq(TeamMateStatus.ONGOING))
//                .fetch();
//        stopWatch.stop();
//        result.forEach(r -> System.out.println(r.getTitle()));
//        System.out.println(stopWatch.getTotalTimeMillis());


        stopWatch.start();

//        List<Integer> right = WeekDayConverter.allCasesWith(LocalDate.now().getDayOfWeek().toString());
        List<Integer> right = List.of(1000, 1001, 1010, 1011, 1100, 1101, 1110, 1111, 11000, 11001, 11010, 11011, 11100, 11101, 11110, 11111, 101000, 101001, 101010, 101011, 101100, 101101, 101110, 101111, 111000, 111001, 111010, 111011, 111100, 111101, 111110, 111111, 1001000, 1001001, 1001010, 1001011, 1001100, 1001101, 1001110, 1001111, 1011000, 1011001, 1011010, 1011011, 1011100, 1011101, 1011110, 1011111, 1101000, 1101001, 1101010, 1101011, 1101100, 1101101, 1101110, 1101111, 1111000, 1111001, 1111010, 1111011, 1111100, 1111101, 1111110, 1111111);
        right.stream().forEach(n -> System.out.print(n + ", "));

        List<TodayGoalInfo> slowResult = queryFactory.select(new QTodayGoalInfo(goal.id, goal.category, goal.title, goal.checkDays,
                        new CaseBuilder().when(teamMate.lastUploadDay.eq(LocalDate.now())).then(true).otherwise(false)))
                .from(teamMate)
                .join(teamMate.goal, goal).on(goal.checkDays.checkDays.in(right), goal.status.eq(GoalStatus.ONGOING))
                .where(teamMate.userId.eq(11L),
                        teamMate.status.eq(TeamMateStatus.ONGOING))
                .fetch();
        stopWatch.stop();

        //then
//        assertThat(slowResult).isEqualTo(result);
        slowResult.forEach(r -> System.out.println(r.getTitle()));
        System.out.println(stopWatch.getTotalTimeMillis());
    }

    @Test
    void findGoalCompleteNotification() throws Exception{
        stopWatch.start();
        queryFactory
                .selectFrom(notification)
                .join(notification.receivers.receivers, notificationReceiver).on(notificationReceiver.checked.eq(false))
                .where(notificationReceiver.userId.eq(11L),
                        notification.type.eq(NotificationType.COMPLETE_GOAL))
                .fetch();
        stopWatch.stop();
        System.out.println("# First : " + stopWatch.getTotalTimeMillis());

        stopWatch.start();
        queryFactory
                .selectFrom(notification).distinct()
                .join(notification.receivers.receivers, notificationReceiver).on(notificationReceiver.checked.eq(false))
                .where(notificationReceiver.userId.eq(11L),
                        notification.type.eq(NotificationType.COMPLETE_GOAL))
                .fetch();
        stopWatch.stop();
        System.out.println("# Second : " + stopWatch.getTotalTimeMillis());
    }
}
