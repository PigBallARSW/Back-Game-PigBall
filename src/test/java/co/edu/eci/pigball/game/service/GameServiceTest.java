package co.edu.eci.pigball.game.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import co.edu.eci.pigball.game.exception.GameException;
import co.edu.eci.pigball.game.model.Movement;
import co.edu.eci.pigball.game.model.dto.GameDTO;
import co.edu.eci.pigball.game.model.dto.PlayerDTO;
import co.edu.eci.pigball.game.model.entity.impl.Player;

@ExtendWith(MockitoExtension.class)
class GameServiceTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;
    private static final double PLAYER_RADIUS = 20.0;
    private GameService gameService;
    private GameDTO gameDTO;

    @BeforeEach
    void setUp() {
        gameService = new GameService(messagingTemplate);
        gameDTO = new GameDTO();
        gameDTO.setGameName("Test Game");
        gameDTO.setCreatorName("Test Creator");
        gameDTO.setMaxPlayers(4);
        gameDTO.setPrivateGame(false);
    }

    @Test
    void testCreateGame() throws GameException {
        GameDTO createdGame = gameService.createGame(gameDTO);
        assertNotNull(createdGame);
        assertEquals(gameDTO.getGameName(), createdGame.getGameName());
        assertEquals(gameDTO.getCreatorName(), createdGame.getCreatorName());
        assertEquals(gameDTO.getMaxPlayers(), createdGame.getMaxPlayers());
        assertFalse(createdGame.isPrivateGame());
    }

    @Test
    void testCreateGameWithEmptyName() {
        gameDTO.setGameName("");
        assertThrows(GameException.class, () -> gameService.createGame(gameDTO));
    }

    @Test
    void testCreateGameWithNullName() {
        gameDTO.setGameName(null);
        assertThrows(GameException.class, () -> gameService.createGame(gameDTO));
    }

    @Test
    void testGetGame() throws GameException {
        GameDTO createdGame = gameService.createGame(gameDTO);
        GameDTO retrievedGame = gameService.getGame(createdGame.getId());
        assertNotNull(retrievedGame);
        assertEquals(createdGame.getId(), retrievedGame.getId());
    }

    @Test
    void testGetGameWithNullId() {
        assertThrows(GameException.class, () -> gameService.getGame(null));
    }

    @Test
    void testGetNonExistentGame() {
        assertThrows(GameException.class, () -> gameService.getGame("non-existent-id"));
    }

    @Test
    void testGetAllGames() throws GameException {
        gameService.createGame(gameDTO);
        Collection<GameDTO> games = gameService.getAllGames();
        assertNotNull(games);
        assertFalse(games.isEmpty());
    }

    @Test
    void testRemoveGame() throws GameException {
        GameDTO createdGame = gameService.createGame(gameDTO);
        gameService.removeGame(createdGame.getId());
        assertThrows(GameException.class, () -> gameService.getGame(createdGame.getId()));
    }

    @Test
    void testRemoveGameWithNullId() {
        assertThrows(GameException.class, () -> gameService.removeGame(null));
    }

    @Test
    void testRemoveNonExistentGame() {
        assertThrows(GameException.class, () -> gameService.removeGame("non-existent-id"));
    }

    @Test
    void testAddPlayerToGame() throws GameException {
        GameDTO createdGame = gameService.createGame(gameDTO);
        Player player = new Player("TestPlayer", "123",null, 0, 0, PLAYER_RADIUS);
        GameDTO gameDTO = gameService.addPlayerToGame(createdGame.getId(), player);
        List<PlayerDTO> players = gameDTO.getPlayers();
        assertNotNull(players);
        assertEquals(1, players.size());
        assertEquals("TestPlayer", players.get(0).getName());
    }

    @Test
    void testAddPlayerToNonExistentGame() {
        Player player = new Player("TestPlayer","123", null, 0, 0, PLAYER_RADIUS);
        assertThrows(GameException.class, () -> gameService.addPlayerToGame("non-existent-id", player));
    }

    @Test
    void testRemovePlayerFromGame() throws GameException {
        GameDTO createdGame = gameService.createGame(gameDTO);
        Player player = new Player("TestPlayer", "123",null, 0, 0, PLAYER_RADIUS);
        gameService.addPlayerToGame(createdGame.getId(), player);
        List<Player> players = gameService.removePlayerFromGame(createdGame.getId(), player);
        assertNotNull(players);
        assertTrue(players.isEmpty());
    }

    @Test
    void testRemovePlayerFromGameWithNullId() {
        Player player = new Player("TestPlayer", "123",null, 0, 0, PLAYER_RADIUS);
        assertThrows(GameException.class, () -> gameService.removePlayerFromGame(null, player));
    }

    @Test
    void testRemovePlayerFromNonExistentGame() {
        Player player = new Player("TestPlayer", "123",null, 0, 0, PLAYER_RADIUS);
        assertThrows(GameException.class, () -> gameService.removePlayerFromGame("non-existent-id", player));
    }

    @Test
    void testRemovePlayerFromGameByName() throws GameException {
        GameDTO createdGame = gameService.createGame(gameDTO);
        Player player = new Player("TestPlayer", "123",null, 0, 0, PLAYER_RADIUS);
        gameService.addPlayerToGame(createdGame.getId(), player);
        List<Player> players = gameService.removePlayerFromGame(createdGame.getId(), "TestPlayer");
        assertNotNull(players);
        assertTrue(players.isEmpty());
    }

    @Test
    void testRemovePlayerFromGameByNameWithNullId() {
        assertThrows(GameException.class, () -> gameService.removePlayerFromGame(null, "TestPlayer"));
    }

    @Test
    void testRemovePlayerFromGameByNameWithNonExistentGame() {
        assertThrows(GameException.class, () -> gameService.removePlayerFromGame("non-existent-id", "TestPlayer"));
    }

    @Test
    void testGetPlayersFromGame() throws GameException {
        GameDTO createdGame = gameService.createGame(gameDTO);
        Player player = new Player("TestPlayer", "123",null, 0, 0, PLAYER_RADIUS);
        gameService.addPlayerToGame(createdGame.getId(), player);
        List<Player> players = gameService.getPlayersFromGame(createdGame.getId());
        assertNotNull(players);
        assertEquals(1, players.size());
        assertEquals("TestPlayer", players.get(0).getName());
    }

    @Test
    void testGetPlayersFromGameWithNullId() {
        assertThrows(GameException.class, () -> gameService.getPlayersFromGame(null));
    }

    @Test
    void testGetPlayersFromNonExistentGame() {
        assertThrows(GameException.class, () -> gameService.getPlayersFromGame("non-existent-id"));
    }

    @Test
    void testStartGame() throws GameException {
        GameDTO createdGame = gameService.createGame(gameDTO);
        Player player = new Player("TestPlayer", "123",null, 0, 0, PLAYER_RADIUS);
        gameService.addPlayerToGame(createdGame.getId(), player);
        GameDTO startedGame = gameService.startGame(createdGame.getId());
        assertNotNull(startedGame);
        assertEquals(createdGame.getId(), startedGame.getId());
    }

    @Test
    void testStartGameWithNullId() {
        assertThrows(GameException.class, () -> gameService.startGame(null));
    }

    @Test
    void testStartNonExistentGame() {
        assertThrows(GameException.class, () -> gameService.startGame("non-existent-id"));
    }

    @Test
    void testMakeMoveInGame() throws GameException {
        GameDTO createdGame = gameService.createGame(gameDTO);
        Player player = new Player("TestPlayer", "123",null, 0, 0, PLAYER_RADIUS);
        gameService.addPlayerToGame(createdGame.getId(), player);
        Movement movement = new Movement("TestPlayer", 1, 1, false);
        assertDoesNotThrow(() -> gameService.makeMoveInGame(createdGame.getId(), movement));
    }

    @Test
    void testMakeMoveInGameWithNullId() {
        Movement movement = new Movement("TestPlayer", 1, 1, false);
        assertThrows(GameException.class, () -> gameService.makeMoveInGame(null, movement));
    }

    @Test
    void testMakeMoveInNonExistentGame() {
        Movement movement = new Movement("TestPlayer", 1, 1, false);
        assertThrows(GameException.class, () -> gameService.makeMoveInGame("non-existent-id", movement));
    }

    @Test
    void testMakeMoveInGameWithNullPlayer() throws GameException {
        GameDTO createdGame = gameService.createGame(gameDTO);
        Movement movement = new Movement(null, 1, 1, false);
        assertThrows(GameException.class, () -> gameService.makeMoveInGame(createdGame.getId(), movement));
    }
}