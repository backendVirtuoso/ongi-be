package com.ongi.domain.admin.controller;

import com.ongi.domain.admin.dto.AdminSendHistoryResponse;
import com.ongi.domain.admin.dto.AdminStatsResponse;
import com.ongi.domain.admin.dto.AdminSubscriberResponse;
import com.ongi.domain.admin.service.AdminService;
import com.ongi.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin", description = "관리자 전용 (JWT + X-Admin-Key 헤더 필요)")
@SecurityRequirement(name = "BearerAuth")
@SecurityRequirement(name = "AdminKey")
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @Operation(summary = "관리자 통계 조회", description = "구독자 수, 발송 수 등 전체 통계를 반환합니다.")
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<AdminStatsResponse>> getStats() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getStats()));
    }

    @Operation(summary = "구독자 목록 조회", description = "전체 구독자 목록을 페이지 단위로 반환합니다.")
    @GetMapping("/subscribers")
    public ResponseEntity<ApiResponse<Page<AdminSubscriberResponse>>> getSubscribers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                adminService.getSubscribers(PageRequest.of(page, size))));
    }

    @Operation(summary = "이메일 발송 이력 조회", description = "뉴스레터 이메일 발송 이력을 페이지 단위로 반환합니다.")
    @GetMapping("/send-history")
    public ResponseEntity<ApiResponse<Page<AdminSendHistoryResponse>>> getSendHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                adminService.getSendHistory(PageRequest.of(page, size))));
    }
}
