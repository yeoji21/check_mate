package checkmate.exception;

public class JsonConvertingException extends BusinessException{
    public JsonConvertingException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
