package checkmate.user.application;

import checkmate.exception.BusinessException;
import checkmate.exception.code.ErrorCode;
import checkmate.user.infrastructure.UserQueryDao;
import checkmate.user.presentation.dto.UserScheduleResponse;
import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class UserQueryService {

    private final UserQueryDao userQueryDao;

    @Transactional(readOnly = true)
    public void existsNicknameCheck(String nickname) {
        if (userQueryDao.isExistsNickname(nickname)) {
            throw new BusinessException(ErrorCode.DUPLICATED_NICKNAME);
        }
    }

    public UserScheduleResponse getWeeklySchdule(long userId, LocalDate date) {
        return UserScheduleResponse.builder()
            .requestDate(date)
            .schedule(userQueryDao.findSchedule(userId, getDatesOfWeek(date)))
            .build();
    }

    private List<LocalDate> getDatesOfWeek(LocalDate date) {
        int week = date.get(ChronoField.ALIGNED_WEEK_OF_YEAR);
        return date.minusDays(8).datesUntil(date.plusDays(8))
            .filter(localDate -> localDate.get(ChronoField.ALIGNED_WEEK_OF_YEAR) == week)
            .toList();
    }
}
