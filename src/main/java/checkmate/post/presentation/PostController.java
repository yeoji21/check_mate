package checkmate.post.presentation;

import checkmate.config.auth.JwtUserDetails;
import checkmate.post.application.PostCommandService;
import checkmate.post.application.PostQueryService;
import checkmate.post.application.dto.response.PostInfoResult;
import checkmate.post.application.dto.response.PostUploadResult;
import checkmate.post.presentation.dto.PostDate;
import checkmate.post.presentation.dto.PostDtoMapper;
import checkmate.post.presentation.dto.PostUploadDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
public class PostController {
    private final PostCommandService postCommandService;
    private final PostQueryService postQueryService;
    private final PostDtoMapper postDtoMapper;

    @PostMapping("/posts")
    public PostUploadResult upload(@ModelAttribute PostUploadDto dto,
                                   @AuthenticationPrincipal JwtUserDetails principal) {
        if (dto.getImages() == null && dto.getContent() == null)
            throw new IllegalArgumentException("빈 목표인증 요청");
        return postCommandService.upload(postDtoMapper.toCommand(dto, principal.getUserId()));
    }

    // TODO: 2023/03/02 API 명세 변경 고려
    @GetMapping("/posts")
    public PostInfoResult findPostInfoByDate(@RequestParam long goalId,
                                             @RequestParam @PostDate String date) {
        return postQueryService.findPostByGoalIdAndDate(goalId, date);
    }

    @PostMapping("/posts/{postId}/like")
    public void like(@PathVariable long postId,
                     @AuthenticationPrincipal JwtUserDetails principal) {
        postCommandService.like(principal.getUserId(), postId);
    }

    @DeleteMapping("/posts/{postId}/unlike")
    public void unlike(@PathVariable long postId,
                       @AuthenticationPrincipal JwtUserDetails principal) {
        postCommandService.unlike(principal.getUserId(), postId);
    }
}