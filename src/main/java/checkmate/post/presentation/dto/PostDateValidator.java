package checkmate.post.presentation.dto;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class PostDateValidator implements ConstraintValidator<PostDate, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value.matches("\\d{8}");
    }
}
