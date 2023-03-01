package checkmate.notification.presentation;

import checkmate.config.auth.JwtUserDetails;
import checkmate.notification.application.NotificationQueryService;
import checkmate.notification.application.dto.response.NotificationDetailResult;
import checkmate.notification.application.dto.response.NotificationInfo;
import checkmate.notification.presentation.dto.NotificationDtoMapper;
import checkmate.notification.presentation.dto.NotificationInfoResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
public class NotificationController {
    private final NotificationQueryService notificationQueryService;
    private final NotificationDtoMapper dtoMapper;

    @GetMapping("/notifications/{notificationId}")
    public NotificationInfo findNotificationInfo(@PathVariable long notificationId,
                                                 @AuthenticationPrincipal JwtUserDetails userDetails) {
        return notificationQueryService.findNotificationInfo(notificationId, userDetails.getUserId());
    }

    @GetMapping("/notifications/goal-complete")
    public NotificationInfoResult goalCompleteNotifications(@AuthenticationPrincipal JwtUserDetails userDetails) {
        return notificationQueryService.findGoalCompleteNotifications(userDetails.getUserId());
    }

    @GetMapping("/notifications")
    public NotificationDetailResult findNotifications(Long cursorId,
                                                      @RequestParam(required = false, defaultValue = "10") int size,
                                                      @AuthenticationPrincipal JwtUserDetails userDetails) {
        return notificationQueryService
                .findNotificationDetails(dtoMapper.toCriteria(cursorId, size, userDetails.getUserId()));
    }
}
