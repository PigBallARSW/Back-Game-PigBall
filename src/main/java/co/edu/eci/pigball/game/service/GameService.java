package co.edu.eci.pigball.game.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import co.edu.eci.pigball.game.model.store.IGameStore;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import co.edu.eci.pigball.game.model.store.RedisGameStore;
import co.edu.eci.pigball.game.exception.GameException;
import co.edu.eci.pigball.game.model.Game;
import co.edu.eci.pigball.game.model.Movement;
import co.edu.eci.pigball.game.model.dto.GameDTO;
import co.edu.eci.pigball.game.model.entity.impl.Player;
import co.edu.eci.pigball.game.model.mapper.GameMapper;

@Service
public class GameService {

    private static final Logger logger = LoggerFactory.getLogger(GameService.class);

    private final IGameStore store;    // puede ser null si no hay bean
    private final SimpMessagingTemplate messagingTemplate;
    private final Map<String, Game> localGames = new ConcurrentHashMap<>();

    public GameService(
            IGameStore store,
            SimpMessagingTemplate messagingTemplate) {
        this.store = store; // null en modo memoria
        this.messagingTemplate = messagingTemplate;
    }

    @PostConstruct
    public void init() {
        try {
            // 1) recuperar o sembrar
            if (store.findAllMeta().isEmpty()) {
                seedTestGames();
            } else {
                recoverGames();
            }

            // 2) si el store es RedisGameStore, suscribirnos al topic de updates
            if (store instanceof RedisGameStore) {
                RedisGameStore redis = (RedisGameStore) store;
                redis.getUpdateTopic()
                        .addListener(GameDTO.class, (channel, dto) -> {
                            Game g = localGames.get(dto.getId());
                            if (g == null) {
                                startAndRegister(dto);
                            } else {
                                GameMapper.restoreState(g, dto);
                            }
                            // messagingTemplate.convertAndSend("/topic/play/"+dto.getId(), dto);
                        });
                logger.info("Subscribed to Redis updates topic");
            }

        } catch (Exception e) {
            logger.error("Init: error al recuperar o seed de juegos", e);
        }
    }

    private void seedTestGames() {
        for (int i = 1; i <= 5; i++) {
            Game game = new Game(
                    "Game " + i,
                    "Creator " + i,
                    20,
                    false,
                    "classic",
                    messagingTemplate
            );
            game.setIdForTest(String.valueOf(i));
            try {
                store.save(game);
                game.start();
                localGames.put(game.getGameId(), game);
                logger.info("Test game created id: {}", game.getGameId());
            } catch (GameException ex) {
                logger.error("Error al guardar partida de prueba {}: {}", game.getGameId(), ex.getMessage());
            }
        }
    }

    private void recoverGames() {
        for (GameDTO dto : store.findAllMeta()) {
            startAndRegister(dto);
            logger.info("Recovered and started game {}", dto.getId());
        }
    }

    private void startAndRegister(GameDTO dto) {
        Game game = new Game(
                dto.getGameName(),
                dto.getCreatorName(),
                dto.getMaxPlayers(),
                dto.isPrivateGame(),
                dto.getStyle(),
                messagingTemplate
        );
        game.setIdForTest(dto.getId());
        GameMapper.restoreState(game, dto);
        game.start();
        localGames.put(dto.getId(), game);
    }

    public GameDTO createGame(GameDTO dto) throws GameException {
        if (dto.getGameName() == null || dto.getGameName().trim().isEmpty()) {
            throw new GameException(GameException.NOT_EMPTY_NAME);
        }
        Game game = new Game(
                dto.getGameName(),
                dto.getCreatorName(),
                dto.getMaxPlayers(),
                dto.isPrivateGame(),
                dto.getStyle(),
                messagingTemplate
        );
        String id = game.getGameId();
        if (store.exists(id)) {
            throw new GameException("Game ID already exists: " + id);
        }
        // Arrancamos la partida en memoria y la persistimos
        localGames.put(id, game);
        game.start();
        store.save(game);
        logger.info("Created new game {}", id);
        return GameMapper.toDTO(game);
    }

