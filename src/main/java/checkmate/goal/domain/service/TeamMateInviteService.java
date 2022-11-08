package checkmate.goal.domain.service;

import checkmate.goal.domain.Goal;
import checkmate.goal.domain.TeamMate;
import checkmate.user.domain.User;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class TeamMateInviteService {
    public void invite(Goal goal,
                       Optional<TeamMate> teamMate,
                       User user) {
        teamMate.ifPresentOrElse(
                TeamMate::changeToWaitingStatus,
                () -> goal.join(user)
        );
    }

}
