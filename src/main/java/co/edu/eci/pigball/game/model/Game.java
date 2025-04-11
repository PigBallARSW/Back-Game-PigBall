package co.edu.eci.pigball.game.model;

import java.security.SecureRandom;
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
public class Game implements Runnable, GameObserver {

    private static final Logger logger = LoggerFactory.getLogger(Game.class);

    private SimpMessagingTemplate messagingTemplate;
    private String gameId;
    private String gameName;
    private String creatorName;
    private int maxPlayers;
    private GameStatus status;
    private boolean privateGame;
    private Instant creationTime;
    private Instant startTime;
    private int borderX;
    private int borderY;
    private Pair<Team, Team> teams;
    private ConcurrentHashMap<String, Player> players;
    private Ball ball;

    private static final int VELOCITY = 3;
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
        this.startTime = null;
        this.messagingTemplate = messagingTemplate;
        this.borderX = 1200;
        this.borderY = 900;
        this.teams = new Pair<>(new Team(), new Team());
        this.players = new ConcurrentHashMap<>();
        this.ball = new Ball(this.borderX / 2, this.borderY / 2, 0, 0);
        this.ball.addObserver(this);
    }

    public void setIdForTest(String id) {
        this.gameId = id;
    }

    @Override
    public void run() {
        Instant actualTime = Instant.now();
        while (status != GameStatus.FINISHED && status != GameStatus.ABANDONED) {
            try {
                broadcastGameState();
                TimeUnit.MILLISECONDS.sleep((int) (1000 / FRAME_RATE));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }

            actualTime = Instant.now();

            if (startTime != null && actualTime.isAfter(startTime.plusSeconds(300))) {
                status = GameStatus.FINISHED;
                try {
                    messagingTemplate.convertAndSend("/topic/finished/" + gameId, GameDTO.toDTO(this));
                    logger.info("La partida con id " + gameId + " ha terminado.");
                } catch (Exception e) {
                    logger.error("Error al enviar el estado del juego");
                }
            }
            else if (creationTime.plusSeconds(1800).isBefore(actualTime) && status == GameStatus.WAITING_FOR_PLAYERS) {
                status = GameStatus.ABANDONED;
                try {
                    messagingTemplate.convertAndSend("/topic/abandoned/" + gameId, GameDTO.toDTO(this));
                    logger.info("La partida con id " + gameId + " ha sido abandonada por inactividad.");
                } catch (Exception e) {
                    logger.error("Error al enviar el estado del juego");
                }
            }
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

        if (players.size() == maxPlayers && status == GameStatus.WAITING_FOR_PLAYERS) {
            status = GameStatus.WAITING_FULL;
        } else if (players.size() == maxPlayers && status == GameStatus.IN_PROGRESS) {
            status = GameStatus.IN_PROGRESS_FULL;
        }
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
        Pair<Double, Double> coordinates = new Pair<>(existingPlayer.getX(), existingPlayer.getY());
        player.setPosition(borderX, borderY, coordinates, new ArrayList<>());

        if (player.getTeam() == null) {
            player.setTeam(existingPlayer.getTeam());
        } else if (!player.getTeam().equals(existingPlayer.getTeam())) {
            updateTeamCounts(existingPlayer.getTeam());
        }
        return player;
    }

    private Player handleNewPlayer(Player player) {
        SecureRandom random = new SecureRandom();
        double newX = random.nextDouble(borderX - 40) + 20;
        double newY = random.nextDouble(borderY - 40) + 20;
        Pair<Double, Double> coordinates = new Pair<>(newX, newY);
        player.setPosition(borderX, borderY, coordinates, new ArrayList<>());

        if (player.getTeam() == null) {
            assignTeam(player);
        }
        return player;
    }

    private void updateTeamCounts(Integer oldTeam) {
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
        removePlayer(player.getName());
    }

    public void removePlayer(String playerName) {
        players.remove(playerName);
        if (players.size() == 0) {
            status = GameStatus.ABANDONED;
        } else if (players.size() == maxPlayers - 1 && status == GameStatus.WAITING_FOR_PLAYERS) {
            status = GameStatus.WAITING_FOR_PLAYERS;
        } else if (players.size() == maxPlayers - 1 && status == GameStatus.IN_PROGRESS) {
            status = GameStatus.IN_PROGRESS;
        }
    }

    public List<Player> getAllPlayers() {
        return new ArrayList<>(players.values());
    }

    public GameDTO startGame() throws GameException {
        
        status = GameStatus.STARTING;
        ubicatePlayersAndBallInTheField();
        try {
            Thread.sleep(5000);
            status = GameStatus.IN_PROGRESS;
            startTime = Instant.now();
            logger.info("La partida con id " + gameId + " ha comenzado.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new GameException(GameException.GAME_START_INTERRUPTED);
        }
        return GameDTO.toDTO(this);
    }

    private void ubicatePlayersAndBallInTheField() {
        this.ball.setVelocity(0, 0);
        this.ball.setPosition(borderX, borderY, new Pair<>(borderX / 2.0, borderY / 2.0), new ArrayList<>());
        int ubicatedPlayersTeamOne = 0;
        int ubicatedPlayersTeamTwo = 0;
        for (Player player : players.values()) {
            if (player.getTeam() == 0) {
                double base_x = 3 * Player.RADIUS;
                double base_y = 3 * Player.RADIUS;
                double x = ubicatedPlayersTeamOne % 2 == 0 ? base_x : base_x + (3 * Player.RADIUS);
                double y = 0;
                if( maxPlayers == 2) {  
                    y = borderY / 2;
                } else {
                    y = (((borderY - (2 * base_y)) / ((maxPlayers / 2) - 1)) * ubicatedPlayersTeamOne) + base_y;
                }
                logger.info("Player team 0: " + player.getName() + " set to position " + x + ", " + y);
                Pair<Double, Double> coordinates = new Pair<>(x, y);
                player.setPosition(borderX, borderY, coordinates, new ArrayList<>());
                ubicatedPlayersTeamOne++;
            } else {
                double base_x = borderX - (3 * Player.RADIUS);
                double base_y = 3 * Player.RADIUS;
                double x = ubicatedPlayersTeamTwo % 2 == 0 ? base_x : base_x - (3 * Player.RADIUS);
                double y = 0;
                if( maxPlayers == 2) {  
                    y = borderY / 2;
                } else {
                    y = (((borderY - (2 * base_y)) / ((maxPlayers / 2) - 1)) * ubicatedPlayersTeamTwo) + base_y;
                }
                logger.info("Player team 1: " + player.getName() + " set to position " + x + ", " + y);
                Pair<Double, Double> coordinates = new Pair<>(x, y);
                player.setPosition(borderX, borderY, coordinates, new ArrayList<>());
                ubicatedPlayersTeamTwo++;
            }
        }
    }
    
    public void broadcastGameState() {
        try {
            makeABallMove();
            messagingTemplate.convertAndSend("/topic/play/" + gameId, GameDTO.toDTO(this));
        } catch (Exception e) {
            logger.error("Error al enviar el estado del juego");
        }
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
        Pair<Double, Double> movement = new Pair<>(fdx * adjustedVelocity, fdy * adjustedVelocity);
        player.move(borderX, borderY, movement, new ArrayList<>(players.values()));
    }

    public void makeABallMove() {
        // dt en segundos (FRAME_RATE es frames por segundo)
        double dt = 1.0 / FRAME_RATE;  
        // Coeficiente de fricción (ajústalo según la sensación que busques)
        double frictionCoefficient = 1.8;  
        // Obtener velocidades actuales de la pelota
        double ballVelocityX = ball.getVelocityX();
        double ballVelocityY = ball.getVelocityY();
        // Calcular el factor de fricción para este frame
        double frictionFactor = 1 - frictionCoefficient * dt;
        // Aplicar la fricción a las velocidades
        ball.setVelocity(ballVelocityX * frictionFactor, ballVelocityY * frictionFactor);
        // Mover la pelota usando la velocidad actualizada y dt para un desplazamiento correcto
        ball.move(borderX, borderY, new Pair<>(ball.getVelocityX() * dt, ball.getVelocityY() * dt), new ArrayList<>(players.values()));
    }

    @Override
    public void onGoalScored(int team) {
        if (team == 0) {
            teams.getFirst().increaseScore();
        } else {
            teams.getSecond().increaseScore();
        }

        try {
            messagingTemplate.convertAndSend("/topic/goal/" + gameId, GameDTO.toDTO(this));
            ubicatePlayersAndBallInTheField();
            Thread.sleep(100);
            this.ball.setLastGoalTeam(-1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
