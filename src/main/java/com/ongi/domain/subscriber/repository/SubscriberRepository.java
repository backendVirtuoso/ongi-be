package com.ongi.domain.subscriber.repository;

import com.ongi.domain.subscriber.entity.Subscriber;
import com.ongi.domain.subscriber.entity.SubscriberStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SubscriberRepository extends JpaRepository<Subscriber, Long> {

    Optional<Subscriber> findByEmail(String email);

    Optional<Subscriber> findByVerifyToken(String verifyToken);

    List<Subscriber> findByStatus(SubscriberStatus status);

    boolean existsByEmail(String email);

    long countByStatus(SubscriberStatus status);

    @Query("SELECT CAST(s.subscribedAt AS date) as date, COUNT(s) as cnt " +
           "FROM Subscriber s WHERE s.subscribedAt >= :since " +
           "GROUP BY CAST(s.subscribedAt AS date) ORDER BY CAST(s.subscribedAt AS date) ASC")
    List<Object[]> countDailySubscriptions(@Param("since") LocalDateTime since);

    Page<Subscriber> findAllByOrderBySubscribedAtDesc(Pageable pageable);
}
