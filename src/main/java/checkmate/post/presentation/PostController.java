package checkmate.post.presentation;

import checkmate.config.auth.JwtUserDetails;
import checkmate.post.application.PostCommandService;
import checkmate.post.application.PostQueryService;
import checkmate.post.application.dto.response.PostInfoListResult;
import checkmate.post.presentation.dto.PostDate;
import checkmate.post.presentation.dto.PostDtoMapper;
import checkmate.post.presentation.dto.PostUploadDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
public class PostController {
    private final PostCommandService postCommandService;
    private final PostQueryService postQueryService;
    private final PostDtoMapper postDtoMapper;

    @PostMapping("/post")
    public long uploadPost(@ModelAttribute PostUploadDto dto,
                           @AuthenticationPrincipal JwtUserDetails principal) {
        if (dto.getImages() == null && dto.getContent() == null)
            throw new IllegalArgumentException("빈 목표인증 요청");
        return postCommandService.upload(postDtoMapper.toCommand(dto, principal.getUserId()));
    }

    @GetMapping("/post")
    public PostInfoListResult findPostListByDate(@RequestParam long goalId,
                                                 @RequestParam @PostDate String date) {
        return postQueryService.findPostByGoalIdAndDate(goalId, date);
    }

    @PostMapping("/post/{postId}/like")
    public void like(@AuthenticationPrincipal JwtUserDetails principal, @PathVariable long postId) {
        postCommandService.like(principal.getUserId(), postId);
    }

    @DeleteMapping("/post/{postId}/unlike")
    public void unlike(@AuthenticationPrincipal JwtUserDetails principal, @PathVariable long postId) {
        postCommandService.unlike(principal.getUserId(), postId);
    }
}