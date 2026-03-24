package com.ongi.domain.quote.entity;

import com.ongi.domain.subscriber.entity.Subscriber;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_quote_like")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuoteLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "like_id")
    private Long likeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quote_id", nullable = false)
    private Quote quote;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscriber_id", nullable = false)
    private Subscriber subscriber;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public static QuoteLike create(Quote quote, Subscriber subscriber) {
        QuoteLike like = new QuoteLike();
        like.quote = quote;
        like.subscriber = subscriber;
        return like;
    }
}
