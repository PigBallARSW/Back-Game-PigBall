package co.edu.eci.pigball.game.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.springframework.messaging.simp.SimpMessagingTemplate;

import co.edu.eci.pigball.game.model.DTO.GameDTO;
import lombok.Getter;

@Getter
public class Game implements Runnable {

    private static final double FRAME_RATE = 60;

    private SimpMessagingTemplate messagingTemplate;
    private String gameId;
    private String gameName;
    private String creatorName;
    private int maxPlayers;
    private GameStatus status;
    private boolean privateGame;
    private Instant creationTime;
    private ConcurrentHashMap<String, Player> players;

    private static final int velocity = 5;

    public Game(String gameName, String creatorName, int maxPlayers, boolean privateGame, SimpMessagingTemplate messagingTemplate) {
        this.gameId = UUID.randomUUID().toString(); // Genera un UUID único
        this.gameName = gameName;
        this.creatorName = creatorName;
        this.maxPlayers = maxPlayers;
        this.status = GameStatus.WAITING_FOR_PLAYERS;
        this.privateGame = privateGame;
        this.creationTime = Instant.now();
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
        Player playerInGame = players.get(player.getName());
        if (playerInGame != null) {
            player.setX(playerInGame.getX());
            player.setY(playerInGame.getY());
        } else {
            player.setX((int) (Math.random() * 100));
            player.setY((int) (Math.random() * 100));
        }
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
