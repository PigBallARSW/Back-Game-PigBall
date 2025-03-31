package co.edu.eci.pigball.game.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import co.edu.eci.pigball.game.exception.GameException;
import co.edu.eci.pigball.game.model.Game;
import co.edu.eci.pigball.game.model.Player;
import co.edu.eci.pigball.game.model.dto.GameDTO;

public class GameTest {
    private Game game;
    private Player player1;
    private Player player2;
    private Player player3;
    private Player player4;

    @BeforeEach
    void setUp() {
        game = new Game("Juego1", "Creador1", 4, false, null);
        
        player1 = new Player("player1", null, 0, 0, GameDTO.toDTO(game));
        player2 = new Player("player2", null, 0, 0, GameDTO.toDTO(game));
        player3 = new Player("player3", null, 0, 0, GameDTO.toDTO(game));
        player4 = new Player("player4", null, 0, 0, GameDTO.toDTO(game));
    }

    @Test
    void testAddPlayer() {
        try {
            game.addPlayer(player1);
        } catch (GameException e) {
            fail("Exception should not be thrown when adding a player: " + e.getMessage());
        }
        assertEquals(1, game.getAllPlayers().size());
        assertTrue(game.getAllPlayers().contains(player1));
        player1.setPosition(20,20);
        assertEquals(20, player1.getX());
    }

    @Test
    void testGetAllPlayers() {
        try {
            game.addPlayer(player1);
            game.addPlayer(player2);
        } catch (GameException e) {
            fail("Exception should not be thrown when adding players: " + e.getMessage());
        }
        List<Player> players = game.getAllPlayers();
        assertEquals(2, players.size());
        assertTrue(players.contains(player1));
        assertTrue(players.contains(player2));
    }

    @Test
    void testStartGame() {
        try {
            game.addPlayer(player1);
            game.addPlayer(player2);
        } catch (GameException e) {
            fail("Exception should not be thrown when adding players: " + e.getMessage());
        }
        game.startGame();
        assertNotNull(GameDTO.toDTO(game));
        assertEquals(2, GameDTO.toDTO(game).getPlayers().size());
    }

    @Test
    void testMakeAMove() {
        try {
            game.addPlayer(player1);
            game.startGame();
        } catch (GameException e) {
            fail("Exception should not be thrown when adding players or starting game: " + e.getMessage());
        }
        // Store initial position
        int initialX = player1.getX();
        int initialY = player1.getY();
        
        game.makeAMove("player1", 2, 3);
        // Check that the player moved in the correct direction
        assertTrue(player1.getX() > initialX);
        assertTrue(player1.getY() > initialY);
    }

    @Test
    void testMakeMoveForNewPlayer() {
        try {
            game.addPlayer(player1);
            game.startGame();
        } catch (GameException e) {
            fail("Exception should not be thrown when adding players or starting game: " + e.getMessage());
        }
        // Store initial position
        int initialX = player1.getX();
        int initialY = player1.getY();
        game.makeAMove("player1", 1, 1);
        Player movedPlayer = game.getPlayers().get("player1");
        assertNotNull(movedPlayer);
        // Validate that the player moved in the correct direction
        assertTrue(movedPlayer.getX() > initialX);
        assertTrue(movedPlayer.getY() > initialY);
    }

    @Test
    void testPlayerMovement() {
        try {
            game.addPlayer(player1);
        } catch (GameException e) {
            fail("Exception should not be thrown when adding a player: " + e.getMessage());
        }
        int initialX = player1.getX();
        int initialY = player1.getY();
        player1.move(5, -5);
        assertEquals(initialX + 5, player1.getX());
        assertEquals(initialY - 5, player1.getY());
    }

    @Test
    void testMaxPlayersExceeded() {
        try {
            game.addPlayer(player1);
            game.addPlayer(player2);
            game.addPlayer(player3);
            game.addPlayer(player4);
            game.addPlayer(new Player("player5", null, 0, 0, GameDTO.toDTO(game)));
            fail("Should throw GameException when exceeding max players");
        } catch (GameException e) {
            assertEquals(GameException.EXCEEDED_MAX_PLAYERS, e.getMessage());
        }
    }

    @Test
    void testInvalidTeamAssignment() {
        try {
            player1.setTeam(2); // Invalid team number
            game.addPlayer(player1);
            fail("Should throw GameException for invalid team");
        } catch (GameException e) {
            assertEquals(GameException.INVALID_TEAM, e.getMessage());
        }
    }

    @Test
    void testPlayerRemoval() {
        try {
            game.addPlayer(player1);
            game.addPlayer(player2);
        } catch (GameException e) {
            fail("Exception should not be thrown when adding players: " + e.getMessage());
        }
        
        game.removePlayer(player1);
        assertEquals(1, game.getAllPlayers().size());
        assertFalse(game.getAllPlayers().contains(player1));
        
        game.removePlayer("player2");
        assertEquals(0, game.getAllPlayers().size());
    }

    @Test
    void testTeamAssignment() {
        try {
            game.addPlayer(player1);
            game.addPlayer(player2);
            game.addPlayer(player3);
        } catch (GameException e) {
            fail("Exception should not be thrown when adding players: " + e.getMessage());
        }
        
        // Check team assignments
        assertNotNull(player1.getTeam());
        assertNotNull(player2.getTeam());
        assertNotNull(player3.getTeam());
        
        // Check team balance
        int team0Count = 0;
        int team1Count = 0;
        for (Player p : game.getAllPlayers()) {
            if (p.getTeam() == 0) team0Count++;
            if (p.getTeam() == 1) team1Count++;
        }
        assertTrue(Math.abs(team0Count - team1Count) <= 1);
    }

    @Test
    void testGameStatusTransitions() {
        assertEquals(GameStatus.WAITING_FOR_PLAYERS, game.getStatus());
        
        try {
            game.addPlayer(player1);
            game.addPlayer(player2);
        } catch (GameException e) {
            fail("Exception should not be thrown when adding players: " + e.getMessage());
        }
        
        // Start game and verify initial state
        GameDTO gameDTO = game.startGame();
        assertNotNull(gameDTO);
        
        // Verify game state after starting
        assertTrue(game.getStatus() == GameStatus.STARTING || game.getStatus() == GameStatus.IN_PROGRESS);
        
        // Verify game data
        assertEquals(2, gameDTO.getPlayers().size());
        assertNotNull(gameDTO.getId());
        assertEquals("Juego1", gameDTO.getGameName());
        assertEquals("Creador1", gameDTO.getCreatorName());
        assertEquals(4, gameDTO.getMaxPlayers());
        assertFalse(gameDTO.isPrivateGame());
    }

    @Test
    void testBorderConstraints() {
        try {
            game.addPlayer(player1);
        } catch (GameException e) {
            fail("Exception should not be thrown when adding a player: " + e.getMessage());
        }
        
        // Test movement beyond borders
        player1.move(game.getBorderX() + 100, game.getBorderY() + 100);
        assertTrue(player1.getX() <= game.getBorderX());
        assertTrue(player1.getY() <= game.getBorderY());
        
        player1.move(-100, -100);
        assertTrue(player1.getX() >= 0);
        assertTrue(player1.getY() >= 0);
    }

    @Test
    void testPlayerReconnection() {
        try {
            game.addPlayer(player1);
            player1.setPosition(20, 20);
            game.addPlayer(player1); // Reconnect same player
        } catch (GameException e) {
            fail("Exception should not be thrown when reconnecting player: " + e.getMessage());
        }
        
        assertEquals(1, game.getAllPlayers().size());
        assertEquals(20, player1.getX());
        assertEquals(20, player1.getY());
    }
}
