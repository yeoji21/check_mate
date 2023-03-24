package checkmate.exception.advice;

import checkmate.exception.*;
import checkmate.exception.code.ErrorCode;
import com.amazonaws.services.ecr.model.ImageNotFoundException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.impl.SizeLimitExceededException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionAdvice {
    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e, HttpServletRequest request) {
        return createErrorResponse(e, request, e.getErrorCode());
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> methodArgumentValidation(MethodArgumentNotValidException e, HttpServletRequest request) {
        return createErrorResponse(e, request, ErrorCode.INVALID_REQUEST_PARAMETER);
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> constraintViolationValidation(ConstraintViolationException e, HttpServletRequest request) {
        return createErrorResponse(e, request, ErrorCode.INVALID_REQUEST_PARAMETER);
    }

    @ExceptionHandler({MaxUploadSizeExceededException.class, SizeLimitExceededException.class, MissingServletRequestPartException.class, MultipartException.class})
    protected ResponseEntity<ErrorResponse> imageFileSize(Exception e, HttpServletRequest request) {
        return createErrorResponse(e, request, ErrorCode.FILE_SIZE);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    protected ResponseEntity<ErrorResponse> invalidHttpMessageParsing(HttpMessageNotReadableException e, HttpServletRequest request) {
        return createErrorResponse(e, request, ErrorCode.INVALID_JSON_TYPE);
    }

    @ExceptionHandler(JsonConvertingException.class)
    protected ResponseEntity<ErrorResponse> invalidJsonParsing(JsonConvertingException e, HttpServletRequest request) {
        return createErrorResponse(e, request, ErrorCode.INVALID_JSON_TYPE);
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> s3ImageNotFound(ImageNotFoundException e, HttpServletRequest request) {
        return createErrorResponse(e, request, ErrorCode.IMAGE_NOT_FOUND);
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> s3ImageSave(RuntimeIOException e, HttpServletRequest request) {
        return createErrorResponse(e, request, e.getErrorCode());
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> responseStatus(ResponseStatusException e, HttpServletRequest request) {
        return createErrorResponse(e, request, ErrorCode.INVALID_REQUEST_PARAMETER);
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> illegalArgumentException(IllegalArgumentException e, HttpServletRequest request) {
        return createErrorResponse(e, request, ErrorCode.INVALID_REQUEST_PARAMETER);
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> refreshTokenExpired(TokenExpiredException e, HttpServletRequest request) {
        return createErrorResponse(e, request, ErrorCode.REFRESH_TOKEN_EXPIRED);
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> authorityException(UnauthorizedException e, HttpServletRequest request) {
        return createErrorResponse(e, request, e.getErrorCode());
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> dataIntegrityViolationException(DataIntegrityViolationException e, HttpServletRequest request) {
        return createErrorResponse(e, request, ErrorCode.DATA_INTEGRITY_VIOLATE);
    }

    private ResponseEntity<ErrorResponse> createErrorResponse(Exception e, HttpServletRequest request, ErrorCode errorCode) {
        ResponseEntity<ErrorResponse> response = ErrorResponse.toResponseEntity(errorCode);
        ExceptionRequestLogger.logging(response, e, request);
        return response;
    }
}
