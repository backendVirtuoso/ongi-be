package com.ongi.infra.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final String KEY_PREFIX = "refresh:";
    private static final Duration TTL = Duration.ofDays(7);

    private final StringRedisTemplate stringRedisTemplate;

    public void save(Long subscriberId, String refreshToken) {
        stringRedisTemplate.opsForValue().set(KEY_PREFIX + subscriberId, refreshToken, TTL);
    }

    public boolean validate(Long subscriberId, String refreshToken) {
        String stored = stringRedisTemplate.opsForValue().get(KEY_PREFIX + subscriberId);
        return refreshToken != null && refreshToken.equals(stored);
    }

    public void delete(Long subscriberId) {
        stringRedisTemplate.delete(KEY_PREFIX + subscriberId);
    }
}
