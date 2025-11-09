package com.igriss.ListIn.chat.config;

import com.igriss.ListIn.security.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class JwtHandshakeInterceptorTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private WebSocketHandler handler;

    @Mock
    private ServerHttpResponse response;

    @Mock
    private HttpServletRequest httpServletRequest;

    private JwtHandshakeInterceptor interceptor;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        interceptor = new JwtHandshakeInterceptor(jwtService);
    }

    @Test
    void beforeHandshake_validToken_shouldReturnTrueAndStoreUsername() throws Exception {
        ServletServerHttpRequest serverRequest =
                new ServletServerHttpRequest(httpServletRequest);

        when(httpServletRequest.getParameter("token")).thenReturn("validToken");
        when(jwtService.validateToken("validToken")).thenReturn(true);
        when(jwtService.extractUsername("validToken")).thenReturn("john");

        Map<String, Object> attributes = new HashMap<>();

        boolean result = interceptor.beforeHandshake(
                serverRequest, response, handler, attributes);

        assertThat(result).isTrue();
        assertThat(attributes.get("username")).isEqualTo("john");

        verify(jwtService).validateToken("validToken");
        verify(jwtService).extractUsername("validToken");
        verifyNoMoreInteractions(response);
    }

    @Test
    void beforeHandshake_invalidToken_shouldReturnFalseAndSetForbidden() throws Exception {

        ServletServerHttpRequest serverRequest =
                new ServletServerHttpRequest(httpServletRequest);

        when(httpServletRequest.getParameter("token")).thenReturn("invalid");
        when(jwtService.validateToken("invalid")).thenReturn(false);

        Map<String, Object> attributes = new HashMap<>();

        boolean result = interceptor.beforeHandshake(
                serverRequest, response, handler, attributes);

        assertThat(result).isFalse();
        verify(response).setStatusCode(HttpStatus.FORBIDDEN);
        assertThat(attributes).isEmpty();
    }

    @Test
    void beforeHandshake_missingToken_shouldReturnFalseAndSetForbidden() throws Exception {

        ServletServerHttpRequest serverRequest =
                new ServletServerHttpRequest(httpServletRequest);

        when(httpServletRequest.getParameter("token")).thenReturn(null);

        Map<String, Object> attributes = new HashMap<>();

        boolean result = interceptor.beforeHandshake(
                serverRequest, response, handler, attributes);

        assertThat(result).isFalse();
        verify(response).setStatusCode(HttpStatus.FORBIDDEN);
    }

    @Test
    void beforeHandshake_nonServletRequest_shouldReturnFalseAndSetForbidden() throws Exception {

        ServerHttpRequest nonServletRequest = mock(ServerHttpRequest.class);

        Map<String, Object> attributes = new HashMap<>();

        boolean result = interceptor.beforeHandshake(
                nonServletRequest, response, handler, attributes);

        assertThat(result).isFalse();
        verify(response).setStatusCode(HttpStatus.FORBIDDEN);
        assertThat(attributes).isEmpty();
    }

    @Test
    void afterHandshake_shouldDoNothing() {

        ServerHttpRequest request = mock(ServerHttpRequest.class);

        interceptor.afterHandshake(request, response, handler, null);

        verifyNoInteractions(response);
        verifyNoInteractions(handler);
    }
}
