package com.ongi.domain.quote.entity;

import com.ongi.domain.subscriber.entity.Subscriber;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_saved_quote")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuoteSave {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "saved_id")
    private Long savedId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscriber_id", nullable = false)
    private Subscriber subscriber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quote_id", nullable = false)
    private Quote quote;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public static QuoteSave create(Subscriber subscriber, Quote quote) {
        QuoteSave save = new QuoteSave();
        save.subscriber = subscriber;
        save.quote = quote;
        return save;
    }
}
