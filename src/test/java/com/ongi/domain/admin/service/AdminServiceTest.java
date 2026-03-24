package com.ongi.domain.admin.service;

import com.ongi.domain.admin.dto.AdminSendHistoryResponse;
import com.ongi.domain.admin.dto.AdminStatsResponse;
import com.ongi.domain.admin.dto.AdminSubscriberResponse;
import com.ongi.domain.subscriber.entity.SendHistory;
import com.ongi.domain.subscriber.entity.Subscriber;
import com.ongi.domain.subscriber.entity.SubscriberStatus;
import com.ongi.domain.subscriber.repository.SendHistoryRepository;
import com.ongi.domain.subscriber.repository.SubscriberRepository;
import com.ongi.domain.quote.entity.Category;
import com.ongi.domain.quote.entity.Quote;
import com.ongi.domain.quote.entity.SourceType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private SubscriberRepository subscriberRepository;

    @Mock
    private SendHistoryRepository sendHistoryRepository;

    @InjectMocks
    private AdminService adminService;

    @Test
    @DisplayName("getStats()는 구독자 수 집계와 발송 통계를 올바르게 반환한다")
    void getStats_ReturnsAggregatedStats() {
        given(subscriberRepository.count()).willReturn(100L);
        given(subscriberRepository.countByStatus(SubscriberStatus.ACTIVE)).willReturn(70L);
        given(subscriberRepository.countByStatus(SubscriberStatus.PENDING_VERIFICATION)).willReturn(20L);
        given(subscriberRepository.countByStatus(SubscriberStatus.UNSUBSCRIBED)).willReturn(10L);

        List<Object[]> dailyData = new ArrayList<>();
        dailyData.add(new Object[]{"2026-03-20", 5L});
        given(subscriberRepository.countDailySubscriptions(any(LocalDateTime.class)))
                .willReturn(dailyData);

        List<Object[]> statusData = new ArrayList<>();
        statusData.add(new Object[]{"SUCCESS", 50L});
        given(sendHistoryRepository.countBySendStatusSince(any(LocalDateTime.class)))
                .willReturn(statusData);

        List<Object[]> typeData = new ArrayList<>();
        typeData.add(new Object[]{"morning", "SUCCESS", 30L});
        given(sendHistoryRepository.countBySendTypeAndStatusSince(any(LocalDateTime.class)))
                .willReturn(typeData);

        AdminStatsResponse stats = adminService.getStats();

        assertThat(stats.totalSubscribers()).isEqualTo(100L);
        assertThat(stats.activeSubscribers()).isEqualTo(70L);
        assertThat(stats.pendingVerification()).isEqualTo(20L);
        assertThat(stats.unsubscribed()).isEqualTo(10L);
        assertThat(stats.dailyGrowth()).hasSize(1);
        assertThat(stats.dailyGrowth().get(0).count()).isEqualTo(5L);
        assertThat(stats.sendStats().last7Days()).containsEntry("SUCCESS", 50L);
        assertThat(stats.sendStats().byType()).containsKey("morning");
        assertThat(stats.sendStats().byType().get("morning")).containsEntry("SUCCESS", 30L);
    }

    @Test
    @DisplayName("getStats() - dailyGrowth가 없는 경우 빈 리스트를 반환한다")
    void getStats_NoDailyData_ReturnsEmptyList() {
        given(subscriberRepository.count()).willReturn(0L);
        given(subscriberRepository.countByStatus(any())).willReturn(0L);
        given(subscriberRepository.countDailySubscriptions(any())).willReturn(List.of());
        given(sendHistoryRepository.countBySendStatusSince(any())).willReturn(List.of());
        given(sendHistoryRepository.countBySendTypeAndStatusSince(any())).willReturn(List.of());

        AdminStatsResponse stats = adminService.getStats();

        assertThat(stats.dailyGrowth()).isEmpty();
        assertThat(stats.sendStats().last7Days()).isEmpty();
        assertThat(stats.sendStats().byType()).isEmpty();
    }

    @Test
    @DisplayName("getSubscribers()는 페이지네이션된 구독자 목록을 AdminSubscriberResponse로 매핑한다")
    void getSubscribers_ReturnsMappedPage() {
        Subscriber subscriber = Subscriber.create("test@example.com", "홍길동", "token");
        ReflectionTestUtils.setField(subscriber, "subscribedAt", LocalDateTime.now());
        Page<Subscriber> page = new PageImpl<>(List.of(subscriber));
        given(subscriberRepository.findAllByOrderBySubscribedAtDesc(any(Pageable.class))).willReturn(page);

        Page<AdminSubscriberResponse> result = adminService.getSubscribers(PageRequest.of(0, 20));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).email()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("getSendHistory()는 페이지네이션된 발송 이력을 AdminSendHistoryResponse로 매핑한다")
    void getSendHistory_ReturnsMappedPage() {
        Subscriber subscriber = Subscriber.create("test@example.com", "홍길동", "token");
        Quote quote = createQuote("좋은 하루 되세요");
        SendHistory history = SendHistory.success(subscriber, quote, "morning");

        Page<SendHistory> page = new PageImpl<>(List.of(history));
        given(sendHistoryRepository.findAllByOrderBySentAtDesc(any(Pageable.class))).willReturn(page);

        Page<AdminSendHistoryResponse> result = adminService.getSendHistory(PageRequest.of(0, 50));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).email()).isEqualTo("test@example.com");
        assertThat(result.getContent().get(0).sendType()).isEqualTo("morning");
        assertThat(result.getContent().get(0).sendStatus()).isEqualTo("SUCCESS");
    }

    private Quote createQuote(String content) {
        return Quote.create(content, Category.COMFORT, SourceType.MANUAL);
    }
}
