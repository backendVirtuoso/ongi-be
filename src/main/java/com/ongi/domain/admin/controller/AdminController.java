package com.ongi.domain.admin.controller;

import com.ongi.domain.admin.dto.AdminSendHistoryResponse;
import com.ongi.domain.admin.dto.AdminStatsResponse;
import com.ongi.domain.admin.dto.AdminSubscriberResponse;
import com.ongi.domain.admin.service.AdminService;
import com.ongi.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<AdminStatsResponse>> getStats() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getStats()));
    }

    @GetMapping("/subscribers")
    public ResponseEntity<ApiResponse<Page<AdminSubscriberResponse>>> getSubscribers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                adminService.getSubscribers(PageRequest.of(page, size))));
    }

    @GetMapping("/send-history")
    public ResponseEntity<ApiResponse<Page<AdminSendHistoryResponse>>> getSendHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                adminService.getSendHistory(PageRequest.of(page, size))));
    }
}
