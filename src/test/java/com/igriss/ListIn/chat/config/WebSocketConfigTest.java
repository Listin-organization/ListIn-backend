package com.igriss.ListIn.chat.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.StompWebSocketEndpointRegistration;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WebSocketConfigTest {

    private final JwtHandshakeInterceptor interceptor = mock(JwtHandshakeInterceptor.class);
    private final WebSocketConfig config = new WebSocketConfig(interceptor);

    @Test
    void configureMessageBroker_shouldConfigureBroker() {
        MessageBrokerRegistry registry = mock(MessageBrokerRegistry.class);

        config.configureMessageBroker(registry);

        verify(registry).enableSimpleBroker("/user");
        verify(registry).setApplicationDestinationPrefixes("/app");
        verify(registry).setUserDestinationPrefix("/user");
    }

    @Test
    void registerStompEndpoints_shouldRegisterEndpointWithInterceptor() {
        StompEndpointRegistry registry = mock(StompEndpointRegistry.class);
        StompWebSocketEndpointRegistration registration = mock(StompWebSocketEndpointRegistration.class);

        when(registry.addEndpoint("/ws")).thenReturn(registration);
        when(registration.addInterceptors(interceptor)).thenReturn(registration);
        when(registration.setAllowedOrigins("*")).thenReturn(registration);

        config.registerStompEndpoints(registry);

        verify(registry).addEndpoint("/ws");
        verify(registration).addInterceptors(interceptor);
        verify(registration).setAllowedOrigins("*");
    }

    @Test
    void configureMessageConverters_shouldAddJacksonConverter() {
        List<MessageConverter> converters = new ArrayList<>();

        boolean result = config.configureMessageConverters(converters);

        assertThat(result).isFalse();
        assertThat(converters).hasSize(1);

        MessageConverter converter = converters.get(0);
        assertThat(converter).isInstanceOf(MappingJackson2MessageConverter.class);

        MappingJackson2MessageConverter jacksonConverter = (MappingJackson2MessageConverter) converter;
        ObjectMapper objectMapper = jacksonConverter.getObjectMapper();

        assertThat(objectMapper.getRegisteredModuleIds()).isNotEmpty();
        assertThat(objectMapper.isEnabled(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)).isFalse();

        assertThat(jacksonConverter.getSupportedMimeTypes())
                .contains(MimeTypeUtils.APPLICATION_JSON);
    }
}
