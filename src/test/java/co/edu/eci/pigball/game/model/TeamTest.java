package co.edu.eci.pigball.game.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TeamTest {
    private Team team;

    @BeforeEach
    void setUp() {
        team = new Team();
    }

    @Test
    void testInitialState() {
        assertEquals(0, team.getScore());
        assertEquals(0, team.getPlayers());
    }

    @Test
    void testScoreOperations() {
        // Test initial score
        assertEquals(0, team.getScore());

        // Test increasing score
        team.increaseScore();
        assertEquals(1, team.getScore());

        // Test multiple score increases
        team.increaseScore();
        team.increaseScore();
        assertEquals(3, team.getScore());
    }

    @Test
    void testPlayerOperations() {
        // Test initial player count
        assertEquals(0, team.getPlayers());

        // Test adding players
        assertEquals(1, team.addPlayer());
        assertEquals(2, team.addPlayer());
        assertEquals(3, team.addPlayer());
        assertEquals(3, team.getPlayers());

        // Test removing players
        assertEquals(2, team.removePlayer());
        assertEquals(1, team.removePlayer());
        assertEquals(0, team.removePlayer());
        assertEquals(0, team.getPlayers());
    }


    @Test
    void testConcurrentOperations() {
        // Test that score and player count operations are atomic
        for (int i = 0; i < 100; i++) {
            team.increaseScore();
            team.addPlayer();
        }

        assertEquals(100, team.getScore());
        assertEquals(100, team.getPlayers());

        for (int i = 0; i < 50; i++) {
            team.removePlayer();
        }

        assertEquals(50, team.getPlayers());
    }
}