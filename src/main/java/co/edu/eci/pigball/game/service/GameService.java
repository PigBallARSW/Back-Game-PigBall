package co.edu.eci.pigball.game.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import co.edu.eci.pigball.game.exception.GameException;
import co.edu.eci.pigball.game.model.Game;
import co.edu.eci.pigball.game.model.Movement;
import co.edu.eci.pigball.game.model.dto.GameDTO;
import co.edu.eci.pigball.game.model.entity.impl.Player;
import co.edu.eci.pigball.game.model.mapper.GameMapper;

@Service
public class GameService {

    private static final Logger logger = LoggerFactory.getLogger(GameService.class);
    private final SimpMessagingTemplate messagingTemplate; // Inject messaging template
    private final Map<String, Game> games;

    public GameService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
        this.games = new ConcurrentHashMap<>();
        Game game = new Game("Game 1", "Creator 1", 2, false, messagingTemplate);
        game.setIdForTest("1");
        games.put(game.getGameId(), game);
        Thread gameThread = new Thread(game);
        gameThread.start();
        logger.info("Game created id: " + game.getGameId());
    }

    public GameDTO createGame(GameDTO gameDTO) throws GameException {
        String gameName = gameDTO.getGameName();
        String creatorName = gameDTO.getCreatorName();
        int maxPlayers = gameDTO.getMaxPlayers();
        boolean privateGame = gameDTO.isPrivateGame();
        if (gameName == null || gameName.trim().isEmpty()) {
            throw new GameException(GameException.NOT_EMPTY_NAME);
        }
        Game game = new Game(gameName, creatorName, maxPlayers, privateGame, messagingTemplate); // Asegurar que el ID
                                                                                                 // es asignado
                                                                                                 // externamente

        // Verificar si el ID ya existe antes de agregarlo
        Game existingGame = games.putIfAbsent(game.getGameId(), game);
        if (existingGame != null) {
            throw new GameException("Game ID already exists: " + game.getGameId());
        }

        Thread gameThread = new Thread(game);
        gameThread.start();
        logger.info("Game created id: " + game.getGameId());
        return GameMapper.toDTO(game);
    }

    public GameDTO getGame(String gameId) throws GameException {
        if (gameId == null) {
            throw new GameException(GameException.NOT_EMPTY_ID);
        }
        Game game = games.get(gameId);
        if (game == null) {
            throw new GameException(GameException.GAME_NOT_FOUND);
        }
        return GameMapper.toDTO(game);
    }

    public Collection<GameDTO> getAllGames() {
        return GameMapper.toDTO(games.values());
    }

    public void removeGame(String gameId) throws GameException {
        if (gameId == null) {
            throw new GameException(GameException.NOT_EMPTY_ID);
        }
        Game game = games.get(gameId);
        if (game == null) {
            throw new GameException(GameException.GAME_NOT_FOUND);
        }
        logger.info("Game removed id: " + game.getGameId());
        games.remove(gameId);
    }

    public List<Player> addPlayerToGame(String gameId, Player player) throws GameException {
        Game game = games.get(gameId);
        if (game == null) {
            throw new GameException(GameException.GAME_NOT_FOUND);
        }
        logger.info("Player added with name: " + player.getName() + " to game " + game.getGameId());
        game.addPlayer(player);
        return game.getAllPlayers();
    }

    public List<Player> removePlayerFromGame(String gameId, Player player) throws GameException {
        if (gameId == null) {
            throw new GameException(GameException.NOT_EMPTY_ID);
        }
        Game game = games.get(gameId);
        if (game == null) {
            throw new GameException(GameException.GAME_NOT_FOUND);
        }
        logger.info("Player removed with name: " + player.getName() + " from game " + game.getGameId());
        game.removePlayer(player);
        return game.getAllPlayers();
    }

    public List<Player> removePlayerFromGame(String gameId, String playerName) throws GameException {
        if (gameId == null) {
            throw new GameException(GameException.NOT_EMPTY_ID);
        }
        Game game = games.get(gameId);
        if (game == null) {
            throw new GameException(GameException.GAME_NOT_FOUND);
        }
        logger.info("Player removed with name: " + playerName + " from game " + game.getGameId());
        game.removePlayer(playerName);
        return game.getAllPlayers();
    }

    public List<Player> getPlayersFromGame(String gameId) throws GameException {
        if (gameId == null) {
            throw new GameException(GameException.NOT_EMPTY_ID);
        }
        Game game = games.get(gameId);
        if (game == null) {
            throw new GameException(GameException.GAME_NOT_FOUND);
        }
        logger.info("Players from game " + game.getGameId() + " retrieved");
        return game.getAllPlayers();
    }

    public GameDTO startGame(String gameId) throws GameException {
        if (gameId == null) {
            throw new GameException(GameException.NOT_EMPTY_ID);
        }
        Game game = games.get(gameId);
        if (game == null) {
            throw new GameException(GameException.GAME_NOT_FOUND);
        }
        logger.info("Game started id: " + game.getGameId());
        return game.startGame();
    }

    public void makeMoveInGame(String gameId, Movement movement) throws GameException {
        if (gameId == null) {
            throw new GameException(GameException.NOT_EMPTY_ID);
        }
        Game game = games.get(gameId);
        if (game == null) {
            throw new GameException(GameException.GAME_NOT_FOUND);
        }
        if (movement.getPlayer() == null) {
            throw new GameException(GameException.NOT_EMPTY_PLAYER);
        }
        game.makeAMove(movement.getPlayer(), movement.getDx(), movement.getDy(), movement.isKicking());
    }

}
