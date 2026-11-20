package dev.sogong_girls.mr_world.global.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import dev.sogong_girls.mr_world.global.logging.RequestLoggingFilter;
import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({ MethodArgumentNotValidException.class, HttpMessageNotReadableException.class })
    public ResponseEntity<?> handleInvalidInput(Exception e, HttpServletRequest request) {
        RequestLoggingFilter.recordError(request, e);
        BusinessErrorCode errorCode = BusinessErrorCode.INVALID_INPUT;
        return ResponseEntity.status(errorCode.getStatus()).body(errorCode.getMessage());
    }

    /**
     * 매핑되지 않은 URL. Exception 핸들러에 잡혀 500이 되지 않도록 404로 응답한다.
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<?> handleNoResource(NoResourceFoundException e, HttpServletRequest request) {
        RequestLoggingFilter.recordError(request, e);
        BusinessErrorCode errorCode = BusinessErrorCode.NOT_FOUND;
        return ResponseEntity.status(errorCode.getStatus()).body(errorCode.getMessage());
    }

    /**
     * URL은 있지만 HTTP 메서드가 다른 경우. 허용 메서드를 Allow 헤더로 알려 준다.
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<?> handleMethodNotSupported(HttpRequestMethodNotSupportedException e,
            HttpServletRequest request) {
        RequestLoggingFilter.recordError(request, e);
        BusinessErrorCode errorCode = BusinessErrorCode.METHOD_NOT_ALLOWED;
        return ResponseEntity.status(errorCode.getStatus())
                .headers(e.getHeaders())
                .body(errorCode.getMessage());
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<?> handleBusinessException(BusinessException e, HttpServletRequest request) {
        RequestLoggingFilter.recordError(request, e);
        BusinessErrorCode errorCode = e.getErrorCode();
        return ResponseEntity.status(errorCode.getStatus()).body(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception e, HttpServletRequest request) {
        RequestLoggingFilter.recordError(request, e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }
}
