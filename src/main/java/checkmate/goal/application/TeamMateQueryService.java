package checkmate.goal.application;

import checkmate.exception.TeamMateNotFoundException;
import checkmate.goal.application.dto.response.TeamMateScheduleInfo;
import checkmate.goal.domain.TeamMate;
import checkmate.goal.domain.TeamMateRepository;
import checkmate.goal.infrastructure.TeamMateQueryDao;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class TeamMateQueryService {
    private final TeamMateQueryDao teamMateQueryDao;
    private final TeamMateRepository teamMateRepository;

    @Transactional(readOnly = true)
    public TeamMateScheduleInfo getCalenderInfo(long teamMateId) {
        return teamMateQueryDao.getTeamMateCalendar(teamMateId)
                .orElseThrow(IllegalArgumentException::new);
    }

    @Transactional(readOnly = true)
    public double getProgressPercent(long teamMateId) {
        TeamMate teamMate = findTeamMate(teamMateId);
        return teamMate.calcProgressPercent();
    }

    private TeamMate findTeamMate(long teamMateId) {
        return teamMateRepository.findTeamMate(teamMateId)
                .orElseThrow(TeamMateNotFoundException::new);
    }
}
