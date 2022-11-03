package checkmate.common.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RequiredArgsConstructor
@RequestMapping("/test")
@RestController
public class TestController {
    @GetMapping("/log")
    public String testLogging() {
        log.warn("test warn log");
        log.info("test info log");
        log.debug("test dedug log");
        return "ok";
    }
}
