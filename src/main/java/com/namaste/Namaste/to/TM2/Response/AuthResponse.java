package com.namaste.Namaste.to.TM2.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String tokenType;
    private String username;
    private long expiresInMs;
}
