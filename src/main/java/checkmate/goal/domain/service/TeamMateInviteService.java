package checkmate.goal.domain.service;

import checkmate.goal.domain.Goal;
import checkmate.goal.domain.TeamMate;
import checkmate.goal.domain.TeamMateRepository;
import checkmate.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class TeamMateInviteService {
    private final TeamMateRepository teamMateRepository;

    public void invite(Goal goal,
                       Optional<TeamMate> teamMate,
                       User user) {
        teamMate.ifPresentOrElse(
                TeamMate::changeToWaitingStatus,
                () -> teamMateRepository.save(goal.join(user))
        );
    }

}
