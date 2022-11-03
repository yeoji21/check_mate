package checkmate.user.domain;

import checkmate.common.domain.BaseTimeEntity;
import checkmate.exception.UpdateDurationException;
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
    @Column(name="user_id")
    private Long id;
    @NotNull
    private String username;
    @Email
    private String email;
    private String password;
    @Column(unique = true)
    private String providerId;
    @Column(unique = true)
    private String nickname;
    @NotNull
    private String role;
    private String fcmToken;
    private LocalDate nicknameUpdated;

    @Builder
    protected User(String username,
                   String email,
                   String nickname,
                   String password,
                   String providerId,
                   String role,
                   String fcmToken) {
        this.username = username;
        this.email = email;
        this.nickname = nickname;
        this.password = password;
        this.providerId = providerId;
        this.role = role;
        this.fcmToken = fcmToken;
    }

    public void changeNickname(String nickname) {
        if(nicknameUpdated != null && nicknameUpdated.plusDays(30L).isAfter(LocalDate.now()))
            throw new UpdateDurationException();
        this.nickname = nickname;
        this.nicknameUpdated = LocalDate.now();
    }

    public void updateFcmToken(String fcmToken){
        this.fcmToken = fcmToken;
    }
}
