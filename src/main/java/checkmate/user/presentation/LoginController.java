package checkmate.user.presentation;

import checkmate.config.auth.JwtUserDetails;
import checkmate.user.application.LoginService;
import checkmate.user.presentation.dto.LoginDtoMapper;
import checkmate.user.presentation.dto.UserAssembler;
import checkmate.user.presentation.dto.request.GoogleLoginDto;
import checkmate.user.presentation.dto.request.KakaoLoginDto;
import checkmate.user.presentation.dto.request.NaverLoginDto;
import checkmate.user.presentation.dto.request.TokenReissueDto;
import checkmate.user.presentation.dto.response.LoginTokenResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@RequiredArgsConstructor
@RestController
public class LoginController {
    private final LoginService loginService;
    private final LoginDtoMapper loginDtoMapper;

    @PostMapping("/login/kakao")
    public LoginTokenResponse kakaoLogin(@RequestBody @Valid KakaoLoginDto kakaoLoginDto) {
        return loginService.login(loginDtoMapper.toCommand(kakaoLoginDto));
    }

    @PostMapping("/login/google")
    public LoginTokenResponse googleLogin(@RequestBody @Valid GoogleLoginDto googleLoginDto) {
        return loginService.login(loginDtoMapper.toCommand(googleLoginDto));
    }

    @PostMapping("/login/naver")
    public LoginTokenResponse naverLogin(@RequestBody @Valid NaverLoginDto naverLoginDto) {
        return loginService.login(loginDtoMapper.toCommand(naverLoginDto));
    }

    @PostMapping("/login/reissue")
    public LoginTokenResponse tokenReissue(@RequestBody TokenReissueDto tokenReissueDto) {
        return loginService.reissueToken(UserAssembler.tokenReissueCommand(tokenReissueDto));
    }

    @DeleteMapping("/login/logout")
    public void logout(@AuthenticationPrincipal JwtUserDetails userDetails) {
        loginService.logout(userDetails.getUserId());
    }
}
