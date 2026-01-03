package com.inguzdev.gitly.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Custom business exception with Builder pattern
 */
@Getter
public class BusinessException extends RuntimeException {

    private final String errorCode;
    private final HttpStatus httpStatus;

    private BusinessException(Builder builder) {
        super(builder.message, builder.cause);
        this.errorCode = builder.errorCode != null ? builder.errorCode : "BUSINESS_ERROR";
        this.httpStatus = builder.httpStatus != null ? builder.httpStatus : HttpStatus.BAD_REQUEST;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String message;
        private String errorCode = "BUSINESS_ERROR";
        private HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
        private Throwable cause;

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder errorCode(String errorCode) {
            this.errorCode = errorCode;
            return this;
        }

        public Builder httpStatus(HttpStatus httpStatus) {
            this.httpStatus = httpStatus;
            return this;
        }

        public Builder cause(Throwable cause) {
            this.cause = cause;
            return this;
        }

        public BusinessException build() {
            if (message == null || message.trim().isEmpty()) {
                throw new IllegalArgumentException("Message is required");
            }
            return new BusinessException(this);
        }
    }
}
