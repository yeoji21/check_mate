package checkmate.user.presentation;

import checkmate.config.auth.JwtUserDetails;
import checkmate.user.application.LoginService;
import checkmate.user.presentation.dto.LoginDtoMapper;
import checkmate.user.presentation.dto.request.*;
import checkmate.user.presentation.dto.response.LoginResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

// TODO: 2023/03/02 로그인 API 통합 고려
@Slf4j
@RequiredArgsConstructor
@RestController
public class LoginController {
    private final LoginService loginService;
    private final LoginDtoMapper loginDtoMapper;

    @PostMapping("/users/login")
    public LoginResponse login(@RequestBody @Valid LoginRequestDto loginRequestDto) {
        return loginService.login(loginDtoMapper.toCommand(loginRequestDto));
    }

    @PostMapping("/login/kakao")
    public LoginResponse kakaoLogin(@RequestBody @Valid KakaoLoginDto kakaoLoginDto) {
        return loginService.login_v1(loginDtoMapper.toCommand(kakaoLoginDto));
    }

    @PostMapping("/login/google")
    public LoginResponse googleLogin(@RequestBody @Valid GoogleLoginDto googleLoginDto) {
        return loginService.login_v1(loginDtoMapper.toCommand(googleLoginDto));
    }

    @PostMapping("/login/naver")
    public LoginResponse naverLogin(@RequestBody @Valid NaverLoginDto naverLoginDto) {
        return loginService.login_v1(loginDtoMapper.toCommand(naverLoginDto));
    }

    @PostMapping("/login/reissue")
    public LoginResponse tokenReissue(@RequestBody TokenReissueDto tokenReissueDto) {
        return loginService.reissueToken(loginDtoMapper.toCommand(tokenReissueDto));
    }

    @DeleteMapping("/user/logout")
    public void logout(@AuthenticationPrincipal JwtUserDetails userDetails) {
        loginService.logout(userDetails.getUserId());
    }
}
