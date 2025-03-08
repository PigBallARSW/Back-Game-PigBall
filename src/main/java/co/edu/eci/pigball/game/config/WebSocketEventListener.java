package co.edu.eci.pigball.game.config;


import org.springframework.context.ApplicationListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;

import co.edu.eci.pigball.game.controller.GameController;

@Component
public class WebSocketEventListener implements ApplicationListener<SessionConnectEvent> {

    private final GameController gameController; // Reference to game logic

    public WebSocketEventListener(GameController gameController) {
        this.gameController = gameController;
    }

    @Override
    public void onApplicationEvent(SessionConnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        
        // Get the player's name from headers (or assign a default)
        String playerName = (String) headerAccessor.getSessionAttributes().get("playerName");
        if (playerName == null) {
            playerName = "Player-" + event.getTimestamp(); // Default name if not provided
        }

        System.out.println("New player connected: " + playerName);

        
    }
}