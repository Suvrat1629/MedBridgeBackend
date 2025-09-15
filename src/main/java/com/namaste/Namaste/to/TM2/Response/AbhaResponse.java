package com.namaste.Namaste.to.TM2.Response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AbhaResponse<T> {
    private boolean success;
    private String message;
    private String errorCode;
    private T data;
    private String txnId;

    public static <T> AbhaResponse<T> success(T data) {
        return AbhaResponse.<T>builder()
                .success(true)
                .data(data)
                .build();
    }

    public static <T> AbhaResponse<T> success(T data, String txnId) {
        return AbhaResponse.<T>builder()
                .success(true)
                .data(data)
                .txnId(txnId)
                .build();
    }

    public static <T> AbhaResponse<T> error(String message, String errorCode) {
        return AbhaResponse.<T>builder()
                .success(false)
                .message(message)
                .errorCode(errorCode)
                .build();
    }
}
