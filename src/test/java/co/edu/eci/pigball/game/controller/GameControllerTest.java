package co.edu.eci.pigball.game.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import co.edu.eci.pigball.game.exception.GameException;
import co.edu.eci.pigball.game.model.Movement;
import co.edu.eci.pigball.game.model.Player;
import co.edu.eci.pigball.game.model.dto.GameDTO;
import co.edu.eci.pigball.game.model.dto.PlayerDTO;
import co.edu.eci.pigball.game.model.Game;
import co.edu.eci.pigball.game.service.GameService;
import co.edu.eci.pigball.game.config.WebSocketEventListener;

@ExtendWith(MockitoExtension.class)
public class GameControllerTest {

    @Mock
    private GameService gameService;

    @Mock
    private WebSocketEventListener webSocketEventListener;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private SimpMessageHeaderAccessor headerAccessor;

    @Mock(lenient = true)
    private Game game;

    @InjectMocks
    private GameController gameController;

    private String gameId;
    private Player player;
    private Movement movement;
    private GameDTO gameDTO;

    @BeforeEach
    void setUp() {
        gameId = "test-game-id";
        
        // Mock the Game object
        when(game.getGameId()).thenReturn(gameId);
        when(game.getPlayers()).thenReturn(new ConcurrentHashMap<>());
        
        // Create GameDTO from the mocked Game
        gameDTO = new GameDTO(game);
        
        // Create player with the GameDTO
        player = new Player("TestPlayer", null, 0, 0, gameDTO);
        movement = new Movement("TestPlayer", 1, 1);
    }

    @Test
    void testHandleNewPlayer() throws GameException {
        when(game.getGameId()).thenReturn(gameId);
        when(headerAccessor.getSessionId()).thenReturn("test-session-id");
        when(gameService.addPlayerToGame(gameId, player)).thenReturn(List.of(player));
        
        List<PlayerDTO> result = gameController.handleNewPlayer(gameId, player, headerAccessor);
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("TestPlayer", result.get(0).getName());
        verify(webSocketEventListener).setANewConnection("test-session-id", gameId, "TestPlayer");
    }

    @Test
    void testHandleNewPlayerWithException() throws GameException {
        when(game.getGameId()).thenReturn(gameId);
        when(headerAccessor.getSessionId()).thenReturn("test-session-id");
        when(gameService.addPlayerToGame(gameId, player)).thenThrow(new GameException("Test error"));
        
        List<PlayerDTO> result = gameController.handleNewPlayer(gameId, player, headerAccessor);
        
        assertNull(result);
    }

    @Test
    void testHandlePlayerExit() throws GameException {
        when(game.getGameId()).thenReturn(gameId);
        when(gameService.removePlayerFromGame(gameId, player)).thenReturn(List.of());
        List<PlayerDTO> result = gameController.handlePlayerExit(gameId, player);
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testHandlePlayerExitWithException() throws GameException {
        when(game.getGameId()).thenReturn(gameId);
        when(gameService.removePlayerFromGame(gameId, player)).thenThrow(new GameException("Test error"));
        when(gameService.getPlayersFromGame(gameId)).thenReturn(List.of());
        
        List<PlayerDTO> result = gameController.handlePlayerExit(gameId, player);
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testStartGame() throws GameException {
        GameDTO gameDTO = new GameDTO();
        when(gameService.startGame(gameId)).thenReturn(gameDTO);
        
        GameDTO result = gameController.startGame(gameId);
        
        assertNotNull(result);
        assertEquals(gameDTO, result);
    }

    @Test
    void testStartGameWithException() throws GameException {
        when(gameService.startGame(gameId)).thenThrow(new GameException("Test error"));
        
        GameDTO result = gameController.startGame(gameId);
        
        assertNull(result);
    }

    @Test
    void testMakeAMovement() throws GameException {
        doNothing().when(gameService).makeMoveInGame(gameId, movement);
        
        assertDoesNotThrow(() -> gameController.makeAMovement(gameId, movement));
        verify(gameService).makeMoveInGame(gameId, movement);
    }

    @Test
    void testMakeAMovementWithException() throws GameException {
        doThrow(new GameException("Test error")).when(gameService).makeMoveInGame(gameId, movement);
        
        assertDoesNotThrow(() -> gameController.makeAMovement(gameId, movement));
        verify(gameService).makeMoveInGame(gameId, movement);
    }
} 