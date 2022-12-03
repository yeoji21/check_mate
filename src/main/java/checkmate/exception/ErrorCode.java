package checkmate.exception;

import checkmate.common.CommonCode;
import checkmate.config.jwt.TokenCode;
import checkmate.goal.domain.GoalCode;
import checkmate.goal.domain.TeamMateCode;
import checkmate.notification.domain.NotificationCode;
import checkmate.post.domain.PostCode;
import checkmate.user.domain.UserCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;


@Getter
@AllArgsConstructor
public enum ErrorCode {
    // 400 BAD_REQUEST : 잘못된 요청
    INVALID_REQUEST_PARAMETER(CommonCode.REQUEST_PARAMETER.getCode(), BAD_REQUEST, CommonCode.REQUEST_PARAMETER.getMessage()),
    INVALID_JSON_TYPE(CommonCode.JSON_TYPE.getCode(), BAD_REQUEST, CommonCode.JSON_TYPE.getMessage()),
    UPDATE_DURATION(CommonCode.UPDATE_DURATION.getCode(), BAD_REQUEST, CommonCode.UPDATE_DURATION.getMessage()),
    INVALID_GOAL_WEEK_DAYS(GoalCode.WEEK_DAYS.getCode(), BAD_REQUEST, GoalCode.WEEK_DAYS.getMessage()),
    INVALID_GOAL_DATE(GoalCode.DATE_RANGE.getCode(), BAD_REQUEST, GoalCode.DATE_RANGE.getMessage()),
    ALREADY_IN_GOAL(TeamMateCode.ALREADY_IN_GOAL.getCode(), BAD_REQUEST, TeamMateCode.ALREADY_IN_GOAL.getMessage()),
    DUPLICATED_INVITE_REQUEST(TeamMateCode.DUPLICATED_INVITE.getCode(), BAD_REQUEST, TeamMateCode.DUPLICATED_INVITE.getMessage()),
    EXCEED_GOAL_LIMIT(GoalCode.COUNT_LIMIT.getCode(), BAD_REQUEST, GoalCode.COUNT_LIMIT.getMessage()),
    EMPTY_NICKNAME(UserCode.EMPTY_NICKNAME.getCode(), BAD_REQUEST, UserCode.EMPTY_NICKNAME.getMessage()),
    EXCEED_IMAGE_LIMIT(PostCode.IMAGE_LIMIT.getCode(), BAD_REQUEST, PostCode.IMAGE_LIMIT.getMessage()),
    REFRESH_TOKEN_EXPIRED(TokenCode.REFRESH_TOKEN_EXPIRED.getCode(), BAD_REQUEST, TokenCode.REFRESH_TOKEN_EXPIRED.getMessage()),
    IMAGE_PROCESSING_IO(PostCode.IMAGE_IO.getCode(), BAD_REQUEST, PostCode.IMAGE_IO.getMessage()),
    NOTIFICATION_PUSH_IO(NotificationCode.PUSH_IO.getCode(), BAD_REQUEST, NotificationCode.PUSH_IO.getMessage()),

    // 404 NOT_FOUND : Resource를 찾을 수 없음
    USER_NOT_FOUND(UserCode.NOT_FOUND.getCode(), NOT_FOUND, UserCode.NOT_FOUND.getMessage()),
    GOAL_NOT_FOUND(GoalCode.NOT_FOUND.getCode(), NOT_FOUND, GoalCode.NOT_FOUND.getMessage()),
    POST_NOT_FOUND(PostCode.NOT_FOUND.getCode(), NOT_FOUND, PostCode.NOT_FOUND.getMessage()),
    IMAGE_NOT_FOUND(PostCode.IMAGE_NOT_FOUND.getCode(), NOT_FOUND, PostCode.IMAGE_NOT_FOUND.getMessage()),
    TEAM_MATE_NOT_FOUND(TeamMateCode.NOT_FOUND.getCode(), NOT_FOUND, TeamMateCode.NOT_FOUND.getMessage()),
    NOTIFICATION_NOT_FOUND(NotificationCode.NOT_FOUND.getCode(), NOT_FOUND, NotificationCode.NOT_FOUND.getMessage()),
    REFRESH_TOKEN_NOT_FOUND(TokenCode.NOT_FOUND.getCode(), NOT_FOUND, TokenCode.NOT_FOUND.getMessage()),

    // 409 CONFLICT
    DUPLICATED_NICKNAME(UserCode.DUPLICATED_NICKNAME.getCode(), CONFLICT, UserCode.DUPLICATED_NICKNAME.getMessage()),

    // 413 PAYLOAD_TOO_LARGE
    FILE_SIZE(CommonCode.FILE_SIZE.getCode(), PAYLOAD_TOO_LARGE, CommonCode.FILE_SIZE.getMessage()),

    // 503 SERVICE_UNAVAILABLE
    SERVICE_UNAVAILABLE(CommonCode.SERVICE_UNAVAILABLE.getCode(), HttpStatus.SERVICE_UNAVAILABLE, CommonCode.SERVICE_UNAVAILABLE.getMessage()),
    ;

    private final String code;
    private final HttpStatus status;
    private final String detail;
}
