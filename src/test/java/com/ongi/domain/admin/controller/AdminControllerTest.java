package com.ongi.domain.admin.controller;

import com.ongi.domain.admin.dto.AdminSendHistoryResponse;
import com.ongi.domain.admin.dto.AdminStatsResponse;
import com.ongi.domain.admin.dto.AdminSubscriberResponse;
import com.ongi.domain.admin.service.AdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AdminService adminService;

    @InjectMocks
    private AdminController adminController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(adminController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    @DisplayName("GET /api/v1/admin/stats - 200 OK와 통계 데이터를 반환한다")
    void getStats_Returns200WithStats() throws Exception {
        AdminStatsResponse stats = new AdminStatsResponse(
                100L, 70L, 20L, 10L,
                List.of(new AdminStatsResponse.DailyCount("2026-03-20", 5L)),
                new AdminStatsResponse.SendStats(Map.of("SUCCESS", 50L), Map.of())
        );
        given(adminService.getStats()).willReturn(stats);

        mockMvc.perform(get("/api/v1/admin/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalSubscribers").value(100))
                .andExpect(jsonPath("$.data.activeSubscribers").value(70))
                .andExpect(jsonPath("$.data.dailyGrowth[0].count").value(5));
    }

    @Test
    @DisplayName("GET /api/v1/admin/subscribers - adminService.getSubscribers()를 호출하고 200 OK를 반환한다")
    void getSubscribers_Returns200AndCallsService() throws Exception {
        AdminSubscriberResponse subscriber = new AdminSubscriberResponse(
                1L, "test@example.com", "홍길동", "ACTIVE", LocalDateTime.now(), null
        );
        Page<AdminSubscriberResponse> page = new PageImpl<>(List.of(subscriber), PageRequest.of(0, 20), 1);
        given(adminService.getSubscribers(any())).willReturn(page);

        mockMvc.perform(get("/api/v1/admin/subscribers")
                        .param("page", "0").param("size", "20"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/admin/send-history - adminService.getSendHistory()를 호출하고 200 OK를 반환한다")
    void getSendHistory_Returns200AndCallsService() throws Exception {
        AdminSendHistoryResponse history = new AdminSendHistoryResponse(
                1L, "test@example.com", "좋은 하루 되세요", "morning", "SUCCESS",
                LocalDateTime.now(), null
        );
        Page<AdminSendHistoryResponse> page = new PageImpl<>(List.of(history), PageRequest.of(0, 50), 1);
        given(adminService.getSendHistory(any())).willReturn(page);

        mockMvc.perform(get("/api/v1/admin/send-history"))
                .andExpect(status().isOk());
    }
}
