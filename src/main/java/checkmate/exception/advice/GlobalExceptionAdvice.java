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
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionAdvice {
    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.warn(request.getMethod() + " - " + request.getRequestURI() + " [handleBusinessException] : {}", e.getErrorCode());
        return ErrorResponse.toResponseEntity(e.getErrorCode());
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> methodArgumentValidation(MethodArgumentNotValidException e, HttpServletRequest request) {
        log.warn(request.getMethod() + " - " + request.getRequestURI()
                + " [exception - {}] -> {}", ErrorCode.INVALID_REQUEST_PARAMETER, e.getFieldErrors().stream()
                .map(err -> err.getDefaultMessage()).collect(Collectors.joining(" and ")));
        return ErrorResponse.toResponseEntity(ErrorCode.INVALID_REQUEST_PARAMETER);
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> constraintViolationValidation(ConstraintViolationException e, HttpServletRequest request) {
        log.warn(request.getMethod() + " - " + request.getRequestURI() + " [constraintViolationException] : {}", e.getMessage());
        return ErrorResponse.toResponseEntity(ErrorCode.INVALID_REQUEST_PARAMETER);
    }

    @ExceptionHandler({MaxUploadSizeExceededException.class, SizeLimitExceededException.class, MissingServletRequestPartException.class, MultipartException.class})
    protected ResponseEntity<ErrorResponse> imageFileSize(Exception e, HttpServletRequest request) {
        log.warn(request.getMethod() + " - " + request.getRequestURI() + " [handleMultipartException] : {}", e.getMessage());
        return ErrorResponse.toResponseEntity(ErrorCode.FILE_SIZE);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    protected ResponseEntity<ErrorResponse> invalidHttpMessageParsing(HttpMessageNotReadableException e, HttpServletRequest request) {
        log.warn(request.getMethod() + " - " + request.getRequestURI() + " [handleHttpMessageNotReadableException] : {}", e.getMessage());
        return ErrorResponse.toResponseEntity(ErrorCode.INVALID_JSON_TYPE);
    }

    @ExceptionHandler(JsonConvertingException.class)
    protected ResponseEntity<ErrorResponse> invalidJsonParsing(JsonConvertingException e, HttpServletRequest request) {
        log.warn(request.getMethod() + " - " + request.getRequestURI() + " [handleJsonConvertingException] : {}", e.getMessage());
        return ErrorResponse.toResponseEntity(ErrorCode.INVALID_JSON_TYPE);
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> s3ImageNotFound(ImageNotFoundException e, HttpServletRequest request) {
        log.warn(request.getMethod() + " - " + request.getRequestURI() + " [handleImageSaveIOException] : {}", e.getMessage());
        return ErrorResponse.toResponseEntity(ErrorCode.IMAGE_NOT_FOUND);
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> s3ImageSave(RuntimeIOException e, HttpServletRequest request) {
        log.warn(request.getMethod() + " - " + request.getRequestURI() + " [handleRuntimeIOException] : {}", e.getMessage());
        return ErrorResponse.toResponseEntity(e.getErrorCode());
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> responseStatus(ResponseStatusException e, HttpServletRequest request) {
        log.warn(request.getMethod() + " - " + request.getRequestURI() + " [handleResponseStatusException] : {}", e.getMessage());
        return ErrorResponse.toResponseEntity(ErrorCode.INVALID_REQUEST_PARAMETER);
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> illegalArgumentException(IllegalArgumentException e, HttpServletRequest request) {
        log.warn(request.getMethod() + " - " + request.getRequestURI() + " [handleIllegalArgumentException] : {}", e.getMessage());
        return ErrorResponse.toResponseEntity(ErrorCode.INVALID_REQUEST_PARAMETER);
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> refreshTokenExpired(TokenExpiredException e, HttpServletRequest request) {
        log.warn(request.getMethod() + " - " + request.getRequestURI() + " [handleRefreshTokenExpiredException] : {}", e.getMessage());
        return ErrorResponse.toResponseEntity(ErrorCode.REFRESH_TOKEN_EXPIRED);
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> authorityException(UnauthorizedException e, HttpServletRequest request) {
        log.warn(request.getMethod() + " - " + request.getRequestURI() + " [handleAuthorityException] : {}", e.getErrorCode().getDetail());
        return ErrorResponse.toResponseEntity(e.getErrorCode());
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> dataIntegrityViolationException(DataIntegrityViolationException e, HttpServletRequest request) {
        log.warn(request.getMethod() + " - " + request.getRequestURI() + " [handleAuthorityException] : {}", e.getMessage());
        return ErrorResponse.toResponseEntity(ErrorCode.DATA_INTEGRITY_VIOLATE);
    }
}
