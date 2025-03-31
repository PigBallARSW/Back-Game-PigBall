package co.edu.eci.pigball.game.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import co.edu.eci.pigball.game.exception.GameException;
import co.edu.eci.pigball.game.model.temp_change.GameDTO;
import co.edu.eci.pigball.game.service.GameService;

@ExtendWith(MockitoExtension.class)
public class LobbyControllerTest {

    @Mock
    private GameService gameService;

    @InjectMocks
    private LobbyController lobbyController;

    private GameDTO gameDTO;
    private String gameId;

    @BeforeEach
    void setUp() {
        gameDTO = new GameDTO();
        gameDTO.setGameName("Test Game");
        gameDTO.setCreatorName("Test Creator");
        gameDTO.setMaxPlayers(4);
        gameDTO.setPrivateGame(false);
        gameId = "test-game-id";
    }

    @Test
    void testCreateGameSuccess() throws GameException {
        when(gameService.createGame(gameDTO)).thenReturn(gameDTO);

        ResponseEntity<?> response = lobbyController.createGame(gameDTO);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(gameDTO, response.getBody());
    }

    @Test
    void testCreateGameFailure() throws GameException {
        when(gameService.createGame(gameDTO)).thenThrow(new GameException("Test error"));

        ResponseEntity<?> response = lobbyController.createGame(gameDTO);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Test error", response.getBody());
    }

    @Test
    void testGetGameSuccess() throws GameException {
        when(gameService.getGame(gameId)).thenReturn(gameDTO);

        ResponseEntity<?> response = lobbyController.getGame(gameId);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(gameDTO, response.getBody());
    }

    @Test
    void testGetGameFailure() throws GameException {
        when(gameService.getGame(gameId)).thenThrow(new GameException("Test error"));

        ResponseEntity<?> response = lobbyController.getGame(gameId);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Test error", response.getBody());
    }

    @Test
    void testGetAllGamesSuccess() {
        Collection<GameDTO> games = List.of(gameDTO);
        when(gameService.getAllGames()).thenReturn(games);

        ResponseEntity<?> response = lobbyController.getAllGames();

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(games, response.getBody());
    }

    @Test
    void testRemoveGameSuccess() throws GameException {
        doNothing().when(gameService).removeGame(gameId);

        ResponseEntity<?> response = lobbyController.removeGame(gameId);

        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(gameService).removeGame(gameId);
    }

    @Test
    void testRemoveGameFailure() throws GameException {
        doThrow(new GameException("Test error")).when(gameService).removeGame(gameId);

        ResponseEntity<?> response = lobbyController.removeGame(gameId);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Test error", response.getBody());
        verify(gameService).removeGame(gameId);
    }

    @Test
    void testCreateGameWithEmptyName() throws GameException {
        gameDTO.setGameName("");
        when(gameService.createGame(gameDTO)).thenThrow(new GameException(GameException.NOT_EMPTY_NAME));

        ResponseEntity<?> response = lobbyController.createGame(gameDTO);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(GameException.NOT_EMPTY_NAME, response.getBody());
    }

    @Test
    void testGetGameWithNullId() throws GameException {
        when(gameService.getGame(null)).thenThrow(new GameException(GameException.NOT_EMPTY_ID));

        ResponseEntity<?> response = lobbyController.getGame(null);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(GameException.NOT_EMPTY_ID, response.getBody());
    }

    @Test
    void testGetGameWithNonExistentId() throws GameException {
        when(gameService.getGame("non-existent-id")).thenThrow(new GameException(GameException.GAME_NOT_FOUND));

        ResponseEntity<?> response = lobbyController.getGame("non-existent-id");

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(GameException.GAME_NOT_FOUND, response.getBody());
    }

    @Test
    void testRemoveGameWithNullId() throws GameException {
        doThrow(new GameException(GameException.NOT_EMPTY_ID)).when(gameService).removeGame(null);

        ResponseEntity<?> response = lobbyController.removeGame(null);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(GameException.NOT_EMPTY_ID, response.getBody());
    }

    @Test
    void testRemoveGameWithNonExistentId() throws GameException {
        doThrow(new GameException(GameException.GAME_NOT_FOUND)).when(gameService).removeGame("non-existent-id");

        ResponseEntity<?> response = lobbyController.removeGame("non-existent-id");

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(GameException.GAME_NOT_FOUND, response.getBody());
    }
} 