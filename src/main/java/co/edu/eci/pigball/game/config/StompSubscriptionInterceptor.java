package co.edu.eci.pigball.game.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

@Component
public class StompSubscriptionInterceptor implements ChannelInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(StompSubscriptionInterceptor.class);

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        SimpMessageHeaderAccessor accessor = SimpMessageHeaderAccessor.wrap(message);

        if (StompCommand.SUBSCRIBE.equals(accessor.getHeader("stompCommand"))) {
            String sessionId = accessor.getSessionId();
            String destination = accessor.getDestination();
            logger.info("📢 Cliente suscrito - SessionID: {}, Destino: {}", sessionId, destination);
        }

        return message;
    }
}
