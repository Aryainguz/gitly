package com.inguzdev.gitly.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private ResponseStatus status;

    private String message;

    private T data;

    private ApiError error;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    private String path;

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .status(ResponseStatus.SUCCESS)
                .message("Request processed successfully")
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .status(ResponseStatus.SUCCESS)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
                .status(ResponseStatus.SUCCESS)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> error(String message, ApiError error) {
        return ApiResponse.<T>builder()
                .status(ResponseStatus.ERROR)
                .message(message)
                .error(error)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .status(ResponseStatus.ERROR)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> failure(String message) {
        return ApiResponse.<T>builder()
                .status(ResponseStatus.FAILURE)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> failure(String message, T data) {
        return ApiResponse.<T>builder()
                .status(ResponseStatus.FAILURE)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public ApiResponse<T> withPath(String path) {
        this.path = path;
        return this;
    }
}
