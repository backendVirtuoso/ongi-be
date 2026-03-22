package com.ongi.batch.processor;

import com.ongi.domain.quote.entity.Quote;
import com.ongi.domain.subscriber.entity.Subscriber;

public record EmailMessage(
        Subscriber subscriber,
        Quote quote,
        String sendType,
        String subject,
        String html
) {}
