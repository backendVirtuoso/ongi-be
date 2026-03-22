package com.ongi.domain.subscriber.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_subscriber")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Subscriber {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subscriber_id")
    private Long subscriberId;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SubscriberStatus status = SubscriberStatus.PENDING_VERIFICATION;

    @Column(name = "preferred_cats", columnDefinition = "JSON")
    private String preferredCats;

    @Column(name = "verify_token", length = 100)
    private String verifyToken;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @CreationTimestamp
    @Column(name = "subscribed_at", updatable = false)
    private LocalDateTime subscribedAt;

    @Column(name = "unsubscribed_at")
    private LocalDateTime unsubscribedAt;

    public static Subscriber create(String email, String name, String verifyToken) {
        Subscriber subscriber = new Subscriber();
        subscriber.email = email;
        subscriber.name = name;
        subscriber.verifyToken = verifyToken;
        subscriber.status = SubscriberStatus.PENDING_VERIFICATION;
        return subscriber;
    }

    public void verify() {
        this.status = SubscriberStatus.ACTIVE;
        this.verifiedAt = LocalDateTime.now();
        this.verifyToken = null;
    }

    public void unsubscribe() {
        this.status = SubscriberStatus.UNSUBSCRIBED;
        this.unsubscribedAt = LocalDateTime.now();
    }

    public void updatePreferredCats(String preferredCats) {
        this.preferredCats = preferredCats;
    }
}
