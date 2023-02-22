package checkmate.common.api;

import checkmate.common.cache.CacheKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RequiredArgsConstructor
@RestController
public class TestController {
    @Cacheable(
            value = CacheKey.TODAY_GOALS,
            key = "T(java.time.LocalDate).now().format(@dateFormatter)"
    )
    @GetMapping("/test/log")
    public String testLogging() {
        log.warn("test warn log");
        log.info("test info log");
        log.debug("test dedug log");
        return "ok";
    }
}
