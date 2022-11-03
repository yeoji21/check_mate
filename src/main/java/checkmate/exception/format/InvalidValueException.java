package checkmate.exception.format;

public class InvalidValueException extends BusinessException{
    public InvalidValueException(ErrorCode errorCode) {
        super(errorCode);
    }
}
