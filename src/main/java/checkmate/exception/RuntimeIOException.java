package checkmate.exception;

import checkmate.exception.code.ErrorCode;
import lombok.Getter;

@Getter
public class RuntimeIOException extends RuntimeException{
    private final ErrorCode errorCode;

    public RuntimeIOException(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    public RuntimeIOException(Throwable cause, ErrorCode errorCode) {
        super(cause);
        this.errorCode = errorCode;
    }
}
