package belote.ex.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.socket.server.WebSocketService;
import org.springframework.web.reactive.socket.server.support.HandshakeWebSocketService;
import org.springframework.web.reactive.socket.server.upgrade.ReactorNettyRequestUpgradeStrategy;

@Configuration
public class GatewayWebSocketConfig {

    @Bean
    public WebSocketService webSocketService() {
        return new HandshakeWebSocketService(new ReactorNettyRequestUpgradeStrategy());
    }
}