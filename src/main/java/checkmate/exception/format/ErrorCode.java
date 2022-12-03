package checkmate.exception.format;

import checkmate.goal.domain.GoalCode;
import checkmate.goal.domain.TeamMateCode;
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
    INVALID_GOAL_WEEK_DAYS(GoalCode.WEEK_DAYS.getCode(), BAD_REQUEST, GoalCode.WEEK_DAYS.getMessage()),
    INVALID_GOAL_DATE(GoalCode.DATE_RANGE.getCode(), BAD_REQUEST, GoalCode.DATE_RANGE.getMessage()),
    ALREADY_IN_GOAL("", BAD_REQUEST, "이미 함께 목표를 진행 중인 동료에요!"),
    DUPLICATED_INVITE_REQUEST("", BAD_REQUEST, "이미 초대 요청을 보냈어요!"),
    EXCEED_GOAL_LIMIT(GoalCode.COUNT_LIMIT.getCode(), BAD_REQUEST, GoalCode.COUNT_LIMIT.getMessage()),
    EMPTY_NICKNAME(UserCode.EMPTY_NICKNAME.getCode(), BAD_REQUEST, UserCode.EMPTY_NICKNAME.getMessage()),

    // 404 NOT_FOUND : Resource를 찾을 수 없음
    USER_NOT_FOUND(UserCode.NOT_FOUND.getCode(), NOT_FOUND, UserCode.NOT_FOUND.getMessage()),
    GOAL_NOT_FOUND(GoalCode.NOT_FOUND.getCode(), NOT_FOUND, GoalCode.NOT_FOUND.getMessage()),
    IMAGE_NOT_FOUND("IMAGE-001", NOT_FOUND, "해당 이미지를 찾을 수 없습니다."),
    TEAM_MATE_NOT_FOUND(TeamMateCode.NOT_FOUND.getCode(), NOT_FOUND, TeamMateCode.NOT_FOUND.getMessage()),
    NOTIFICATION_NOT_FOUND("NOTI-001", NOT_FOUND, "해당 알림을 찾을 수 없습니다."),
    REFRESH_TOKEN_NOT_FOUND("TOKEN-001", NOT_FOUND, "해당 토큰을 찾을 수 없습니다."),

    // 406 NOT_ACCEPTABLE : 거절
    EXCEED_IMAGE_LIMIT("", NOT_ACCEPTABLE, "최대 허용치를 초과했습니다."),
    UPDATE_DURATION("", NOT_ACCEPTABLE, "아직 변경할 수 없어요."),
    IMAGE_FILE_SIZE("", NOT_ACCEPTABLE, "사진 파일 용량이 초과되었습니다."),
    REFRESH_TOKEN_EXPIRED("", NOT_ACCEPTABLE, "만료된 Refresh Token을 사용할 수 없습니다."),

    // 409 CONFLICT
    DUPLICATED_NICKNAME(UserCode.DUPLICATED_NICKNAME.getCode(), CONFLICT, UserCode.DUPLICATED_NICKNAME.getMessage()),
    DUPLICATED_LIKE("", CONFLICT, "이미 좋아요한 게시글입니다."),

    // 503 SERVICE_UNAVAILABLE
    SERVICE_UNAVAILABLE("", HttpStatus.SERVICE_UNAVAILABLE, "서비스에 문제가 발생했습니다."),
    ;

    private final String code;
    private final HttpStatus status;
    private final String detail;
}
