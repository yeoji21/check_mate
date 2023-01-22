package checkmate.user.presentation.dto.request;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GoogleLoginDto {
    @NotBlank(message = "providerId is blank")
    private String providerId;
    private String fcmToken;

    @Builder
    public GoogleLoginDto(String providerId, String fcmToken) {
        this.providerId =providerId;
        this.fcmToken = fcmToken;
    }
}
