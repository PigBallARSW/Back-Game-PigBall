package co.edu.eci.pigball.game;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import co.edu.eci.pigball.game.exception.GameException;
import co.edu.eci.pigball.game.model.Game;
import co.edu.eci.pigball.game.model.Player;
import co.edu.eci.pigball.game.model.DTO.GameDTO;

public class GameTest {
    private Game game;
    private Player player1;
    private Player player2;

    @BeforeEach
    void setUp() {
        game = new Game("Juego1", "Creador1", 4, false, null);
        player1 = new Player("player1", null, 0, 0, game);
        player2 = new Player("player2", null, 10, 10, game);
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
        player1.move(5, -5);
        assertEquals(5, player1.getX());
        assertEquals(-5, player1.getY());
    }
}
