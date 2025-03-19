package co.edu.eci.pigball.game.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import co.edu.eci.pigball.game.exception.GameException;
import co.edu.eci.pigball.game.model.Game;
import co.edu.eci.pigball.game.model.Movement;
import co.edu.eci.pigball.game.model.Player;
import co.edu.eci.pigball.game.model.DTO.GameDTO;

@Service
public class GameService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate; // Inject messaging template

    private final Map<Long, Game> games = new ConcurrentHashMap<>();
    
    public GameDTO createGame(String gameName) throws GameException {
        if (gameName == null) {
            throw new GameException(GameException.NOT_EMPTY_NAME);
        }
        Game game = new Game(gameName, messagingTemplate);
        Thread gameThread = new Thread(game);
        gameThread.start();
        games.put(game.getGameId(), game);
        System.out.println("Game " + game.getGameId() + " created");
        return GameDTO.toDTO(game);
    }

    public GameDTO getGame(Long gameId) throws GameException {
        if (gameId == null) {
            throw new GameException(GameException.NOT_EMPTY_ID);
        }
        Game game = games.get(gameId);
        if (game == null) {
            throw new GameException(GameException.GAME_NOT_FOUND);
        }
        return GameDTO.toDTO(game);
    }

    public Collection<GameDTO> getAllGames() {
        return GameDTO.toDTO(games.values());
    }

    public void removeGame(Long gameId) throws GameException {
        if (gameId == null) {
            throw new GameException(GameException.NOT_EMPTY_ID);
        }
        Game game = games.get(gameId);
        if (game == null) {
            throw new GameException(GameException.GAME_NOT_FOUND);
        }
        System.out.println("Game " + game.getGameId() + " removed");
        games.remove(gameId);
    }

    public List<Player> addPlayerToGame(Long gameId, Player player) throws GameException {
        Game game = games.get(gameId);
        if (game == null) {
            throw new GameException(GameException.GAME_NOT_FOUND);
        }
        System.out.println("Player " + player.getName() + " joined to game " + game.getGameId());
        game.addPlayer(player);
        return game.getAllPlayers();
    }

    public List<Player> removePlayerFromGame(Long gameId, Player player) throws GameException {
        Game game = games.get(gameId);
        if (game == null) {
            throw new GameException(GameException.GAME_NOT_FOUND);
        }
        System.out.println("Player " + player.getName() + " left game " + game.getGameId());
        game.removePlayer(player);
        return game.getAllPlayers();
    }

    public List<Player> getPlayersFromGame(Long gameId) throws GameException {
        Game game = games.get(gameId);
        if (game == null) {
            throw new GameException(GameException.GAME_NOT_FOUND);
        }
        return game.getAllPlayers();
    }

    public void makeMoveInGame(Long gameId, Movement movement) throws GameException {
        Game game = games.get(gameId);
        if (game == null) {
           throw new GameException(GameException.GAME_NOT_FOUND);
        }
        if (movement.getPlayer() == null) {
            throw new GameException(GameException.NOT_EMPTY_PLAYER);
        }
        game.makeAMove(movement.getPlayer(), movement.getDx(), movement.getDy());
    }

    
}
