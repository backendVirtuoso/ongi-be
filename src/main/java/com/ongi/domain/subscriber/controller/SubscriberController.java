package com.ongi.domain.subscriber.controller;

import com.ongi.domain.subscriber.dto.SubscribeRequest;
import com.ongi.domain.subscriber.dto.SubscribeResponse;
import com.ongi.domain.subscriber.service.SubscriberService;
import com.ongi.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/subscribers")
@RequiredArgsConstructor
public class SubscriberController {

    private final SubscriberService subscriberService;

    @PostMapping
    public ResponseEntity<ApiResponse<SubscribeResponse>> subscribe(
            @Valid @RequestBody SubscribeRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(subscriberService.subscribe(request)));
    }

    @GetMapping("/verify")
    public ResponseEntity<ApiResponse<SubscribeResponse>> verify(@RequestParam String token) {
        return ResponseEntity.ok(ApiResponse.ok(subscriberService.verifyEmail(token)));
    }

    @DeleteMapping("/{email}")
    public ResponseEntity<ApiResponse<Void>> unsubscribe(@PathVariable String email) {
        subscriberService.unsubscribe(email);
        return ResponseEntity.ok(ApiResponse.ok(null, "구독이 해지되었습니다."));
    }
}
