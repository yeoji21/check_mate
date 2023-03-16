package checkmate.exception.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CommonCode {
    REQUEST_PARAMETER("C-001"),
    JSON_TYPE("C-002"),
    UPDATE_DURATION("C-003"),
    FILE_SIZE("C-004"),
    SERVICE_UNAVAILABLE("C-005"),
    UNAUTHORIZED_OPERATION("C-006"),
    DATA_INTEGTITY("C-007");

    private final String code;
}
