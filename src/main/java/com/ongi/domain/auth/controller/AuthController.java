package com.ongi.domain.auth.controller;

import com.ongi.domain.auth.dto.MagicLinkRequest;
import com.ongi.domain.auth.dto.TokenResponse;
import com.ongi.domain.auth.service.MagicLinkService;
import com.ongi.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final MagicLinkService magicLinkService;

    @PostMapping("/magic-link")
    public ResponseEntity<ApiResponse<Void>> sendMagicLink(
            @Valid @RequestBody MagicLinkRequest request
    ) {
        magicLinkService.sendMagicLink(request);
        return ResponseEntity.ok(ApiResponse.ok(null, "매직 링크를 이메일로 발송했습니다."));
    }

    @GetMapping("/magic-link/verify")
    public ResponseEntity<ApiResponse<TokenResponse>> verifyMagicLink(
            @RequestParam String token
    ) {
        return ResponseEntity.ok(ApiResponse.ok(magicLinkService.verifyMagicLink(token)));
    }
}
