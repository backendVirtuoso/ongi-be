package com.ongi.domain.subscriber.repository;

import com.ongi.domain.subscriber.entity.SendHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SendHistoryRepository extends JpaRepository<SendHistory, Long> {
}
