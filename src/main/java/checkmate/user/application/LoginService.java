package checkmate.user.application;

import checkmate.config.jwt.JwtFactory;
import checkmate.config.jwt.JwtVerifier;
import checkmate.config.jwt.LoginToken;
import checkmate.exception.NotFoundException;
import checkmate.exception.code.ErrorCode;
import checkmate.user.application.dto.request.LoginCommand;
import checkmate.user.application.dto.request.TokenReissueCommand;
import checkmate.user.domain.User;
import checkmate.user.domain.UserRepository;
import checkmate.user.presentation.dto.response.LoginResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static checkmate.exception.code.ErrorCode.USER_NOT_FOUND;


@Slf4j
@RequiredArgsConstructor
@Service
public class LoginService {
    private final UserRepository userRepository;
    private final JwtFactory jwtFactory;
    private final JwtVerifier jwtVerifier;

    @Transactional
    public LoginResponse login(LoginCommand loginCommand) {
        User user = userRepository.findByIdentifier(loginCommand.identifier())
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND));
        user.updateFcmToken(loginCommand.fcmToken());
        return toLoginTokenResponse(jwtFactory.createLoginToken(user));
    }

    @Transactional
    public LoginResponse reissueToken(TokenReissueCommand command) {
        String identifier = jwtVerifier.parseIdentifier(command.accessToken());
        jwtVerifier.verifyRefeshToken(identifier, command.refreshToken());
        User user = userRepository.findByIdentifier(identifier)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND));
        return toLoginTokenResponse(jwtFactory.createLoginToken(user));
    }

    // TODO: 2023/03/21 logout 요청 시 token 검증 필요한지 고려
    @Transactional
    public void logout(long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND, userId));
        jwtVerifier.expireRefreshToken(user.getIdentifier());
    }

    private LoginResponse toLoginTokenResponse(LoginToken loginToken) {
        return LoginResponse.builder()
                .accessToken(loginToken.accessToken())
                .refreshToken(loginToken.refreshToken())
                .build();
    }
}
