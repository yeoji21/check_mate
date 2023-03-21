package checkmate.user.presentation;

import checkmate.config.auth.JwtUserDetails;
import checkmate.user.application.UserCommandService;
import checkmate.user.application.UserQueryService;
import checkmate.user.presentation.dto.UserDtoMapper;
import checkmate.user.presentation.dto.request.*;
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

    // TODO: 2023/03/18 providerId를 대체할 수 있는 방법 고려
    // 1. emailAddress를 이용 (emailAddress는 unique)
    // 2. Ramdom UUID를 이용
    // 사용자의 emailAddress가 토큰 등에서 노출될 수 있으니 UUID 사용을 고려
    // 하위 호환성을 위해 완전히 전환되기 전까지 기존 API 유지
    @PostMapping("/users")
    public void signUp(@RequestBody @Valid UserSignUpDto userSignUpDto) {
        userCommandService.signUp(userDtoMapper.toCommand(userSignUpDto));
    }

    @PostMapping("/users/kakao")
    public void kakaoSignUp(@RequestBody @Valid KakaoSignUpDto kakaoSignUpDto) {
        userCommandService.signUp(userDtoMapper.toCommand(kakaoSignUpDto));
    }

    @PostMapping("/users/google")
    public void googleSignUp(@RequestBody @Valid GoogleSignUpDto googleSignUpDto) {
        userCommandService.signUp(userDtoMapper.toCommand(googleSignUpDto));
    }

    @PostMapping("/users/naver")
    public void naverSignUp(@RequestBody @Valid NaverSignUpDto naverSignUpDto) {
        userCommandService.signUp(userDtoMapper.toCommand(naverSignUpDto));
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
