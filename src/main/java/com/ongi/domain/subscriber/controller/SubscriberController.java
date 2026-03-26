package com.ongi.domain.subscriber.controller;

import com.ongi.domain.subscriber.dto.*;
import com.ongi.domain.subscriber.service.SubscriberService;
import com.ongi.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Subscriber", description = "구독자 관리")
@RestController
@RequestMapping("/api/v1/subscribers")
@RequiredArgsConstructor
public class SubscriberController {

    private final SubscriberService subscriberService;

    @Operation(summary = "이메일 구독 신청", description = "이메일과 이름을 입력하여 뉴스레터를 구독합니다. 인증 메일이 발송됩니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<SubscribeResponse>> subscribe(
            @Valid @RequestBody SubscribeRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(subscriberService.subscribe(request)));
    }

    @Operation(summary = "이메일 인증", description = "구독 신청 후 이메일로 받은 토큰으로 이메일을 인증합니다.")
    @GetMapping("/verify")
    public ResponseEntity<ApiResponse<SubscribeResponse>> verify(@RequestParam String token) {
        return ResponseEntity.ok(ApiResponse.ok(subscriberService.verifyEmail(token)));
    }

    @Operation(summary = "구독 해지", description = "이메일 주소로 뉴스레터 구독을 해지합니다.")
    @DeleteMapping("/{email}")
    public ResponseEntity<ApiResponse<Void>> unsubscribe(@PathVariable String email) {
        subscriberService.unsubscribe(email);
        return ResponseEntity.ok(ApiResponse.ok(null, "구독이 해지되었습니다."));
    }

    @Operation(summary = "내 정보 조회", description = "로그인한 구독자의 프로필 정보를 반환합니다.")
    @SecurityRequirement(name = "BearerAuth")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<SubscriberMeResponse>> getMe(Authentication authentication) {
        Long subscriberId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(subscriberService.getMe(subscriberId)));
    }

    @Operation(summary = "수신 설정 변경", description = "이메일 수신 카테고리 등 개인 설정을 변경합니다.")
    @SecurityRequirement(name = "BearerAuth")
    @PatchMapping("/me/preferences")
    public ResponseEntity<ApiResponse<SubscriberMeResponse>> updatePreferences(
            @Valid @RequestBody PreferenceUpdateRequest request,
            Authentication authentication
    ) {
        Long subscriberId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(
                subscriberService.updatePreferences(subscriberId, request)));
    }
}
