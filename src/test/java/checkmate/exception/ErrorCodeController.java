package checkmate.exception;

import checkmate.exception.code.ErrorCode;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class ErrorCodeController {

    @GetMapping("/error-code")
    public Map<String, String> getErrorCodes() {
        return Arrays.stream(ErrorCode.values())
                .collect(Collectors.toMap(ErrorCode::getCode, ErrorCode::getDetail));
    }
}
