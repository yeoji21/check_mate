package checkmate.post.presentation;

import checkmate.common.interceptor.GoalId;
import checkmate.common.interceptor.GoalMember;
import checkmate.config.auth.JwtUserDetails;
import checkmate.post.application.PostCommandService;
import checkmate.post.application.PostQueryService;
import checkmate.post.application.dto.response.PostCreateResult;
import checkmate.post.application.dto.response.PostInfoResult;
import checkmate.post.presentation.dto.PostCreateDto;
import checkmate.post.presentation.dto.PostDate;
import checkmate.post.presentation.dto.PostDtoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
public class PostController {

    private final PostCommandService postCommandService;
    private final PostQueryService postQueryService;
    private final PostDtoMapper postDtoMapper;

    @PostMapping("/posts")
    public PostCreateResult create(
        @ModelAttribute PostCreateDto dto,
        @AuthenticationPrincipal JwtUserDetails principal) {
        return postCommandService.create(postDtoMapper.toCommand(dto, principal.getUserId()));
    }

    @GetMapping("/goals/{goalId}/posts/{date}")
    public PostInfoResult findPostInfoByDate(
        @PathVariable long goalId,
        @PathVariable @PostDate String date) {
        return postQueryService.findPostByGoalIdAndDate(goalId, date);
    }

    @GoalMember(value = GoalId.PATH_VARIABLE)
    @PostMapping("/goals/{goalId}/posts/{postId}/like")
    public void like(
        @PathVariable long goalId,
        @PathVariable long postId,
        @AuthenticationPrincipal JwtUserDetails details) {
        postCommandService.like(details.getUserId(), postId);
    }

    @GoalMember(value = GoalId.PATH_VARIABLE)
    @DeleteMapping("/goals/{goalId}/posts/{postId}/unlike")
    public void unlike(
        @PathVariable long goalId,
        @PathVariable long postId,
        @AuthenticationPrincipal JwtUserDetails details) {
        postCommandService.unlike(details.getUserId(), postId);
    }
}