package co.edu.eci.pigball.game;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import co.edu.eci.pigball.game.model.Game;
import co.edu.eci.pigball.game.model.Player;

public class GameTest {
    private Game game;
    private Player player1;
    private Player player2;

    @BeforeEach
    void setUp() {
        game = new Game();
        player1 = new Player("player1", 0, 0);
        player2 = new Player("player2", 10, 10);
    }

    @Test
    void testAddPlayer() {
        game.addPlayer("player1", player1);
        assertEquals(1, game.getAllPlayers().size());
        assertTrue(game.getAllPlayers().contains(player1));
        player1.setX(20);
        assertEquals(20, player1.getX());
    }

    @Test
    void testGetAllPlayers() {
        game.addPlayer("player1", player1);
        game.addPlayer("player2", player2);
        List<Player> players = game.getAllPlayers();
        assertEquals(2, players.size());
        assertTrue(players.contains(player1));
        assertTrue(players.contains(player2));
    }

    @Test
    void testStartGame() {
        game.addPlayer("player1", player1);
        game.addPlayer("player2", player2);
        game.startGame();
        assertNotNull(game.getGameDTO());
        assertEquals(2, game.getGameDTO().getPlayers().size());
    }

    @Test
    void testMakeAMove() {
        game.addPlayer("player1", player1);
        game.makeAMove("player1", 2, 3);
        assertEquals(10, player1.getX()); // 2 * 5
        assertEquals(15, player1.getY()); // 3 * 5
    }

    @Test
    void testMakeMoveForNewPlayer() {
        game.makeAMove("newPlayer", 1, 1);
        Player newPlayer = game.getPlayers().get("newPlayer");
        assertNotNull(newPlayer);
        assertEquals(5, newPlayer.getX()); // 1 * 5
        assertEquals(5, newPlayer.getY()); // 1 * 5
    }

    @Test
    void testPlayerMovement() {
        player1.moveInX(5);
        player1.moveInY(-5);
        assertEquals(5, player1.getX());
        assertEquals(-5, player1.getY());
    }

    @Test
    void testPlayerThreadExecution() {
        Thread thread = new Thread(player1);
        thread.start();
        try {
            thread.join(); // Esperar a que el hilo termine
        } catch (InterruptedException e) {
            fail("Thread execution interrupted");
        }
    }
}
