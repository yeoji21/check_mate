package checkmate.user.domain;

import checkmate.exception.BusinessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserCode implements BusinessCode {
    NOT_FOUND("USER-001", "해당 사용자를 찾을 수 없습니다."),
    EMPTY_NICKNAME("USER-002", "닉네임을 설정해야 합니다."),
    DUPLICATED_NICKNAME("USER-003", "중복된 닉네임입니다.");

    private final String code;
    private final String message;
}
