package com.ongi.domain.auth.controller;

import com.ongi.domain.auth.dto.MagicLinkRequest;
import com.ongi.domain.auth.dto.TokenRefreshResponse;
import com.ongi.domain.auth.dto.TokenResponse;
import com.ongi.domain.auth.service.AuthService;
import com.ongi.domain.auth.service.MagicLinkService;
import com.ongi.global.response.ApiResponse;
import com.ongi.infra.jwt.CookieUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth", description = "매직 링크 인증")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final MagicLinkService magicLinkService;
    private final AuthService authService;
    private final CookieUtils cookieUtils;

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
            @RequestParam String token,
            HttpServletResponse response
    ) {
        MagicLinkService.VerifyResult result = magicLinkService.verifyMagicLink(token);
        response.addHeader(HttpHeaders.SET_COOKIE, cookieUtils.createRefreshCookie(result.refreshToken()).toString());
        return ResponseEntity.ok(ApiResponse.ok(
                new TokenResponse(result.accessToken(), result.subscriberId(), result.email())
        ));
    }

    @Operation(summary = "액세스 토큰 갱신", description = "Refresh Token 쿠키로 새 액세스 토큰을 발급합니다.")
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenRefreshResponse>> refresh(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String refreshToken = cookieUtils.resolveRefreshToken(request);
        AuthService.RefreshResult result = authService.refresh(refreshToken);
        response.addHeader(HttpHeaders.SET_COOKIE, cookieUtils.createRefreshCookie(result.refreshToken()).toString());
        return ResponseEntity.ok(ApiResponse.ok(new TokenRefreshResponse(result.accessToken())));
    }

    @Operation(summary = "로그아웃", description = "Refresh Token을 무효화하고 쿠키를 삭제합니다.")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String refreshToken = cookieUtils.resolveRefreshToken(request);
        authService.logout(refreshToken);
        response.addHeader(HttpHeaders.SET_COOKIE, cookieUtils.expireRefreshCookie().toString());
        return ResponseEntity.ok(ApiResponse.ok(null, "로그아웃되었습니다."));
    }
}
