package checkmate.exception.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PostCode {
    NOT_FOUND("POST-001"),
    IMAGE_LIMIT("POST-002"),
    IMAGE_NOT_FOUND("POST-003"),
    IMAGE_IO("POST-004"),
    LIKES_CONDITION("POST-005"),
    ;

    private final String code;
}