    public GameDTO getGame(String gameId) throws GameException {
        if (gameId == null) {
            throw new GameException(GameException.NOT_EMPTY_ID);
        }
        Game game = localGames.get(gameId);
        if (game == null) {
            // Si no la hemos cargado en memoria, leemos el DTO y la reconstruimos
            GameDTO dto = store.findMeta(gameId);
            game = new Game(dto.getGameName(), dto.getCreatorName(), dto.getMaxPlayers(), dto.isPrivateGame(), dto.getStyle(), messagingTemplate);
            GameMapper.restoreState(game, dto);
            game.start();
            localGames.put(gameId, game);
        }
        return GameMapper.toDTO(game);
    }
    public Collection<GameDTO> getAllGames(){
        // Aseguramos que localGames contiene todas las que hay en meta
        for (GameDTO dto : store.findAllMeta()) {
            if (!localGames.containsKey(dto.getId())) {
                Game g = new Game(
                        dto.getGameName(),
                        dto.getCreatorName(),
                        dto.getMaxPlayers(),
                        dto.isPrivateGame(),
                        dto.getStyle(),
                        messagingTemplate
                );
                GameMapper.restoreState(g, dto);
                g.start();
                localGames.put(dto.getId(), g);
            }
        }
        // Ahora mapeamos esos Games vivos a DTOs actualizados
        return localGames.values().stream()
                .map(GameMapper::toDTO)
                .collect(Collectors.toList());
    }
    public GameDTO removeGame(String gameId) throws GameException {
        if (gameId == null) {
            throw new GameException(GameException.NOT_EMPTY_ID);
        }
        Game game = localGames.remove(gameId);
        if (game == null) {
            throw new GameException(GameException.GAME_NOT_FOUND);
        }
        if (game != null) {
            game.stop();
        }
        store.deleteMeta(gameId);
        logger.info("Removed game {}", gameId);
        return GameMapper.toDTO(game);
    }
    public GameDTO addPlayerToGame(String gameId, Player player) throws GameException {
        Game game = localGames.get(gameId);
        if (game == null) {
            throw new GameException(GameException.GAME_NOT_FOUND);
        }
        game.addPlayer(player);
        store.save(game);
        logger.info("Added player {} to game {}", player.getName(), gameId);
        return GameMapper.toDTO(game);
    }
    public List<Player> removePlayerFromGame(String gameId, Player player) throws GameException {
        return removePlayerFromGame(gameId, player.getName());
    }
    public List<Player> removePlayerFromGame(String gameId, String playerName) throws GameException {
        if (gameId == null) {
            throw new GameException(GameException.NOT_EMPTY_ID);
        }
        Game game = localGames.get(gameId);
        if (game == null) {
            throw new GameException(GameException.GAME_NOT_FOUND);
        }
        game.removePlayer(playerName);
        store.save(game);
        logger.info("Removed player {} from game {}", playerName, gameId);
        return game.getAllPlayers();
    }

    public List<Player> getPlayersFromGame(String gameId) throws GameException {
        if (gameId == null) {
            throw new GameException(GameException.NOT_EMPTY_ID);
        }
        Game game = localGames.get(gameId);
        if (game == null) {
            throw new GameException(GameException.GAME_NOT_FOUND);
        }
        return game.getAllPlayers();
    }

    public GameDTO startGame(String gameId) throws GameException {
        if (gameId == null) {
            throw new GameException(GameException.NOT_EMPTY_ID);
        }
        Game game = localGames.get(gameId);
        if (game == null) {
            throw new GameException(GameException.GAME_NOT_FOUND);
        }
        GameDTO dto = game.startGame();
        store.save(game);
        logger.info("Started game {}", gameId);
        return dto;
    }

    public void makeMoveInGame(String gameId, Movement movement) throws GameException {
        if (gameId == null) {
            throw new GameException(GameException.NOT_EMPTY_ID);
        }
        Game game = localGames.get(gameId);
        if (game == null) {
            throw new GameException(GameException.GAME_NOT_FOUND);
        }
        if (movement.getPlayer() == null) {
            throw new GameException(GameException.NOT_EMPTY_PLAYER);
        }
        game.updatePlayerLastMove(movement.getPlayer(), movement.getDx(), movement.getDy(), movement.isKicking());
        store.save(game);
    }

}
