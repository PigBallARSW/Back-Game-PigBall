package co.edu.eci.pigball.game.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

import co.edu.eci.pigball.game.model.Game;
import co.edu.eci.pigball.game.model.Movement;
import co.edu.eci.pigball.game.model.Player;
import co.edu.eci.pigball.game.model.DTO.GameDTO;
import lombok.Getter;
import lombok.Setter;

@Controller
@Getter
@Setter
public class GameController {
    @Autowired
    private Game game;

    @Autowired
    private SimpMessagingTemplate messagingTemplate; // Inject messaging template
    private static final double FRAME_RATE = 60;

    @MessageMapping("/play")
    public void makeAMovement(Movement movement) {
        game.makeAMove(movement.getPlayer(), movement.getDx(), movement.getDy());
        // No need to return anything; state is sent periodically
    }

    @Scheduled(fixedRate = ((int)(1000/FRAME_RATE)))
    public void broadcastGameState() {
        try {
            GameDTO gameDTO = game.getGameDTO();
            if (gameDTO == null) {
                return; // Prevent sending null messages
            }
            messagingTemplate.convertAndSend("/topic/play", gameDTO);
        } catch (Exception e) {
        }
    }

    @MessageMapping("/join")
    @SendTo("/topic/playerJoined")
    public List<Player> handleNewPlayer(Player player) {
        System.out.println("New player joined: " + player.getName());

        // Add player to the game
        game.addPlayer(player.getName(), player);

        // Return the updated list of all players in the game
        return game.getAllPlayers();
    }
}