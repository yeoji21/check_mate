package checkmate.goal.application.dto.response;

import lombok.Builder;

@Builder
public record TeamMateAcceptResult(
    long goalId,
    long teamMateId) {
}
