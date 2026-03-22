package com.ongi.domain.subscriber.repository;

import com.ongi.domain.subscriber.entity.Subscriber;
import com.ongi.domain.subscriber.entity.SubscriberStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubscriberRepository extends JpaRepository<Subscriber, Long> {

    Optional<Subscriber> findByEmail(String email);

    Optional<Subscriber> findByVerifyToken(String verifyToken);

    List<Subscriber> findByStatus(SubscriberStatus status);

    boolean existsByEmail(String email);
}
