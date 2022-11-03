package checkmate.user.presentation.dto.request;

import checkmate.user.domain.ProviderIdGenerator;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GoogleLoginDto {
    @NotBlank(message = "providerId is blank")
    private String providerId;
    private String fcmToken;

    public GoogleLoginDto(String providerId, String fcmToken) {
        this.providerId = ProviderIdGenerator.google(providerId);
        this.fcmToken = fcmToken;
    }
}
