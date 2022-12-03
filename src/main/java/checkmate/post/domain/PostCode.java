package checkmate.post.domain;

import checkmate.exception.format.BusinessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PostCode implements BusinessCode {
    NOT_FOUND("POST-001", "해당 게시글을 찾을 수 없습니다."),
    IMAGE_LIMIT("POST-002", "최대 이미지 수를 초과했습니다."),
    IMAGE_NOT_FOUND("POST-003", "해당 이미지를 찾을 수 없습니다."),

    ;

    private final String code;
    private final String message;
}
