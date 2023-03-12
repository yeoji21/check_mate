package checkmate.exception;


public class JsonConvertingException extends RuntimeException {
    public JsonConvertingException(Throwable e, String message) {
        super(message, e);
    }

    public JsonConvertingException(Throwable e) {
        super(e);
    }
}
