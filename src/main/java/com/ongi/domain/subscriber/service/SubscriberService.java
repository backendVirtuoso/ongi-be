package com.ongi.domain.subscriber.service;

import com.ongi.domain.subscriber.dto.SubscribeRequest;
import com.ongi.domain.subscriber.dto.SubscribeResponse;
import com.ongi.domain.subscriber.entity.Subscriber;
import com.ongi.domain.subscriber.repository.SubscriberRepository;
import com.ongi.global.exception.OngiException;
import com.ongi.global.util.TokenGenerator;
import com.ongi.infra.mail.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SubscriberService {

    private final SubscriberRepository subscriberRepository;
    private final MailService mailService;

    public SubscribeResponse subscribe(SubscribeRequest request) {
        if (subscriberRepository.existsByEmail(request.email())) {
            throw OngiException.conflict("이미 구독 중인 이메일입니다.");
        }

        String token = TokenGenerator.generate();
        String preferredCatsJson = toJsonArray(request.preferredCategories());

        Subscriber subscriber = Subscriber.create(request.email(), request.name(), token);
        subscriber.updatePreferredCats(preferredCatsJson);
        Subscriber saved = subscriberRepository.save(subscriber);

        mailService.sendVerificationEmail(request.email(), request.name(), token);

        return SubscribeResponse.from(saved, "인증 이메일을 발송했습니다. 이메일을 확인해주세요.");
    }

    public SubscribeResponse verifyEmail(String token) {
        Subscriber subscriber = subscriberRepository.findByVerifyToken(token)
                .orElseThrow(() -> OngiException.notFound("유효하지 않은 인증 토큰입니다."));

        subscriber.verify();
        return SubscribeResponse.from(subscriber, "이메일 인증이 완료되었습니다. 온기를 구독해주셔서 감사합니다!");
    }

    public void unsubscribe(String email) {
        Subscriber subscriber = subscriberRepository.findByEmail(email)
                .orElseThrow(() -> OngiException.notFound("구독자를 찾을 수 없습니다."));

        subscriber.unsubscribe();
        log.info("Subscriber unsubscribed: {}", email);
    }

    private String toJsonArray(List<String> list) {
        if (list == null || list.isEmpty()) return null;
        return "[" + list.stream()
                .map(s -> "\"" + s + "\"")
                .collect(Collectors.joining(",")) + "]";
    }
}
