package checkmate.user.application;

import checkmate.config.auth.AuthConstants;
import checkmate.config.jwt.JwtDecoder;
import checkmate.config.jwt.JwtFactory;
import checkmate.exception.BusinessException;
import checkmate.exception.ErrorCode;
import checkmate.exception.NotFoundException;
import checkmate.user.application.dto.request.SnsLoginCommand;
import checkmate.user.application.dto.request.TokenReissueCommand;
import checkmate.user.domain.User;
import checkmate.user.domain.UserRepository;
import checkmate.user.presentation.dto.response.LoginTokenResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static checkmate.exception.ErrorCode.USER_NOT_FOUND;


@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class LoginService {
    private final UserRepository userRepository;
    private final JwtFactory jwtFactory;
    private final JwtDecoder jwtDecoder;
    private final RedisTemplate<String, String> redisTemplate;

    public LoginTokenResponse login(SnsLoginCommand snsLoginCommand) {
        User user = userRepository.findByProviderId(snsLoginCommand.getProviderId())
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND));
        fcmTokenUpdate(user, snsLoginCommand.getFcmToken());
        if(user.getNickname() == null) throw new BusinessException(ErrorCode.EMPTY_NICKNAME);
        return getLoginTokenResponse(user);
    }

    public LoginTokenResponse reissueToken(TokenReissueCommand command) {
        String providerId = jwtDecoder.getProviderId(command.getAccessToken());
        refreshTokenExistCheck(providerId, command.getRefreshToken());
        User user = userRepository.findByProviderId(providerId).orElseThrow(() -> new NotFoundException(USER_NOT_FOUND));
        return getLoginTokenResponse(user);
    }

    private LoginTokenResponse getLoginTokenResponse(User user) {
        LoginTokenResponse loginTokenResponse = LoginTokenResponse.builder()
                .accessToken(jwtFactory.accessToken(user))
                .refreshToken(jwtFactory.refreshToken())
                .build();
        redisTemplate.opsForValue().set(user.getProviderId(), loginTokenResponse.getRefreshToken(), 30, TimeUnit.DAYS);
        return loginTokenResponse;
    }

    public void logout(long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND, userId));
        redisTemplate.delete(user.getProviderId());
    }

    private void refreshTokenExistCheck(String providerId, String refreshToken) {
        Optional<String> findRefreshToken = Optional.ofNullable(redisTemplate.opsForValue().get(providerId));
        findRefreshToken.ifPresentOrElse(
                findToken -> {
                    if (!findToken.equals(AuthConstants.TOKEN_PREFIX.getValue() + refreshToken))
                        throw new NotFoundException(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
                },
                () -> {
                    throw new NotFoundException(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
                });
    }

    private void fcmTokenUpdate(User user, String fcmToken) {
        if(!(user.getFcmToken() != null && user.getFcmToken().equals(fcmToken)))
            user.updateFcmToken(fcmToken);
    }

}
