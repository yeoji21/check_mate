package checkmate.common;

import checkmate.exception.BusinessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CommonCode implements BusinessCode {
    REQUEST_PARAMETER("C-001", "잘못된 요청 형식입니다."),
    JSON_TYPE("C-002", "JSON을 파싱할 수 없습니다."),
    UPDATE_DURATION("C-003", "아직 변경할 수 없습니다."),
    FILE_SIZE("C-004", "파일 용량이 초과되었습니다."),
    SERVICE_UNAVAILABLE("C-005", "서비스에 문제가 발생했습니다."),
    ;

    private final String code;
    private final String message;
}
