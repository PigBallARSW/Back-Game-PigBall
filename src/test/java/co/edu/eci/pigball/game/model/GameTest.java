package co.edu.eci.pigball.game.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.awaitility.Awaitility.await;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import co.edu.eci.pigball.game.exception.GameException;
import co.edu.eci.pigball.game.model.dto.GameDTO;
import co.edu.eci.pigball.game.model.entity.impl.Player;
import co.edu.eci.pigball.game.model.mapper.GameMapper;
import co.edu.eci.pigball.game.utility.Pair;

@ExtendWith(MockitoExtension.class)
class GameTest {
    private Game game;
    private static final double PLAYER_RADIUS = 20.0;
    private Player player1;
    private Player player2;
    private Player player3;
    private Player player4;
    
    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @BeforeEach
    void setUp() {
        game = new Game("Juego1", "Creador1", 4, false, "normal", messagingTemplate);

        player1 = new Player("player1", "123",null, 0, 0, PLAYER_RADIUS);
        player2 = new Player("player2", "123",null, 0, 0, PLAYER_RADIUS);
        player3 = new Player("player3", "123",null, 0, 0, PLAYER_RADIUS);
        player4 = new Player("player4", "123",null, 0, 0, PLAYER_RADIUS);
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
    void testMakeAMove() throws GameException {
        try {
            game.addPlayer(player1);
            game.startGame();
        } catch (GameException e) {
            fail("Exception should not be thrown when adding players or starting game: " + e.getMessage());
        }
        // Store initial position
        double initialX = player1.getX();
        double initialY = player1.getY();

        game.updatePlayerLastMove("player1", 2, 3, false);
        game.makePlayersMoves();
        // Check that the player moved in the correct direction
        assertTrue(player1.getX() > initialX);
        assertTrue(player1.getY() > initialY);
    }

    @Test
    void testMakeMoveForNewPlayer() throws GameException {
        try {
            game.addPlayer(player1);
            game.startGame();
        } catch (GameException e) {
            fail("Exception should not be thrown when adding players or starting game: " + e.getMessage());
        }
        // Store initial position
        double initialX = player1.getX();
        double initialY = player1.getY();
        game.updatePlayerLastMove("player1", 1, 1, false);
        game.makePlayersMoves();
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
        Player player5 = new Player("player5","123", null, 0, 0, PLAYER_RADIUS);
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

    @Test
    void testGoalScoringAndScoreUpdate() throws GameException {
        // Add players and start the game
        game.addPlayer(player1);
        game.addPlayer(player2);
        game.startGame();

        // Initial score should be 0-0
        assertEquals(0, game.getTeams().getFirst().getScore());
        assertEquals(0, game.getTeams().getSecond().getScore());
        List<Player> players = new ArrayList<>();
        players.add(player1);
        players.add(player2);
        // Simulate a goal for team 0
        game.onGoalScored(0, players);
        assertEquals(1, game.getTeams().getFirst().getScore());
        assertEquals(0, game.getTeams().getSecond().getScore());

        // Simulate a goal for team 1
        game.onGoalScored(1, players);
        assertEquals(1, game.getTeams().getFirst().getScore());
        assertEquals(1, game.getTeams().getSecond().getScore());

        // Simulate another goal for team 0
        game.onGoalScored(0, players);
        assertEquals(2, game.getTeams().getFirst().getScore());
        assertEquals(1, game.getTeams().getSecond().getScore());
    }

    @Test
    void testBallPositionResetAfterGoal() throws GameException {
        // Add players and start the game
        game.addPlayer(player1);
        game.addPlayer(player2);
        game.startGame();
        
        List<Player> players = new ArrayList<>();
        players.add(player1);
        players.add(player2);
        // Simulate a goal
        game.onGoalScored(0, players);

        // Use Awaitility to wait for the ball to reset to center position
        await()
            .atMost(2, TimeUnit.SECONDS)
            .pollInterval(50, TimeUnit.MILLISECONDS)
            .until(ballIsAtCenter());

        // Ball should be back at center position
        assertEquals(game.getBorderX() / 2.0, game.getBall().getX(), 0.1);
        assertEquals(game.getBorderY() / 2.0, game.getBall().getY(), 0.1);
    }

    /**
     * Creates a Callable that checks if the ball is at the center position
     */
    private Callable<Boolean> ballIsAtCenter() {
        return () -> {
            double centerX = game.getBorderX() / 2.0;
            double centerY = game.getBorderY() / 2.0;
            double ballX = game.getBall().getX();
            double ballY = game.getBall().getY();
            
            return Math.abs(ballX - centerX) < 0.1 && Math.abs(ballY - centerY) < 0.1;
        };
    }

    @Test
    void testMultipleGoalsInSequence() throws GameException {
        // Add players and start the game
        game.addPlayer(player1);
        game.addPlayer(player2);
        game.startGame();

        List<Player> players = new ArrayList<>();
        players.add(player1);
        players.add(player2);

        // Simulate multiple goals in quick succession
        for (int i = 0; i < 5; i++) {
            game.onGoalScored(i % 2, players); // Alternate between teams
        }

        // Verify final score
        assertEquals(3, game.getTeams().getFirst().getScore());
        assertEquals(2, game.getTeams().getSecond().getScore());

        // Ball should still be at center after all goals
        assertEquals(game.getBorderX() / 2.0, game.getBall().getX());
        assertEquals(game.getBorderY() / 2.0, game.getBall().getY());
    }

    @Test
    void testGoalScoringDuringGame() throws GameException {
        // Add players and start the game
        game.addPlayer(player1);
        game.addPlayer(player2);
        game.startGame();
        
        List<Player> players = new ArrayList<>();
        players.add(player1);
        players.add(player2);
        // Verify game is in progress
        assertEquals(GameStatus.IN_PROGRESS, game.getStatus());

        // Simulate a goal
        game.onGoalScored(0, players);

        // Game should still be in progress
        assertEquals(GameStatus.IN_PROGRESS, game.getStatus());
        
        // Score should be updated
        assertEquals(1, game.getTeams().getFirst().getScore());
        assertEquals(0, game.getTeams().getSecond().getScore());
    }
}
