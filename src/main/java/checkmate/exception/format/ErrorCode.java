package checkmate.exception.format;

import checkmate.user.domain.UserCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import static org.springframework.http.HttpStatus.*;


@Getter
@AllArgsConstructor
public enum ErrorCode {
    // 400 BAD_REQUEST : 잘못된 요청
    INVALID_REQUEST_PARAMETER("", BAD_REQUEST, "잘못된 요청 형식입니다."),
    INVALID_JSON_TYPE("", BAD_REQUEST, "JSON을 파싱할 수 없습니다."),
    INVALID_WEEK_DAYS("", BAD_REQUEST, "올바르지 않은 요일입니다."),
    INVALID_DATE_RANGE("", BAD_REQUEST, "올바르지 않은 기간 설정입니다."),
    ALREADY_IN_GOAL("", BAD_REQUEST, "이미 함께 목표를 진행 중인 동료에요!"),
    WAITING_INVITE("", BAD_REQUEST, "이미 초대 요청을 보냈어요!"),
    INVALID_MINIMUM_LIKE("", BAD_REQUEST, "확인 인증 목표는 최소 좋아요 수를 1 이상으로 설정해야 해요!"),

    // 404 NOT_FOUND : Resource를 찾을 수 없음

    USER_NOT_FOUND(UserCode.NOT_FOUND.getCode(), NOT_FOUND, UserCode.NOT_FOUND.getMessage()),
    GOAL_NOT_FOUND("GOAL-001", NOT_FOUND, "해당 목표를 찾을 수 없습니다."),
    IMAGE_NOT_FOUND("IMAGE-001", NOT_FOUND, "해당 이미지를 찾을 수 없습니다."),
    TEAM_MATE_NOT_FOUND("TM-001", NOT_FOUND, "일치하는 팀원을 찾을 수 없습니다."),
    NOTIFICATION_NOT_FOUND("NOTI-001", NOT_FOUND, "해당 알림을 찾을 수 없습니다."),
    REFRESH_TOKEN_NOT_FOUND("TOKEN-001", NOT_FOUND, "해당 토큰을 찾을 수 없습니다."),

    // 406 NOT_ACCEPTABLE : 거절
    EXCEED_LIMIT("", NOT_ACCEPTABLE, "최대 허용치를 초과하였습니다."),
    EMPTY_NICKNAME(UserCode.EMPTY_NICKNAME.getCode(), NOT_ACCEPTABLE, UserCode.EMPTY_NICKNAME.getMessage()),
    UPDATE_DURATION("", NOT_ACCEPTABLE, "아직 변경할 수 없어요."),
    IMAGE_FILE_SIZE("", NOT_ACCEPTABLE, "사진 파일 용량이 초과되었습니다."),
    REFRESH_TOKEN_EXPIRED("", NOT_ACCEPTABLE, "만료된 Refresh Token을 사용할 수 없습니다."),

    // 409 CONFLICT
    DUPLICATED_NICKNAME("", CONFLICT, "중복된 닉네임입니다."),
    DUPLICATED_LIKE("", CONFLICT, "이미 좋아요한 게시글입니다."),
    CAN_NOT_UPLOADABLE("", CONFLICT, "인증할 수 없습니다."),

    // 503 SERVICE_UNAVAILABLE
    SERVICE_UNAVAILABLE("", HttpStatus.SERVICE_UNAVAILABLE, "서비스에 문제가 발생했습니다."),
    ;

    private final String code;
    private final HttpStatus status;
    private final String detail;
}
