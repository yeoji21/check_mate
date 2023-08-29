package checkmate.mate.domain;

import checkmate.goal.domain.Goal;
import checkmate.user.domain.User;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UninvitedMate {

    private final Mate mate;


    public UninvitedMate of(Goal goal, Mate mate, User user) {
        if (mate == null) {
            new UninvitedMate(goal.createMate(user));
        }

        return new UninvitedMate(mate);
    }
}
