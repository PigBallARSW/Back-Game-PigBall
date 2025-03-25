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
import co.edu.eci.pigball.game.model.DTO.GameDTO;
import lombok.Getter;

@Getter
public class Game implements Runnable {

    

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

    private static final int velocity = 5;
    private static final double FRAME_RATE = 60;

    public Game(String gameName, String creatorName, int maxPlayers, boolean privateGame, SimpMessagingTemplate messagingTemplate) {
        this.gameId = UUID.randomUUID().toString(); // Genera un UUID único
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
        }
    }

    public void addPlayer(Player player) throws GameException {
        // Validar el equipo, si es distinto de null debe ser 0 o 1.
        if (player.getTeam() != null && player.getTeam() != 0 && player.getTeam() != 1) {
            throw new GameException(GameException.INVALID_TEAM);
        }
        // Verificar que no se exceda el máximo de jugadores
        if (players.size() >= maxPlayers) {
            throw new GameException(GameException.EXCEEDED_MAX_PLAYERS);
        }
        
        players.compute(player.getName(), (key, existingPlayer) -> {
            if (existingPlayer != null) {
                // Conservar la posición existente
                player.setX(existingPlayer.getX());
                player.setY(existingPlayer.getY());
                
                // Si el nuevo jugador tiene equipo nulo, se asigna el equipo del existente.
                if (player.getTeam() == null) {
                    player.setTeam(existingPlayer.getTeam());
                } 
                // Si ambos tienen equipos no nulos, se comparan.
                else if (!player.getTeam().equals(existingPlayer.getTeam())) {
                    // Actualizar contadores de equipos: restar en el equipo antiguo y sumar en el nuevo.
                    if (existingPlayer.getTeam() == 0) {
                        teams.getFirst().removePlayer();
                        teams.getSecond().addPlayer();
                    } else { // Si el existente era del equipo 1
                        teams.getSecond().removePlayer();
                        teams.getFirst().addPlayer();
                    }
                }
            } else {
                // Jugador nuevo: asignar posiciones aleatorias.
                player.setX((int) (Math.random() * borderX));
                player.setY((int) (Math.random() * borderY));
                
                // Si el jugador no tiene equipo asignado, se le asigna el equipo según el balance.
                if (player.getTeam() == null) {
                    if (teams.getFirst().getPlayers() < teams.getSecond().getPlayers()) {
                        player.setTeam(0);
                        teams.getFirst().addPlayer();
                    } else {
                        player.setTeam(1);
                        teams.getSecond().addPlayer();
                    }
                }
            }
            return player;
        });
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

    public void startGame() {
        status = GameStatus.STARTING;
        int ubicatedPlayersTeamOne = 0;
        int ubicatedPlayersTeamTwo = 0;
        for (Player player : players.values()) {
            if (player.getTeam() == 0) {
                int x = ubicatedPlayersTeamOne % 2 == 0 ? 5 : 0;
                int y = (((borderY-10) / (maxPlayers / 2)) * ubicatedPlayersTeamOne) + 5;
                player.setX(x);
                player.setY(y);
                ubicatedPlayersTeamOne++;
            } else {
                int x = ubicatedPlayersTeamTwo % 2 == 0 ? borderX - 10 : borderX - 5;
                int y = (((borderY-10) / (maxPlayers / 2)) * ubicatedPlayersTeamTwo) + 5;
                player.setX(x);
                player.setY(y);
                ubicatedPlayersTeamTwo++;
            }
        }
        
        try {
            Thread.sleep(5000);
            status = GameStatus.IN_PROGRESS;
        } catch (InterruptedException e) {
            e.printStackTrace();
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
        double adjustedVelocity = velocity * dt;
        player.moveInX((int) (fdx * adjustedVelocity));
        player.moveInY((int) (fdy * adjustedVelocity));
    }
    

}
