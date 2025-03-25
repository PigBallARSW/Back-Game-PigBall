package co.edu.eci.pigball.game.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import co.edu.eci.pigball.game.exception.GameException;
import co.edu.eci.pigball.game.model.Movement;
import co.edu.eci.pigball.game.model.Player;
import co.edu.eci.pigball.game.service.GameService;
import lombok.Getter;
import lombok.Setter;

import co.edu.eci.pigball.game.config.WebSocketEventListener;

@Controller
@Getter
@Setter
public class GameController {
    @Autowired
    private GameService gameService;
    @Autowired
    private WebSocketEventListener webSocketEventListener;

    @MessageMapping("/join/{game_id}")
    @SendTo("/topic/players/{game_id}")
    public List<Player> handleNewPlayer(@DestinationVariable("game_id") String gameId, Player player,
            SimpMessageHeaderAccessor headerAccessor) {
        try {
            String sessionId = headerAccessor.getSessionId();
            player.setSessionId(sessionId);
            List<Player> players = gameService.addPlayerToGame(gameId, player);
            webSocketEventListener.setANewConnection(sessionId, gameId, player.getName());
            return players;
        } catch (GameException e) {
            return null;
            
        }
    }

    @MessageMapping("/leave/{game_id}")
    @SendTo("/topic/players/{game_id}")
    public List<Player> handlePlayerExit(@DestinationVariable("game_id") String gameId, Player player) {
        try {
            return gameService.removePlayerFromGame(gameId, player);
        } catch (GameException e) {
            try {
                return gameService.getPlayersFromGame(gameId);
            } catch (GameException e1) {
                return null;
            }
        }
    }

    @MessageMapping("/start/{game_id}")
    public void startGame(@DestinationVariable("game_id") String gameId) {
        try {
            gameService.startGame(gameId);
        } catch (GameException e) {
        }
    }

    @MessageMapping("/play/{game_id}")
    public void makeAMovement(@DestinationVariable("game_id") String gameId, Movement movement) {
        try {
            gameService.makeMoveInGame(gameId, movement);
        } catch (GameException e) {
        }
    }
}