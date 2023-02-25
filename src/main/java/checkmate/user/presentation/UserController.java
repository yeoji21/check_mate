package checkmate.user.presentation;

import checkmate.config.auth.JwtUserDetails;
import checkmate.user.application.UserCommandService;
import checkmate.user.application.UserQueryService;
import checkmate.user.presentation.dto.UserDtoMapper;
import checkmate.user.presentation.dto.request.GoogleSignUpDto;
import checkmate.user.presentation.dto.request.KakaoSignUpDto;
import checkmate.user.presentation.dto.request.NaverSignUpDto;
import checkmate.user.presentation.dto.request.UserNicknameModifyDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@RequiredArgsConstructor
@RestController
public class UserController {
    private final UserQueryService userQueryService;
    private final UserCommandService userCommandService;
    private final UserDtoMapper userDtoMapper;

    @PostMapping("/user/kakao")
    public void kakaoSignUp(@RequestBody @Valid KakaoSignUpDto kakaoSignUpDto) {
        userCommandService.signUp(userDtoMapper.toCommand(kakaoSignUpDto));
    }

    @PostMapping("/user/google")
    public void googleSignUp(@RequestBody @Valid GoogleSignUpDto googleSignUpDto) {
        userCommandService.signUp(userDtoMapper.toCommand(googleSignUpDto));
    }

    @PostMapping("/user/naver")
    public void naverSignUp(@RequestBody @Valid NaverSignUpDto naverSignUpDto) {
        userCommandService.signUp(userDtoMapper.toCommand(naverSignUpDto));
    }

    @PatchMapping("/user/nickname")
    public void updateNickname(@RequestBody @Valid UserNicknameModifyDto userNicknameModifyDto,
                               @AuthenticationPrincipal JwtUserDetails userDetails) {
        userCommandService.nicknameUpdate(userDtoMapper.toCommand(userDetails.getUserId(), userNicknameModifyDto));
    }

    @GetMapping("/user/exists")
    public void nicknameDuplicateCheck(@RequestParam String nickname) {
        userQueryService.existsNicknameCheck(nickname);
    }
}
