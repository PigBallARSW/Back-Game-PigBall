package co.edu.eci.pigball.game.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.springframework.messaging.simp.SimpMessagingTemplate;

import co.edu.eci.pigball.game.exception.GameException;
import co.edu.eci.pigball.game.java.Pair;
import co.edu.eci.pigball.game.model.dto.GameDTO;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Getter
public class Game implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(Game.class);

    private SimpMessagingTemplate messagingTemplate;
    private String gameId;
    private String gameName;
    private String creatorName;
    private int maxPlayers;
    private GameStatus status;
    private boolean privateGame;
    private Instant creationTime;
    private int borderX;
    private int borderY;
    private Pair<Team, Team> teams;
    private ConcurrentHashMap<String, Player> players;

    private static final int VELOCITY = 5;
    private static final double FRAME_RATE = 60;

    public Game(String gameName, String creatorName, int maxPlayers, boolean privateGame,
            SimpMessagingTemplate messagingTemplate) {
        this.gameId = UUID.randomUUID().toString();
        this.gameName = gameName;
        this.creatorName = creatorName;
        this.maxPlayers = maxPlayers;
        this.status = GameStatus.WAITING_FOR_PLAYERS;
        this.privateGame = privateGame;
        this.creationTime = Instant.now();
        this.messagingTemplate = messagingTemplate;
        this.borderX = 1200;
        this.borderY = 900;
        this.teams = new Pair<>(new Team(), new Team());
        this.players = new ConcurrentHashMap<>();
    }

    public void setIdForTest(String id) {
        this.gameId = id;
    }

    @Override
    public void run() {
        while (status != GameStatus.FINISHED && status != GameStatus.ABANDONED) {
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
            logger.error("Error al enviar el estado del juego");
        }
    }

    public void addPlayer(Player player) throws GameException {
        validatePlayerTeam(player);
        validateMaxPlayers();

        players.compute(player.getName(), (key, existingPlayer) -> {
            if (existingPlayer != null) {
                return handleExistingPlayer(player, existingPlayer);
            }
            return handleNewPlayer(player);
        });
    }

    private void validatePlayerTeam(Player player) throws GameException {
        if (player.getTeam() != null && player.getTeam() != 0 && player.getTeam() != 1) {
            throw new GameException(GameException.INVALID_TEAM);
        }
    }

    private void validateMaxPlayers() throws GameException {
        if (players.size() >= maxPlayers) {
            throw new GameException(GameException.EXCEEDED_MAX_PLAYERS);
        }
    }

    private Player handleExistingPlayer(Player player, Player existingPlayer) {
        player.setPosition(borderX, borderY, existingPlayer.getX(), existingPlayer.getY());

        if (player.getTeam() == null) {
            player.setTeam(existingPlayer.getTeam());
        } else if (!player.getTeam().equals(existingPlayer.getTeam())) {
            updateTeamCounts(existingPlayer.getTeam(), player.getTeam());
        }
        return player;
    }

    private Player handleNewPlayer(Player player) {
        player.setPosition(borderX, borderY,
                (int) (Math.random() * (borderX - 20)) + 20,
                (int) (Math.random() * (borderY - 20)) + 20);

        if (player.getTeam() == null) {
            assignTeam(player);
        }
        return player;
    }

    private void updateTeamCounts(Integer oldTeam, Integer newTeam) {
        if (oldTeam == 0) {
            teams.getFirst().removePlayer();
            teams.getSecond().addPlayer();
        } else {
            teams.getSecond().removePlayer();
            teams.getFirst().addPlayer();
        }
    }

    private void assignTeam(Player player) {
        if (teams.getFirst().getPlayers() < teams.getSecond().getPlayers()) {
            player.setTeam(0);
            teams.getFirst().addPlayer();
        } else {
            player.setTeam(1);
            teams.getSecond().addPlayer();
        }
    }

    public void removePlayer(Player player) {
        players.remove(player.getName());
    }

    public void removePlayer(String playerName) {
        players.remove(playerName);
    }

    public List<Player> getAllPlayers() {
        return new ArrayList<>(players.values());
    }

    public GameDTO startGame() {
        status = GameStatus.STARTING;
        int ubicatedPlayersTeamOne = 0;
        int ubicatedPlayersTeamTwo = 0;
        for (Player player : players.values()) {
            if (player.getTeam() == 0) {
                int x = ubicatedPlayersTeamOne % 2 == 0 ? 5 : 0;
                int y = (((borderY - 40) / (maxPlayers / 2)) * ubicatedPlayersTeamOne) + 5;
                player.setPosition(borderX, borderY, x, y);
                ubicatedPlayersTeamOne++;
            } else {
                int x = ubicatedPlayersTeamTwo % 2 == 0 ? borderX - 40 : borderX - 5;
                int y = (((borderY - 40) / (maxPlayers / 2)) * ubicatedPlayersTeamTwo) + 5;
                player.setPosition(borderX, borderY, x, y);
                ubicatedPlayersTeamTwo++;
            }
        }

        try {
            Thread.sleep(5000);
            status = GameStatus.IN_PROGRESS;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Game start interrupted", e);
        }
        return GameDTO.toDTO(this);
    }

    public void makeAMove(String name, int dx, int dy) {
        if (status != GameStatus.WAITING_FOR_PLAYERS && status != GameStatus.IN_PROGRESS) {
            return;
        }
        if (!players.containsKey(name)) {
            return;
        }

        Player player = players.get(name);

        double fdx = dx;
        double fdy = dy;
        double magnitude = Math.sqrt(fdx * fdx + fdy * fdy);
        if (magnitude > 0) {
            fdx /= magnitude;
            fdy /= magnitude;
        }
        // Uso de Delta Time
        double dt = 100.0 / FRAME_RATE; // Delta Time basado en el framerate
        double adjustedVelocity = VELOCITY * dt;
        player.move(borderX, borderY, (int) (fdx * adjustedVelocity), (int) (fdy * adjustedVelocity),
                new ArrayList<>(players.values()));
    }

}
