package checkmate.post.presentation.dto;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({PARAMETER})
@Retention(RUNTIME)
@Constraint(validatedBy = PostDateValidator.class)
public @interface PostDate {
    String message() default "올바르지 않은 날짜 형식입니다.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
