package com.ongi.domain.subscriber.repository;

import com.ongi.domain.subscriber.entity.SendHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SendHistoryRepository extends JpaRepository<SendHistory, Long> {

    @Query("SELECT sh.sendStatus, COUNT(sh) FROM SendHistory sh WHERE sh.sentAt >= :since GROUP BY sh.sendStatus")
    List<Object[]> countBySendStatusSince(@Param("since") LocalDateTime since);

    @Query("SELECT sh.sendType, sh.sendStatus, COUNT(sh) FROM SendHistory sh WHERE sh.sentAt >= :since GROUP BY sh.sendType, sh.sendStatus")
    List<Object[]> countBySendTypeAndStatusSince(@Param("since") LocalDateTime since);

    Page<SendHistory> findAllByOrderBySentAtDesc(Pageable pageable);
}
