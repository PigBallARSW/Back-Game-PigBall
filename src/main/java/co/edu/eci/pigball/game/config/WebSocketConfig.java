package co.edu.eci.pigball.game.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${ALLOWED_ORIGINS_HTTP}")
    private String allowedOriginsHttp;

    @Value("${ALLOWED_ORIGINS_HTTPS}")
    private String allowedOriginsHttps;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        String[] origins = (allowedOriginsHttp + "," + allowedOriginsHttps).split(",");
        registry.addEndpoint("/pigball")
                .setAllowedOrigins(origins);
    }
}
