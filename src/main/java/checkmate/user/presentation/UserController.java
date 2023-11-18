package checkmate.user.presentation;

import checkmate.config.auth.JwtUserDetails;
import checkmate.user.application.UserCommandService;
import checkmate.user.application.UserQueryService;
import checkmate.user.presentation.dto.UserDtoMapper;
import checkmate.user.presentation.dto.UserScheduleResponse;
import checkmate.user.presentation.dto.request.UserNicknameModifyDto;
import checkmate.user.presentation.dto.request.UserSignUpDto;
import java.time.LocalDate;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
public class UserController {

    private final UserQueryService userQueryService;
    private final UserCommandService userCommandService;
    private final UserDtoMapper userDtoMapper;

    @PostMapping("/users")
    public void signUp(@RequestBody @Valid UserSignUpDto userSignUpDto) {
        userCommandService.signUp(userDtoMapper.toCommand(userSignUpDto));
    }

    @PatchMapping("/users/nickname")
    public void updateNickname(@RequestBody @Valid UserNicknameModifyDto userNicknameModifyDto,
        @AuthenticationPrincipal JwtUserDetails userDetails) {
        userCommandService.nicknameUpdate(
            userDtoMapper.toCommand(userDetails.getUserId(), userNicknameModifyDto));
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

    @GetMapping("/users/weekly-schedule")
    public UserScheduleResponse getWeeklySchedule(
        @RequestParam String date,
        @AuthenticationPrincipal JwtUserDetails details) {
        return userQueryService.getWeeklySchdule(details.getUserId(), LocalDate.parse(date));
    }
}
