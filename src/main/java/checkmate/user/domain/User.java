package checkmate.user.domain;

import checkmate.common.domain.BaseTimeEntity;
import checkmate.exception.format.BusinessException;
import checkmate.exception.format.ErrorCode;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Getter
@Table(name = "users")
@EqualsAndHashCode(of = "id")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class User extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Long id;
    @NotNull
    @Column(name = "username")
    private String username;
    @Email @Column(name = "email_address")
    private String emailAddress;
    @Column(name = "password")
    private String password;
    @Column(unique = true, name ="provider_id")
    private String providerId;
    @Column(unique = true, name = "nickname")
    private String nickname;
    @NotNull @Column(name = "role")
    private String role;
    @Column(name = "fcm_token")
    private String fcmToken;
    @Column(name = "nickname_updated_date")
    private LocalDate nicknameUpdatedDate;

    @Builder
    protected User(String username,
                   String emailAddress,
                   String nickname,
                   String password,
                   String providerId,
                   String role,
                   String fcmToken) {
        this.username = username;
        this.emailAddress = emailAddress;
        this.nickname = nickname;
        this.password = password;
        this.providerId = providerId;
        this.role = role;
        this.fcmToken = fcmToken;
    }

    public void changeNickname(String nickname) {
        if(nicknameUpdatedDate != null && nicknameUpdatedDate.plusDays(30L).isAfter(LocalDate.now()))
            throw new BusinessException(ErrorCode.UPDATE_DURATION);
        this.nickname = nickname;
        this.nicknameUpdatedDate = LocalDate.now();
    }

    public void updateFcmToken(String fcmToken){
        this.fcmToken = fcmToken;
    }
}
