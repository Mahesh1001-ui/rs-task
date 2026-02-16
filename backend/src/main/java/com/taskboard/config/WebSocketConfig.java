package com.taskboard.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final WebSocketEventHandler webSocketEventHandler;

    public WebSocketConfig(WebSocketEventHandler webSocketEventHandler) {
        this.webSocketEventHandler = webSocketEventHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(webSocketEventHandler, "/ws/taskboard")
                .setAllowedOrigins("*");
    }
}
