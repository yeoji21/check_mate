package checkmate.mate.application.dto.response;

import lombok.Builder;

@Builder
public record MateAcceptResult(
        long goalId,
        long mateId) {
}
