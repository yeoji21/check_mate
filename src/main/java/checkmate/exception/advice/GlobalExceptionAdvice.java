package checkmate.exception.advice;

import checkmate.exception.BusinessException;
import checkmate.exception.ErrorCode;
import checkmate.exception.ErrorResponse;
import checkmate.exception.RuntimeIOException;
import com.amazonaws.services.ecr.model.ImageNotFoundException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.impl.SizeLimitExceededException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.ConstraintViolationException;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionAdvice {
    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        log.warn("[handleBusinessException] : {}", e.getErrorCode());
        return ErrorResponse.toResponseEntity(e.getErrorCode());
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> methodArgumentValidation(MethodArgumentNotValidException e) {
        log.warn("[exception - {}] -> {}", ErrorCode.INVALID_REQUEST_PARAMETER, e.getFieldErrors().stream()
                .map(err-> err.getDefaultMessage()).collect(Collectors.joining(" and ")));
        return ErrorResponse.toResponseEntity(ErrorCode.INVALID_REQUEST_PARAMETER);
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> constraintViolationValidation(ConstraintViolationException e) {
        log.warn("[constraintViolationException] : {}", e.getMessage());
        return ErrorResponse.toResponseEntity(ErrorCode.INVALID_REQUEST_PARAMETER);
    }

    @ExceptionHandler({MaxUploadSizeExceededException.class, SizeLimitExceededException.class, MissingServletRequestPartException.class, MultipartException.class})
    protected ResponseEntity<ErrorResponse> imageFileSize(Exception e) {
        log.warn("[handleMultipartException] : {}", e.getMessage());
        return ErrorResponse.toResponseEntity(ErrorCode.FILE_SIZE);
    }

    @ExceptionHandler({JsonProcessingException.class, HttpMessageNotReadableException.class})
    protected ResponseEntity<ErrorResponse> invalidJsonParsing(Exception e) {
        log.warn("[handleHttpMessageNotReadableException] : {}", e.getMessage());
        return ErrorResponse.toResponseEntity(ErrorCode.INVALID_JSON_TYPE);
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> s3ImageNotFound(ImageNotFoundException e) {
        log.warn("[handleImageSaveIOException] : {}", e.getMessage());
        return ErrorResponse.toResponseEntity(ErrorCode.IMAGE_NOT_FOUND);
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> s3ImageSave(RuntimeIOException e) {
        log.warn("[handleRuntimeIOException] : {}", e.getMessage());
        return ErrorResponse.toResponseEntity(e.getErrorCode());
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> responseStatus(ResponseStatusException e) {
        log.warn("[handleResponseStatusException] : {}", e.getMessage());
        return ErrorResponse.toResponseEntity(ErrorCode.INVALID_REQUEST_PARAMETER);
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> illegalArgumentException(IllegalArgumentException e) {
        log.warn("[handleIllegalArgumentException] : {}", e.getMessage());
        return ErrorResponse.toResponseEntity(ErrorCode.INVALID_REQUEST_PARAMETER);
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> refreshTokenExpired(TokenExpiredException e) {
        log.warn("[handleRefreshTokenExpiredException] : {}", e.getMessage());
        return ErrorResponse.toResponseEntity(ErrorCode.REFRESH_TOKEN_EXPIRED);
    }
}
