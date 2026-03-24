package com.ongi.domain.admin.service;

import com.ongi.domain.admin.dto.AdminSendHistoryResponse;
import com.ongi.domain.admin.dto.AdminStatsResponse;
import com.ongi.domain.admin.dto.AdminSubscriberResponse;
import com.ongi.domain.subscriber.entity.SubscriberStatus;
import com.ongi.domain.subscriber.repository.SendHistoryRepository;
import com.ongi.domain.subscriber.repository.SubscriberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final SubscriberRepository subscriberRepository;
    private final SendHistoryRepository sendHistoryRepository;

    public AdminStatsResponse getStats() {
        long total = subscriberRepository.count();
        long active = subscriberRepository.countByStatus(SubscriberStatus.ACTIVE);
        long pending = subscriberRepository.countByStatus(SubscriberStatus.PENDING_VERIFICATION);
        long unsubscribed = subscriberRepository.countByStatus(SubscriberStatus.UNSUBSCRIBED);

        LocalDateTime since14Days = LocalDateTime.now().minusDays(14);
        List<AdminStatsResponse.DailyCount> dailyGrowth = subscriberRepository
                .countDailySubscriptions(since14Days)
                .stream()
                .map(row -> new AdminStatsResponse.DailyCount(row[0].toString(), ((Number) row[1]).longValue()))
                .toList();

        LocalDateTime since7Days = LocalDateTime.now().minusDays(7);

        Map<String, Long> last7Days = new HashMap<>();
        for (Object[] row : sendHistoryRepository.countBySendStatusSince(since7Days)) {
            last7Days.put((String) row[0], ((Number) row[1]).longValue());
        }

        Map<String, Map<String, Long>> byType = new HashMap<>();
        for (Object[] row : sendHistoryRepository.countBySendTypeAndStatusSince(since7Days)) {
            String type = (String) row[0];
            String status = (String) row[1];
            long count = ((Number) row[2]).longValue();
            byType.computeIfAbsent(type, k -> new HashMap<>()).put(status, count);
        }

        return new AdminStatsResponse(total, active, pending, unsubscribed, dailyGrowth,
                new AdminStatsResponse.SendStats(last7Days, byType));
    }

    public Page<AdminSubscriberResponse> getSubscribers(Pageable pageable) {
        return subscriberRepository.findAllByOrderBySubscribedAtDesc(pageable)
                .map(AdminSubscriberResponse::from);
    }

    public Page<AdminSendHistoryResponse> getSendHistory(Pageable pageable) {
        return sendHistoryRepository.findAllByOrderBySentAtDesc(pageable)
                .map(AdminSendHistoryResponse::from);
    }
}
