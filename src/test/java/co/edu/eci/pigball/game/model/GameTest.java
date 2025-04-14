package co.edu.eci.pigball.game.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import co.edu.eci.pigball.game.exception.GameException;
import co.edu.eci.pigball.game.java.Pair;
import co.edu.eci.pigball.game.model.dto.GameDTO;
import co.edu.eci.pigball.game.model.entity.impl.Player;
import co.edu.eci.pigball.game.model.mapper.GameMapper;

class GameTest {
    private Game game;
    private Player player1;
    private Player player2;
    private Player player3;
    private Player player4;

    @BeforeEach
    void setUp() {
        game = new Game("Juego1", "Creador1", 4, false, null);

        player1 = new Player("player1", null, 0, 0, 30.0);
        player2 = new Player("player2", null, 0, 0, 30.0);
        player3 = new Player("player3", null, 0, 0, 30.0);
        player4 = new Player("player4", null, 0, 0, 30.0);
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
        player1.setPosition(game.getBorderX(), game.getBorderY(), new Pair<Double,Double>(player1.getRadius(), player1.getRadius()), new ArrayList<>(game.getAllPlayers()));
        assertEquals(player1.getRadius(), player1.getX());
        assertEquals(player1.getRadius(), player1.getY());
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
        try {
            game.startGame();
        } catch (GameException e) {
            fail("Exception should not be thrown when starting game: " + e.getMessage());
        }
        assertNotNull(GameMapper.toDTO(game));
        assertEquals(2, GameMapper.toDTO(game).getPlayers().size());
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
        double initialX = player1.getX();
        double initialY = player1.getY();

        game.makeAMove("player1", 2, 3, false);
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
        double initialX = player1.getX();
        double initialY = player1.getY();
        game.makeAMove("player1", 1, 1, false);
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
        double initialX = player1.getX();
        double initialY = player1.getY();
        player1.move(game.getBorderX(), game.getBorderY(), new Pair<Double,Double>(5.0, -5.0), new ArrayList<>(game.getAllPlayers()));
        assertEquals(initialX + 5, player1.getX());
        assertEquals(initialY - 5, player1.getY());
    }

    @Test
    void testMaxPlayersExceeded() {
        // Add first 4 players without try/catch since we know it should work
        assertDoesNotThrow(() -> {
            game.addPlayer(player1);
            game.addPlayer(player2);
            game.addPlayer(player3);
            game.addPlayer(player4);
        });

        // Test adding the 5th player which should throw exception
        Player player5 = new Player("player5", null, 0, 0, 30.0 );
        GameException exception = assertThrows(GameException.class, () -> game.addPlayer(player5));
        assertEquals(GameException.EXCEEDED_MAX_PLAYERS, exception.getMessage());
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
            if (p.getTeam() == 0)
                team0Count++;
            if (p.getTeam() == 1)
                team1Count++;
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
        GameDTO gameDTO = null;
        try {
            gameDTO = game.startGame();
        } catch (GameException e) {
            fail("Exception should not be thrown when starting game: " + e.getMessage());
        }
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
        player1.move(game.getBorderX(), game.getBorderY(), new Pair<>(game.getBorderX() + 100.0, game.getBorderY() + 100.0), new ArrayList<>(game.getAllPlayers()));
        assertTrue(player1.getX() <= game.getBorderX());
        assertTrue(player1.getY() <= game.getBorderY());

        player1.move(game.getBorderX(), game.getBorderY(), new Pair<>(-100.0, -100.0), new ArrayList<>(game.getAllPlayers()));
        assertTrue(player1.getX() >= 0);
        assertTrue(player1.getY() >= 0);
    }

    @Test
    void testPlayerReconnection() {
        try {
            game.addPlayer(player1);
            player1.setPosition(game.getBorderX(), game.getBorderY(), new Pair<Double,Double>(player1.getRadius(), player1.getRadius()), new ArrayList<>(game.getAllPlayers()));
            game.addPlayer(player1); // Reconnect same player
        } catch (GameException e) {
            fail("Exception should not be thrown when reconnecting player: " + e.getMessage());
        }

        assertEquals(1, game.getAllPlayers().size());
        assertEquals(player1.getRadius(), player1.getX());
        assertEquals(player1.getRadius(), player1.getY());
    }
}
