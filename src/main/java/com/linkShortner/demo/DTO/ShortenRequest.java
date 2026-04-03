package com.linkShortner.demo.DTO;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ShortenRequest {
    private  String originalUrl;
    private LocalDateTime expiresAt;
    private String customCode;
}
