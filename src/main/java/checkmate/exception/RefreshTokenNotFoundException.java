package checkmate.exception;

import checkmate.exception.format.EntityNotFoundException;
import checkmate.exception.format.ErrorCode;

public class RefreshTokenNotFoundException extends EntityNotFoundException {

    public RefreshTokenNotFoundException() {
        super(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
    }
}
