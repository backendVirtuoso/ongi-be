package com.ongi.domain.auth.service;

import com.ongi.global.exception.OngiException;
import com.ongi.infra.jwt.JwtProvider;
import com.ongi.infra.jwt.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtProvider jwtProvider;
    private final RefreshTokenService refreshTokenService;

    /**
     * Refresh Token으로 새 Access Token과 새 Refresh Token을 발급합니다 (Token Rotation).
     */
    public RefreshResult refresh(String refreshToken) {
        if (refreshToken == null) {
            throw OngiException.badRequest("리프레시 토큰이 없습니다.");
        }
        if (!jwtProvider.isValid(refreshToken)) {
            throw OngiException.badRequest("유효하지 않은 리프레시 토큰입니다.");
        }

        Long subscriberId = jwtProvider.getSubscriberId(refreshToken);

        if (!refreshTokenService.validate(subscriberId, refreshToken)) {
            throw OngiException.badRequest("유효하지 않은 리프레시 토큰입니다.");
        }

        String newAccessToken = jwtProvider.generateToken(subscriberId);
        String newRefreshToken = jwtProvider.generateRefreshToken(subscriberId);
        refreshTokenService.save(subscriberId, newRefreshToken);

        return new RefreshResult(newAccessToken, newRefreshToken);
    }

    /**
     * 로그아웃 처리: Redis에서 Refresh Token을 삭제합니다.
     */
    public void logout(String refreshToken) {
        if (refreshToken == null || !jwtProvider.isValid(refreshToken)) {
            return; // 멱등성 보장: 이미 만료된 토큰도 정상 처리
        }
        Long subscriberId = jwtProvider.getSubscriberId(refreshToken);
        refreshTokenService.delete(subscriberId);
    }

    public record RefreshResult(String accessToken, String refreshToken) {}
}
