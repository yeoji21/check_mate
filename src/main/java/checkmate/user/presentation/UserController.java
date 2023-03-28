package checkmate.user.presentation;

import checkmate.config.auth.JwtUserDetails;
import checkmate.user.application.UserCommandService;
import checkmate.user.application.UserQueryService;
import checkmate.user.presentation.dto.UserDtoMapper;
import checkmate.user.presentation.dto.request.UserNicknameModifyDto;
import checkmate.user.presentation.dto.request.UserSignUpDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

// TODO: 2023/03/02 회원가입 API 통합 고려
@Slf4j
@RequiredArgsConstructor
@RestController
public class UserController {
    private final UserQueryService userQueryService;
    private final UserCommandService userCommandService;
    private final UserDtoMapper userDtoMapper;

    @PostMapping("/users")
    public void signUp(@RequestBody @Valid UserSignUpDto userSignUpDto) {
        userCommandService.signUp(userDtoMapper.toCommand(userSignUpDto));
    }

    @PatchMapping("/users/nickname")
    public void updateNickname(@RequestBody @Valid UserNicknameModifyDto userNicknameModifyDto,
                               @AuthenticationPrincipal JwtUserDetails userDetails) {
        userCommandService.nicknameUpdate(userDtoMapper.toCommand(userDetails.getUserId(), userNicknameModifyDto));
    }

    /**
     * 백오피스 전용 회원 삭제 API
     *
     * @param nickname - 삭제할 회원의 닉네임
     */
    @DeleteMapping("/users/{nickname}")
    public void delete(@PathVariable String nickname) {
        userCommandService.delete(nickname);
    }

    @GetMapping("/users/exists")
    public void nicknameDuplicateCheck(@RequestParam String nickname) {
        userQueryService.existsNicknameCheck(nickname);
    }
}
