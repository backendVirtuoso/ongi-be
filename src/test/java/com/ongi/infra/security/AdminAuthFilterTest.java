package com.ongi.infra.security;

import com.ongi.domain.subscriber.entity.Subscriber;
import com.ongi.domain.subscriber.entity.SubscriberRole;
import com.ongi.domain.subscriber.repository.SubscriberRepository;
import com.ongi.infra.jwt.JwtProvider;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminAuthFilterTest {

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private SubscriberRepository subscriberRepository;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private AdminAuthFilter adminAuthFilter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    @DisplayName("admin 경로에 대해 shouldNotFilter()는 false를 반환한다 (필터 적용)")
    void shouldNotFilter_AdminPath_ReturnsFalse() throws Exception {
        request.setRequestURI("/api/v1/admin/stats");

        boolean result = adminAuthFilter.shouldNotFilter(request);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("비admin 경로에 대해 shouldNotFilter()는 true를 반환한다 (필터 스킵)")
    void shouldNotFilter_NonAdminPath_ReturnsTrue() throws Exception {
        request.setRequestURI("/api/v1/subscribers");

        boolean result = adminAuthFilter.shouldNotFilter(request);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Authorization 헤더가 없으면 403과 JSON 에러 메시지를 반환한다")
    void doFilterInternal_NoToken_Returns403() throws Exception {
        // Authorization 헤더 없음

        adminAuthFilter.doFilterInternal(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentAsString()).contains("접근 권한이 없습니다.");
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    @DisplayName("유효하지 않은 토큰이면 403을 반환한다")
    void doFilterInternal_InvalidToken_Returns403() throws Exception {
        request.addHeader("Authorization", "Bearer invalid-token");
        given(jwtProvider.isValid("invalid-token")).willReturn(false);

        adminAuthFilter.doFilterInternal(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(403);
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    @DisplayName("유효한 토큰이지만 구독자가 없으면 403을 반환한다")
    void doFilterInternal_ValidTokenNoSubscriber_Returns403() throws Exception {
        request.addHeader("Authorization", "Bearer valid-token");
        given(jwtProvider.isValid("valid-token")).willReturn(true);
        given(jwtProvider.getSubscriberId("valid-token")).willReturn(99L);
        given(subscriberRepository.findById(99L)).willReturn(Optional.empty());

        adminAuthFilter.doFilterInternal(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(403);
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    @DisplayName("유효한 토큰이지만 USER 역할이면 403을 반환한다")
    void doFilterInternal_ValidTokenUserRole_Returns403() throws Exception {
        request.addHeader("Authorization", "Bearer valid-token");
        Subscriber user = Subscriber.create("user@example.com", "홍길동", "token");
        // 기본 role = USER

        given(jwtProvider.isValid("valid-token")).willReturn(true);
        given(jwtProvider.getSubscriberId("valid-token")).willReturn(1L);
        given(subscriberRepository.findById(1L)).willReturn(Optional.of(user));

        adminAuthFilter.doFilterInternal(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(403);
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    @DisplayName("유효한 토큰 + ADMIN 역할이면 filterChain.doFilter()가 호출된다")
    void doFilterInternal_ValidTokenAdminRole_CallsFilterChain() throws Exception {
        request.addHeader("Authorization", "Bearer admin-token");
        Subscriber admin = Subscriber.create("admin@example.com", "관리자", "token");
        ReflectionTestUtils.setField(admin, "role", SubscriberRole.ADMIN);

        given(jwtProvider.isValid("admin-token")).willReturn(true);
        given(jwtProvider.getSubscriberId("admin-token")).willReturn(1L);
        given(subscriberRepository.findById(1L)).willReturn(Optional.of(admin));

        adminAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(response.getStatus()).isNotEqualTo(403);
    }
}
