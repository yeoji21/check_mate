package checkmate.goal.domain;

import javax.persistence.CascadeType;
import javax.persistence.Embeddable;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

@Embeddable
public class Team {
    @OneToMany(mappedBy = "goal", cascade = CascadeType.PERSIST)
    private final List<TeamMate> teamMates;

    public Team() {
        this.teamMates = new ArrayList<>();
    }
}
