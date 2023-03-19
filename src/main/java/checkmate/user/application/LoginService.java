package checkmate.user.application;

import checkmate.config.jwt.JwtFactory;
import checkmate.config.jwt.JwtVerifier;
import checkmate.config.jwt.LoginToken;
import checkmate.exception.NotFoundException;
import checkmate.exception.code.ErrorCode;
import checkmate.user.application.dto.request.SnsLoginCommand;
import checkmate.user.application.dto.request.TokenReissueCommand;
import checkmate.user.domain.User;
import checkmate.user.domain.UserRepository;
import checkmate.user.presentation.dto.response.LoginResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
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
    private final RedisTemplate<String, String> redisTemplate;

    @Transactional
    public LoginResponse login(SnsLoginCommand snsLoginCommand) {
        User user = userRepository.findByProviderId(snsLoginCommand.providerId())
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND));
        user.updateFcmToken(snsLoginCommand.fcmToken());
        return toLoginTokenResponse(jwtFactory.createLoginToken(user));
    }

    @Transactional
    public LoginResponse reissueToken(TokenReissueCommand command) {
        String providerId = jwtVerifier.verifyRefeshToken(command.accessToken(), command.refreshToken());
        User user = userRepository.findByProviderId(providerId)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND));
        return toLoginTokenResponse(jwtFactory.createLoginToken(user));
    }

    @Transactional
    public void logout(long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND, userId));
        redisTemplate.delete(user.getProviderId());
    }

    private LoginResponse toLoginTokenResponse(LoginToken loginToken) {
        return LoginResponse.builder()
                .accessToken(loginToken.accessToken())
                .refreshToken(loginToken.refreshToken())
                .build();
    }
}
