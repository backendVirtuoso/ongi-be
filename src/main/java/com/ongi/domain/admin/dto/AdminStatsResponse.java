package com.ongi.domain.admin.dto;

import java.util.List;
import java.util.Map;

public record AdminStatsResponse(
        long totalSubscribers,
        long activeSubscribers,
        long pendingVerification,
        long unsubscribed,
        List<DailyCount> dailyGrowth,
        SendStats sendStats
) {
    public record DailyCount(String date, long count) {}

    public record SendStats(
            Map<String, Long> last7Days,
            Map<String, Map<String, Long>> byType
    ) {}
}
