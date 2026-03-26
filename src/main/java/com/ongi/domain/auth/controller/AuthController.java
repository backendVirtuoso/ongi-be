package com.ongi.domain.auth.controller;

import com.ongi.domain.auth.dto.MagicLinkRequest;
import com.ongi.domain.auth.dto.TokenResponse;
import com.ongi.domain.auth.service.MagicLinkService;
import com.ongi.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth", description = "매직 링크 인증")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final MagicLinkService magicLinkService;

    @Operation(summary = "매직 링크 발송", description = "입력한 이메일로 로그인 매직 링크를 발송합니다.")
    @PostMapping("/magic-link")
    public ResponseEntity<ApiResponse<Void>> sendMagicLink(
            @Valid @RequestBody MagicLinkRequest request
    ) {
        magicLinkService.sendMagicLink(request);
        return ResponseEntity.ok(ApiResponse.ok(null, "매직 링크를 이메일로 발송했습니다."));
    }

    @Operation(summary = "매직 링크 검증 및 JWT 발급", description = "이메일로 받은 토큰을 검증하고 JWT 액세스 토큰을 발급합니다.")
    @GetMapping("/magic-link/verify")
    public ResponseEntity<ApiResponse<TokenResponse>> verifyMagicLink(
            @RequestParam String token
    ) {
        return ResponseEntity.ok(ApiResponse.ok(magicLinkService.verifyMagicLink(token)));
    }
}
