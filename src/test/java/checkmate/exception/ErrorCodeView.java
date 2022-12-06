package checkmate.exception;

import lombok.Getter;

import java.util.Map;

@Getter
public class ErrorCodeView {
    private Map<String, String> errorCodes;

    public ErrorCodeView(Map<String, String> errorCodes) {
        this.errorCodes = errorCodes;
    }
}
