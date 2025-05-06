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
        Game game1 = new Game("Game 1", "Creator 1", 20, false, messagingTemplate);
        Game game2 = new Game("Game 2", "Creator 2", 20, false, messagingTemplate);
        Game game3 = new Game("Game 3", "Creator 3", 20, false, messagingTemplate);
        Game game4 = new Game("Game 4", "Creator 4", 20, false, messagingTemplate);
        Game game5 = new Game("Game 5", "Creator 5", 20, false, messagingTemplate);
        game1.setIdForTest("1");
        games.put(game1.getGameId(), game1);
        game1.start();
        logger.info("Game created id: " + game1.getGameId());
        game2.setIdForTest("2");
        games.put(game2.getGameId(), game2);
        game2.start();
        logger.info("Game created id: " + game2.getGameId());
        game3.setIdForTest("3");
        games.put(game3.getGameId(), game3);
        game3.start();
        logger.info("Game created id: " + game3.getGameId());
        game4.setIdForTest("4");
        games.put(game4.getGameId(), game4);
        game4.start();
        logger.info("Game created id: " + game4.getGameId());
        game5.setIdForTest("5");
        games.put(game5.getGameId(), game5);
        game5.start();
        logger.info("Game created id: " + game5.getGameId());
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

        game.start();
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

    public GameDTO removeGame(String gameId) throws GameException {
        if (gameId == null) {
            throw new GameException(GameException.NOT_EMPTY_ID);
        }
        Game game = games.get(gameId);
        if (game == null) {
            throw new GameException(GameException.GAME_NOT_FOUND);
        }
        logger.info("Game removed id: " + game.getGameId());
        games.remove(gameId);
        game.stop();
        return GameMapper.toDTO(game);
    }

    public GameDTO addPlayerToGame(String gameId, Player player) throws GameException {
        Game game = games.get(gameId);
        if (game == null) {
            throw new GameException(GameException.GAME_NOT_FOUND);
        }
        logger.info("Player added with name: " + player.getName() + " to game " + game.getGameId());
        game.addPlayer(player);
        return GameMapper.toDTO(game);
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
        game.updatePlayerLastMove(movement.getPlayer(), movement.getDx(), movement.getDy(), movement.isKicking());
    }

}
