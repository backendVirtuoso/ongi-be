package com.ongi.domain.subscriber.entity;

import com.ongi.domain.quote.entity.Quote;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_send_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SendHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long historyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscriber_id", nullable = false)
    private Subscriber subscriber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quote_id", nullable = false)
    private Quote quote;

    @Column(name = "send_type", nullable = false, length = 10)
    private String sendType;

    @Column(name = "send_status", nullable = false, length = 10)
    private String sendStatus = "SUCCESS";

    @CreationTimestamp
    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    public static SendHistory success(Subscriber subscriber, Quote quote, String sendType) {
        SendHistory history = new SendHistory();
        history.subscriber = subscriber;
        history.quote = quote;
        history.sendType = sendType;
        history.sendStatus = "SUCCESS";
        return history;
    }

    public static SendHistory failed(Subscriber subscriber, Quote quote, String sendType, String errorMessage) {
        SendHistory history = new SendHistory();
        history.subscriber = subscriber;
        history.quote = quote;
        history.sendType = sendType;
        history.sendStatus = "FAILED";
        history.errorMessage = errorMessage;
        return history;
    }
}
