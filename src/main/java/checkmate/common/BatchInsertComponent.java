package checkmate.common;

import checkmate.goal.domain.Goal;
import checkmate.goal.domain.Goal.GoalCategory;
import checkmate.goal.domain.GoalCheckDays;
import checkmate.goal.domain.GoalPeriod;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
//@Component
public class BatchInsertComponent {

    private final JdbcTemplate jdbcTemplate;
    private int batchSize = 10000;

    @Transactional
    @PostConstruct
    public void batchInsert() {
        List<Goal> goals = new ArrayList<>();
        for (int i = 0; i < 3_000_000; i++) {
            Goal goal = Goal.builder()
                .category(GoalCategory.ETC)
                .title(UUID.randomUUID().toString().substring(0, 10))
                .period(new GoalPeriod(LocalDate.now().minusDays((int) (Math.random() * 10)),
                    LocalDate.now().plusDays((int) (Math.random() * 30))))
                .checkDays(GoalCheckDays.ofValue((int) (Math.random() * 126) + 1))
                .build();
            goals.add(goal);
            if (goals.size() == batchSize) {
                insert(goals);
                goals.clear();
            }
        }

    }

    private void insert(List<Goal> goals) {
        jdbcTemplate.batchUpdate(
            "INSERT INTO goal (category, title, start_date, end_date, check_days, status) " +
                "VALUES (?, ?, ?, ?, ?, ?)",
            goals,
            batchSize,
            (ps, argument) -> {
                ps.setString(1, argument.getCategory().name());
                ps.setString(2, argument.getTitle());
                ps.setString(3, argument.getStartDate().toString());
                ps.setString(4, argument.getEndDate().toString());
                ps.setInt(5, argument.getCheckDays().toInt());
                ps.setString(6, argument.getStatus().name());
            });
    }
}
