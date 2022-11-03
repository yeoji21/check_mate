package checkmate.goal.presentation;

import checkmate.config.auth.JwtUserDetails;
import checkmate.config.redis.RedisKey;
import checkmate.goal.application.TeamMateCommandService;
import checkmate.goal.application.TeamMateQueryService;
import checkmate.goal.application.dto.response.TeamMateCalendarInfo;
import checkmate.goal.application.dto.response.TeamMateInviteReplyResult;
import checkmate.goal.presentation.dto.TeamMateDtoMapper;
import checkmate.goal.presentation.dto.request.TeamMateInviteDto;
import checkmate.goal.presentation.dto.request.TeamMateInviteReplyDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RequiredArgsConstructor
@Slf4j
@RestController
public class TeamMateController {
    private final TeamMateCommandService teamMateCommandService;
    private final TeamMateQueryService teamMateQueryService;
    private final TeamMateDtoMapper mapper;

    @PostMapping("/mate")
    public void inviteToGoal(@RequestBody @Valid TeamMateInviteDto inviteDto,
                             @AuthenticationPrincipal JwtUserDetails principal) {
        teamMateCommandService.inviteTeamMate(mapper.toInviteCommand(inviteDto, principal.getUserId()));
    }

    @Caching(evict = {
            @CacheEvict(
                    value = RedisKey.ONGOING_GOALS,
                    key = "{#principal.userId, T(java.time.LocalDate).now()}",
                    condition = "#dto.accept == true"),
            @CacheEvict(value =
                    RedisKey.TODAY_GOALS,
                    key = "{#principal.userId, T(java.time.LocalDate).now()}",
                    condition = "#dto.accept == true")
    })
    @PatchMapping("/mate")
    public TeamMateInviteReplyResult inviteReply(@RequestBody TeamMateInviteReplyDto dto,
                                                 @AuthenticationPrincipal JwtUserDetails principal) {
        return teamMateCommandService.applyInviteReply(mapper.toInviteReplyCommand(dto));
    }

    @GetMapping("/mate/{teamMateId}/calendar")
    public TeamMateCalendarInfo teamMateGoalCalender(@PathVariable long teamMateId) {
        return teamMateQueryService.getCalenderInfo(teamMateId);
    }

    @GetMapping("/mate/{teamMateId}/progress")
    public double progressPercent(@PathVariable Long teamMateId) {
        return teamMateQueryService.getProgressPercent(teamMateId);
    }
}
