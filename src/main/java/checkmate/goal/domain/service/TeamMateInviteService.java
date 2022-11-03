package checkmate.goal.domain.service;

import checkmate.goal.domain.Goal;
import checkmate.goal.domain.TeamMate;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class TeamMateInviteService {
    public void invite(Goal goal,
                       Optional<TeamMate> teamMate,
                       long userId) {
        teamMate.ifPresentOrElse(
                TeamMate::changeToWaitingStatus,
                () -> goal.includeNewTeamMate(userId)
        );
    }

}
