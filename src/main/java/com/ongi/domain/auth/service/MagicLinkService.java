package com.ongi.domain.auth.service;

import com.ongi.domain.auth.dto.MagicLinkRequest;
import com.ongi.domain.auth.dto.TokenResponse;
import com.ongi.domain.auth.entity.MagicLinkToken;
import com.ongi.domain.auth.repository.MagicLinkTokenRepository;
import com.ongi.domain.subscriber.entity.Subscriber;
import com.ongi.domain.subscriber.entity.SubscriberStatus;
import com.ongi.domain.subscriber.repository.SubscriberRepository;
import com.ongi.global.exception.OngiException;
import com.ongi.global.util.TokenGenerator;
import com.ongi.infra.jwt.JwtProvider;
import com.ongi.infra.mail.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MagicLinkService {

    private static final int EXPIRATION_MINUTES = 15;

    private final MagicLinkTokenRepository magicLinkTokenRepository;
    private final SubscriberRepository subscriberRepository;
    private final JwtProvider jwtProvider;
    private final MailService mailService;

    public void sendMagicLink(MagicLinkRequest request) {
        Subscriber subscriber = subscriberRepository.findByEmail(request.email())
                .orElseThrow(() -> OngiException.notFound("가입된 이메일이 아닙니다."));

        if (subscriber.getStatus() != SubscriberStatus.ACTIVE) {
            throw OngiException.badRequest("이메일 인증이 완료된 구독자만 로그인할 수 있습니다.");
        }

        String token = TokenGenerator.generate();
        MagicLinkToken magicLinkToken = MagicLinkToken.create(request.email(), token, EXPIRATION_MINUTES);
        magicLinkTokenRepository.save(magicLinkToken);

        mailService.sendMagicLinkEmail(request.email(), subscriber.getName(), token);
        log.info("Magic link sent to: {}", request.email());
    }

    public TokenResponse verifyMagicLink(String token) {
        MagicLinkToken magicLinkToken = magicLinkTokenRepository.findByToken(token)
                .orElseThrow(() -> OngiException.notFound("유효하지 않은 링크입니다."));

        if (magicLinkToken.isUsed()) {
            throw OngiException.badRequest("이미 사용된 링크입니다.");
        }
        if (magicLinkToken.isExpired()) {
            throw OngiException.badRequest("만료된 링크입니다. 다시 요청해주세요.");
        }

        magicLinkToken.markUsed();

        Subscriber subscriber = subscriberRepository.findByEmail(magicLinkToken.getEmail())
                .orElseThrow(() -> OngiException.notFound("구독자를 찾을 수 없습니다."));

        String jwt = jwtProvider.generateToken(subscriber.getSubscriberId());
        return new TokenResponse(jwt, subscriber.getSubscriberId(), subscriber.getEmail());
    }
}
