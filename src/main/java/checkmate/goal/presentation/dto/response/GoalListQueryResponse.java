package checkmate.goal.presentation.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Getter
@NoArgsConstructor
public class GoalListQueryResponse<T> implements Serializable {
    private List<T> goals;

    public GoalListQueryResponse(List<T> goals) {
        this.goals = goals;
    }
}
