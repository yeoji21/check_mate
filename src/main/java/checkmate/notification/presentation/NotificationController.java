package checkmate.notification.presentation;

import checkmate.config.auth.JwtUserDetails;
import checkmate.notification.application.NotificationQueryService;
import checkmate.notification.application.dto.response.NotificationDetailsResult;
import checkmate.notification.application.dto.response.NotificationInfo;
import checkmate.notification.presentation.dto.NotificationDtoMapper;
import checkmate.notification.presentation.dto.NotificationInfosResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("notification")
@RestController
public class NotificationController {
    private final NotificationQueryService notificationQueryService;
    private final NotificationDtoMapper dtoMapper;

    @GetMapping("/{notificationId}")
    public NotificationInfo findNotificationInfo(@PathVariable long notificationId,
                                                 @AuthenticationPrincipal JwtUserDetails userDetails) {
        return notificationQueryService.findNotificationInfo(notificationId, userDetails.getUserId());
    }

    @GetMapping("/goal-complete")
    public NotificationInfosResult goalCompleteNotifications(@AuthenticationPrincipal JwtUserDetails userDetails) {
        return new NotificationInfosResult(notificationQueryService.findGoalCompleteNotifications(userDetails.getUserId()));
    }

    @GetMapping
    public NotificationDetailsResult notificationInfoListFind(Long cursorId,
                                                              @RequestParam(required = false, defaultValue = "10") int size,
                                                              @AuthenticationPrincipal JwtUserDetails userDetails) {
        return notificationQueryService.findNotificationDetails(
                dtoMapper.toCriteria(cursorId, size, userDetails.getUserId()));
    }
}
