package co.edu.eci.pigball.game.model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.messaging.simp.SimpMessagingTemplate;

import co.edu.eci.pigball.game.model.DTO.GameDTO;
import lombok.Getter;

@Getter
public class Game implements Runnable {

    private static AtomicLong identifier = new AtomicLong(0);
    private static final double FRAME_RATE = 60;

    private SimpMessagingTemplate messagingTemplate;
    private Long gameId;
    private String gameName;
    private ConcurrentHashMap<String, Player> players;

    private static final int velocity = 5;

    public Game(String gameName, SimpMessagingTemplate messagingTemplate) {
        this.gameId = identifier.getAndIncrement();
        this.gameName = gameName;
        this.messagingTemplate = messagingTemplate;
        this.players = new ConcurrentHashMap<>();
    }

    @Override
    public void run() {
        while (true) {
            try {
                broadcastGameState();
                TimeUnit.MILLISECONDS.sleep((int) (1000 / FRAME_RATE));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public void broadcastGameState() {
        try {
            messagingTemplate.convertAndSend("/topic/play/" + gameId, GameDTO.toDTO(this));
        } catch (Exception e) {
        }
    }

    public void addPlayer(Player player) {
        players.put(player.getName(), player);
    }

    public void removePlayer(Player player) {
        players.remove(player.getName());
    }

    public List<Player> getAllPlayers() {
        return new ArrayList<>(players.values());
    }

    public void makeAMove(String name, int dx, int dy) {
        if (!players.containsKey(name)) {
            addPlayer(new Player(name, 0, 0));
        }
        Player player = players.get(name);
        player.moveInX(dx * Game.velocity);
        player.moveInY(dy * Game.velocity);
    }

}
