package checkmate.exception.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;


@Getter
@AllArgsConstructor
public enum ErrorCode {
    // 400 BAD_REQUEST : 잘못된 요청
    INVALID_REQUEST_PARAMETER(CommonCode.REQUEST_PARAMETER.getCode(), BAD_REQUEST, "잘못된 요청 형식인 경우"),
    INVALID_JSON_TYPE(CommonCode.JSON_TYPE.getCode(), BAD_REQUEST, "JSON을 파싱할 수 없는 경우"),
    UPDATE_DURATION(CommonCode.UPDATE_DURATION.getCode(), BAD_REQUEST, "변경할 수 없는 기간인 경우"),
    INVALID_WEEK_DAYS(GoalCode.WEEK_DAYS.getCode(), BAD_REQUEST, "목표 인증 요일이 올바르지 않은 경우"),
    INVALID_GOAL_DATE(GoalCode.DATE.getCode(), BAD_REQUEST, "목표 기간 설정이 올바르지 않은 경우"),
    INVALID_MATE_STATUS(MateCode.INVALID_STATUS.getCode(), BAD_REQUEST, "팀원의 상태가 올바르지 않은 경우"),
    ALREADY_IN_GOAL(MateCode.ALREADY_IN_GOAL.getCode(), BAD_REQUEST, "이미 해당 목표를 진행 중인 유저를 초대하는 경우"),
    DUPLICATED_INVITE_REQUEST(MateCode.DUPLICATED_INVITE.getCode(), BAD_REQUEST, "이미 해당 목표 초대 요청을 받은 유저를 다시 초대하는 경우"),
    EXCEED_GOAL_INVITEABLE_DATE(GoalCode.INVITEABLE_DATE.getCode(), BAD_GATEWAY, "초대 가능한 기간이 지난 목표로 초대하는 경우"),
    EXCEED_GOAL_LIMIT(GoalCode.COUNT_LIMIT.getCode(), BAD_REQUEST, "동시 진행 가능한 목표 수 허용치를 초과한 경우"),
    EMPTY_NICKNAME(UserCode.EMPTY_NICKNAME.getCode(), BAD_REQUEST, "닉네임을 설정하지 않은 경우"),
    EXCEED_IMAGE_LIMIT(PostCode.IMAGE_LIMIT.getCode(), BAD_REQUEST, "업로드 가능한 최대 이미지 수를 초과한 경우"),
    REFRESH_TOKEN_EXPIRED(TokenCode.REFRESH_TOKEN_EXPIRED.getCode(), BAD_REQUEST, "Refresh Token이 만료된 경우"),
    IMAGE_PROCESSING_IO(PostCode.IMAGE_IO.getCode(), BAD_REQUEST, "이미지 처리 중 문제가 발생한 경우"),
    DATA_INTEGRITY_VIOLATE(CommonCode.DATA_INTEGTITY.getCode(), BAD_REQUEST, "데이터 무결성을 위반한 경우"),

    UNAUTHORIZED_OPERATION(CommonCode.UNAUTHORIZED_OPERATION.getCode(), BAD_REQUEST, "허가되지 않은 작업을 시도한 경우"),

    // 404 NOT_FOUND : Resource를 찾을 수 없음
    USER_NOT_FOUND(UserCode.NOT_FOUND.getCode(), NOT_FOUND, "존재하지 않는 유저"),
    GOAL_NOT_FOUND(GoalCode.NOT_FOUND.getCode(), NOT_FOUND, "존재하지 않는 목표"),
    POST_NOT_FOUND(PostCode.NOT_FOUND.getCode(), NOT_FOUND, "존재하지 않는 게시글"),
    IMAGE_NOT_FOUND(PostCode.IMAGE_NOT_FOUND.getCode(), NOT_FOUND, "존재하지 않는 이미지"),
    MATE_NOT_FOUND(MateCode.NOT_FOUND.getCode(), NOT_FOUND, "존재하지 않는 팀원"),
    NOTIFICATION_NOT_FOUND(NotificationCode.NOT_FOUND.getCode(), NOT_FOUND, "존재하지 않는 알림"),
    REFRESH_TOKEN_NOT_FOUND(TokenCode.NOT_FOUND.getCode(), NOT_FOUND, "존재하지 않는 토큰"),

    // 409 CONFLICT
    DUPLICATED_NICKNAME(UserCode.DUPLICATED_NICKNAME.getCode(), CONFLICT, "동일한 닉네임이 존재하는 경우"),

    // 413 PAYLOAD_TOO_LARGE
    FILE_SIZE(CommonCode.FILE_SIZE.getCode(), PAYLOAD_TOO_LARGE, "파일 용량이 초과된 경우"),

    // 503 SERVICE_UNAVAILABLE
    SERVICE_UNAVAILABLE(CommonCode.SERVICE_UNAVAILABLE.getCode(), HttpStatus.SERVICE_UNAVAILABLE, "서비스에 문제가 발생한 경우"),
    ;

    private final String code;
    private final HttpStatus status;
    private final String detail;
}
