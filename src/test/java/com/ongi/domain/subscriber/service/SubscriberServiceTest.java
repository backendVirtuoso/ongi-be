package com.ongi.domain.subscriber.service;

import com.ongi.domain.subscriber.dto.PreferenceUpdateRequest;
import com.ongi.domain.subscriber.dto.SubscribeRequest;
import com.ongi.domain.subscriber.dto.SubscriberMeResponse;
import com.ongi.domain.subscriber.entity.Subscriber;
import com.ongi.domain.subscriber.entity.SubscriberStatus;
import com.ongi.domain.subscriber.repository.SubscriberRepository;
import com.ongi.global.exception.OngiException;
import com.ongi.infra.mail.MailService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriberServiceTest {

    @Mock
    private SubscriberRepository subscriberRepository;

    @Mock
    private MailService mailService;

    @InjectMocks
    private SubscriberService subscriberService;

    @Test
    @DisplayName("신규 이메일로 구독 시 저장 및 인증 메일 발송이 호출된다")
    void subscribe_NewEmail_SavesAndSendsMail() {
        SubscribeRequest request = new SubscribeRequest("new@example.com", "홍길동", List.of("힐링"));
        Subscriber saved = Subscriber.create("new@example.com", "홍길동", "token");
        given(subscriberRepository.existsByEmail("new@example.com")).willReturn(false);
        given(subscriberRepository.save(any())).willReturn(saved);

        var response = subscriberService.subscribe(request);

        verify(subscriberRepository).save(any(Subscriber.class));
        verify(mailService).sendVerificationEmail(eq("new@example.com"), eq("홍길동"), anyString());
        assertThat(response.message()).contains("인증 이메일");
    }

    @Test
    @DisplayName("이미 구독 중인 이메일로 구독 시 409 Conflict 예외가 발생한다")
    void subscribe_DuplicateEmail_ThrowsConflict() {
        SubscribeRequest request = new SubscribeRequest("dup@example.com", "홍길동", List.of());
        given(subscriberRepository.existsByEmail("dup@example.com")).willReturn(true);

        assertThatThrownBy(() -> subscriberService.subscribe(request))
                .isInstanceOf(OngiException.class)
                .satisfies(e -> assertThat(((OngiException) e).getStatus()).isEqualTo(HttpStatus.CONFLICT));

        verify(subscriberRepository, never()).save(any());
    }

    @Test
    @DisplayName("유효한 인증 토큰으로 verifyEmail() 호출 시 구독자가 ACTIVE 상태가 된다")
    void verifyEmail_ValidToken_ActivatesSubscriber() {
        Subscriber subscriber = Subscriber.create("test@example.com", "홍길동", "valid-token");
        given(subscriberRepository.findByVerifyToken("valid-token")).willReturn(Optional.of(subscriber));

        var response = subscriberService.verifyEmail("valid-token");

        assertThat(subscriber.getStatus()).isEqualTo(SubscriberStatus.ACTIVE);
        assertThat(response.message()).contains("인증이 완료");
    }

    @Test
    @DisplayName("유효하지 않은 토큰으로 verifyEmail() 호출 시 404 Not Found 예외가 발생한다")
    void verifyEmail_InvalidToken_ThrowsNotFound() {
        given(subscriberRepository.findByVerifyToken("invalid-token")).willReturn(Optional.empty());

        assertThatThrownBy(() -> subscriberService.verifyEmail("invalid-token"))
                .isInstanceOf(OngiException.class)
                .satisfies(e -> assertThat(((OngiException) e).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    @DisplayName("존재하는 이메일로 unsubscribe() 호출 시 UNSUBSCRIBED 상태로 전환된다")
    void unsubscribe_ExistingEmail_UnsubscribesSubscriber() {
        Subscriber subscriber = Subscriber.create("test@example.com", "홍길동", "token");
        subscriber.verify();
        given(subscriberRepository.findByEmail("test@example.com")).willReturn(Optional.of(subscriber));

        subscriberService.unsubscribe("test@example.com");

        assertThat(subscriber.getStatus()).isEqualTo(SubscriberStatus.UNSUBSCRIBED);
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 unsubscribe() 호출 시 404 Not Found 예외가 발생한다")
    void unsubscribe_UnknownEmail_ThrowsNotFound() {
        given(subscriberRepository.findByEmail("ghost@example.com")).willReturn(Optional.empty());

        assertThatThrownBy(() -> subscriberService.unsubscribe("ghost@example.com"))
                .isInstanceOf(OngiException.class)
                .satisfies(e -> assertThat(((OngiException) e).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    @DisplayName("getMe()는 존재하는 구독자 ID로 SubscriberMeResponse를 반환한다")
    void getMe_ExistingId_ReturnsResponse() {
        Subscriber subscriber = Subscriber.create("test@example.com", "홍길동", "token");
        given(subscriberRepository.findById(1L)).willReturn(Optional.of(subscriber));

        SubscriberMeResponse response = subscriberService.getMe(1L);

        assertThat(response.email()).isEqualTo("test@example.com");
        assertThat(response.name()).isEqualTo("홍길동");
    }

    @Test
    @DisplayName("getMe()는 존재하지 않는 ID로 호출 시 404 Not Found 예외가 발생한다")
    void getMe_UnknownId_ThrowsNotFound() {
        given(subscriberRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> subscriberService.getMe(999L))
                .isInstanceOf(OngiException.class)
                .satisfies(e -> assertThat(((OngiException) e).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    @DisplayName("updatePreferences()는 카테고리 목록을 JSON 배열 형태로 저장한다")
    void updatePreferences_SavesCategoriesAsJson() {
        Subscriber subscriber = Subscriber.create("test@example.com", "홍길동", "token");
        given(subscriberRepository.findById(1L)).willReturn(Optional.of(subscriber));

        PreferenceUpdateRequest request = new PreferenceUpdateRequest(List.of("힐링", "동기부여"));
        subscriberService.updatePreferences(1L, request);

        assertThat(subscriber.getPreferredCats()).isEqualTo("[\"힐링\",\"동기부여\"]");
    }
}
