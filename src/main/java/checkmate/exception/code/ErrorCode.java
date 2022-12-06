package checkmate.exception.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;


@Getter
@AllArgsConstructor
public enum ErrorCode {
    // 400 BAD_REQUEST : 잘못된 요청
    INVALID_REQUEST_PARAMETER(CommonCode.REQUEST_PARAMETER.getCode(), BAD_REQUEST, "잘못된 요청 형식입니다."),
    INVALID_JSON_TYPE(CommonCode.JSON_TYPE.getCode(), BAD_REQUEST, "JSON을 파싱할 수 없습니다."),
    UPDATE_DURATION(CommonCode.UPDATE_DURATION.getCode(), BAD_REQUEST, "아직 변경할 수 없습니다."),
    INVALID_WEEK_DAYS(GoalCode.WEEK_DAYS.getCode(), BAD_REQUEST, "올바르지 않은 인증 요일입니다."),
    INVALID_GOAL_DATE(GoalCode.DATE.getCode(), BAD_REQUEST, "올바르지 않은 목표 기간 설정입니다."),
    ALREADY_IN_GOAL(TeamMateCode.ALREADY_IN_GOAL.getCode(), BAD_REQUEST, "이미 해당 목표를 진행 중입니다."),
    DUPLICATED_INVITE_REQUEST(TeamMateCode.DUPLICATED_INVITE.getCode(), BAD_REQUEST, "이미 초대 요청을 보냈습니다."),
    EXCEED_GOAL_INVITEABLE_DATE(GoalCode.INVITEABLE_DATE.getCode(), BAD_GATEWAY, "초대가능한 기간이 지났습니다."),
    EXCEED_GOAL_LIMIT(GoalCode.COUNT_LIMIT.getCode(), BAD_REQUEST, "동시 진행 가능한 목표 수 허용치를 초과하였습니다."),
    EMPTY_NICKNAME(UserCode.EMPTY_NICKNAME.getCode(), BAD_REQUEST, "닉네임을 설정해야 합니다."),
    EXCEED_IMAGE_LIMIT(PostCode.IMAGE_LIMIT.getCode(), BAD_REQUEST, "최대 이미지 수를 초과했습니다."),
    REFRESH_TOKEN_EXPIRED(TokenCode.REFRESH_TOKEN_EXPIRED.getCode(), BAD_REQUEST, "만료된 Refresh Token을 사용할 수 없습니다."),
    IMAGE_PROCESSING_IO(PostCode.IMAGE_IO.getCode(), BAD_REQUEST, "이미지 처리 중 문제가 발생했습니다."),
    NOTIFICATION_PUSH_IO(NotificationCode.PUSH_IO.getCode(), BAD_REQUEST, "알림 전송 중 문제가 발생했습니다."),

    // 404 NOT_FOUND : Resource를 찾을 수 없음
    USER_NOT_FOUND(UserCode.NOT_FOUND.getCode(), NOT_FOUND, "해당 사용자를 찾을 수 없습니다."),
    GOAL_NOT_FOUND(GoalCode.NOT_FOUND.getCode(), NOT_FOUND, "해당 목표를 찾을 수 없습니다."),
    POST_NOT_FOUND(PostCode.NOT_FOUND.getCode(), NOT_FOUND, "해당 게시글을 찾을 수 없습니다."),
    IMAGE_NOT_FOUND(PostCode.IMAGE_NOT_FOUND.getCode(), NOT_FOUND, "해당 이미지를 찾을 수 없습니다."),
    TEAM_MATE_NOT_FOUND(TeamMateCode.NOT_FOUND.getCode(), NOT_FOUND, "해당 팀원을 찾을 수 없습니다."),
    NOTIFICATION_NOT_FOUND(NotificationCode.NOT_FOUND.getCode(), NOT_FOUND, "해당 알림을 찾을 수 없습니다."),
    REFRESH_TOKEN_NOT_FOUND(TokenCode.NOT_FOUND.getCode(), NOT_FOUND, "해당 토큰을 찾을 수 없습니다."),

    // 409 CONFLICT
    DUPLICATED_NICKNAME(UserCode.DUPLICATED_NICKNAME.getCode(), CONFLICT, "중복된 닉네임입니다."),

    // 413 PAYLOAD_TOO_LARGE
    FILE_SIZE(CommonCode.FILE_SIZE.getCode(), PAYLOAD_TOO_LARGE, "파일 용량이 초과되었습니다."),

    // 503 SERVICE_UNAVAILABLE
    SERVICE_UNAVAILABLE(CommonCode.SERVICE_UNAVAILABLE.getCode(), HttpStatus.SERVICE_UNAVAILABLE, "서비스에 문제가 발생했습니다."),
    ;

    private final String code;
    private final HttpStatus status;
    private final String detail;
}
