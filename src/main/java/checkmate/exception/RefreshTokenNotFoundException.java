package checkmate.exception;

import checkmate.exception.format.NotFoundException;
import checkmate.exception.format.ErrorCode;

public class RefreshTokenNotFoundException extends NotFoundException {

    public RefreshTokenNotFoundException() {
        super(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
    }
}
