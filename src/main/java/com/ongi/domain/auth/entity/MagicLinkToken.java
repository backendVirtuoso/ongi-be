package com.ongi.domain.auth.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_magic_link_token")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MagicLinkToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "token_id")
    private Long tokenId;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(nullable = false, unique = true, length = 255)
    private String token;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public static MagicLinkToken create(String email, String token, int expirationMinutes) {
        MagicLinkToken mlt = new MagicLinkToken();
        mlt.email = email;
        mlt.token = token;
        mlt.expiresAt = LocalDateTime.now().plusMinutes(expirationMinutes);
        return mlt;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }

    public boolean isUsed() {
        return this.usedAt != null;
    }

    public void markUsed() {
        this.usedAt = LocalDateTime.now();
    }
}
