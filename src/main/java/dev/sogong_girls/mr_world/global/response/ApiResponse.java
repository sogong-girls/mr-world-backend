package dev.sogong_girls.mr_world.global.response;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class ApiResponse<T> {

    private final boolean success;
    private final int code;
    private final String message;
    private final T data;
    private final LocalDateTime timestamp;

    private ApiResponse(boolean success, int code, String message, T data) {
        this.success = success;
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }

    public static <T> ApiResponse<T> success(HttpStatus status, T data) {
        return new ApiResponse<>(true, status.value(), "요청 처리에 성공했습니다", data);
    }

    public static <T> ApiResponse<T> success(HttpStatus status, String message, T data) {
        return new ApiResponse<>(true, status.value(), message, data);
    }

    public static ApiResponse<Void> error(HttpStatus status) {
        return new ApiResponse<>(false, status.value(), "요청 처리에 실패했습니다", null);
    }

    public static ApiResponse<Void> error(HttpStatus status, String message) {
        return new ApiResponse<>(false, status.value(), message, null);
    }
}