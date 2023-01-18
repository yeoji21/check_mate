package checkmate.exception;

import checkmate.exception.code.ErrorCode;
import lombok.Getter;

@Getter
public class UnauthorizedException extends RuntimeException{
    private final ErrorCode errorCode = ErrorCode.UNAUTHORIZED_OPERATION;
}
