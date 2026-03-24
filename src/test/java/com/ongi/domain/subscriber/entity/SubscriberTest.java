package com.ongi.domain.subscriber.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class SubscriberTest {

    @Test
    @DisplayName("create()로 생성된 구독자는 PENDING_VERIFICATION 상태이고 토큰이 설정된다")
    void create_SetsStatusAndToken() {
        Subscriber subscriber = Subscriber.create("test@example.com", "홍길동", "token-abc");

        assertThat(subscriber.getEmail()).isEqualTo("test@example.com");
        assertThat(subscriber.getName()).isEqualTo("홍길동");
        assertThat(subscriber.getVerifyToken()).isEqualTo("token-abc");
        assertThat(subscriber.getStatus()).isEqualTo(SubscriberStatus.PENDING_VERIFICATION);
    }

    @Test
    @DisplayName("verify() 호출 시 ACTIVE 상태로 전환되고 verifyToken이 null이 된다")
    void verify_ActivatesSubscriberAndClearsToken() {
        Subscriber subscriber = Subscriber.create("test@example.com", "홍길동", "token-abc");

        subscriber.verify();

        assertThat(subscriber.getStatus()).isEqualTo(SubscriberStatus.ACTIVE);
        assertThat(subscriber.getVerifiedAt()).isNotNull();
        assertThat(subscriber.getVerifyToken()).isNull();
    }

    @Test
    @DisplayName("unsubscribe() 호출 시 UNSUBSCRIBED 상태로 전환되고 해지 시각이 설정된다")
    void unsubscribe_SetsStatusAndTimestamp() {
        Subscriber subscriber = Subscriber.create("test@example.com", "홍길동", "token-abc");
        subscriber.verify();

        subscriber.unsubscribe();

        assertThat(subscriber.getStatus()).isEqualTo(SubscriberStatus.UNSUBSCRIBED);
        assertThat(subscriber.getUnsubscribedAt()).isNotNull();
    }

    @Test
    @DisplayName("기본 role이 USER인 경우 isAdmin()은 false를 반환한다")
    void isAdmin_DefaultUserRole_ReturnsFalse() {
        Subscriber subscriber = Subscriber.create("test@example.com", "홍길동", "token");

        assertThat(subscriber.isAdmin()).isFalse();
    }

    @Test
    @DisplayName("role이 ADMIN인 경우 isAdmin()은 true를 반환한다")
    void isAdmin_AdminRole_ReturnsTrue() {
        Subscriber subscriber = Subscriber.create("admin@example.com", "관리자", "token");
        ReflectionTestUtils.setField(subscriber, "role", SubscriberRole.ADMIN);

        assertThat(subscriber.isAdmin()).isTrue();
    }

    @Test
    @DisplayName("updatePreferredCats()로 선호 카테고리가 업데이트된다")
    void updatePreferredCats_UpdatesField() {
        Subscriber subscriber = Subscriber.create("test@example.com", "홍길동", "token");

        subscriber.updatePreferredCats("[\"힐링\",\"동기부여\"]");

        assertThat(subscriber.getPreferredCats()).isEqualTo("[\"힐링\",\"동기부여\"]");
    }
}
