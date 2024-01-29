package main.model;

import lombok.Data;

@Data
public class TokenRefreshRequest {
    Long userId = 0L;
    String refreshToken = "";
    String accessToken = "";
}
