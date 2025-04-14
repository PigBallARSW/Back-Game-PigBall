package co.edu.eci.pigball.game.model.entity.impl;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import co.edu.eci.pigball.game.java.Pair;
import co.edu.eci.pigball.game.model.entity.Entity;

class PlayerTest {

    private Player player;
    private static final int BORDER_X = 1200;
    private static final int BORDER_Y = 900;
    private static final double PLAYER_RADIUS = 20.0;

    @BeforeEach
    void setUp() {
        player = new Player("TestPlayer", "session123", 100, 100, PLAYER_RADIUS);
    }

    @Test
    void testPlayerInitialization() {
        assertEquals("TestPlayer", player.getName());
        assertEquals("session123", player.getSessionId());
        assertEquals(100, player.getX());
        assertEquals(100, player.getY());
        assertNull(player.getTeam());
        assertEquals(PLAYER_RADIUS, player.getRadius());
    }

    @Test
    void testSetPosition() {
        Pair<Double, Double> newPosition = new Pair<>(200.0, 200.0);
        List<Entity> otherPlayers = new ArrayList<>();
        
        player.setPosition(BORDER_X, BORDER_Y, newPosition, otherPlayers);
        
        assertEquals(200.0, player.getX());
        assertEquals(200.0, player.getY());
    }

    @Test
    void testSetPositionWithCollision() {
        Player otherPlayer = new Player("OtherPlayer", "session456", 150, 150, PLAYER_RADIUS);
        List<Entity> otherPlayers = new ArrayList<>();
        otherPlayers.add(otherPlayer);
        System.out.println(player.getX()+" "+player.getY());
        System.out.println(otherPlayer.getX()+" "+otherPlayer.getY());
        Pair<Double, Double> newPosition = new Pair<>(140.0, 140.0);
        player.setPosition(BORDER_X, BORDER_Y, newPosition, otherPlayers);
        System.out.println(player.getX()+" "+player.getY());
        System.out.println(otherPlayer.getX()+" "+otherPlayer.getY());
        // Position should be adjusted to avoid collision
        assertTrue(Math.abs(player.getX() - 145.0) > PLAYER_RADIUS || 
                  Math.abs(player.getY() - 145.0) > PLAYER_RADIUS);
    }

    @Test
    void testSetPositionWithBoundaries() {
        Pair<Double, Double> positionOutside = new Pair<>(-10.0, -10.0);
        List<Entity> otherPlayers = new ArrayList<>();
        
        player.setPosition(BORDER_X, BORDER_Y, positionOutside, otherPlayers);
        
        // Position should be adjusted to stay within boundaries
        assertTrue(player.getX() >= PLAYER_RADIUS);
        assertTrue(player.getY() >= PLAYER_RADIUS);
        assertTrue(player.getX() <= BORDER_X - PLAYER_RADIUS);
        assertTrue(player.getY() <= BORDER_Y - PLAYER_RADIUS);
    }

    @Test
    void testMove() {
        Pair<Double, Double> movement = new Pair<>(50.0, 50.0);
        List<Entity> otherPlayers = new ArrayList<>();
        
        player.move(BORDER_X, BORDER_Y, movement, otherPlayers);
        
        assertEquals(150.0, player.getX());
        assertEquals(150.0, player.getY());
    }

    @Test
    void testMoveWithCollision() {
        Player otherPlayer = new Player("OtherPlayer", "session456", 160, 160, PLAYER_RADIUS);
        List<Entity> otherPlayers = new ArrayList<>();
        otherPlayers.add(otherPlayer);
        
        Pair<Double, Double> movement = new Pair<>(55.0, 55.0);
        player.move(BORDER_X, BORDER_Y, movement, otherPlayers);
        // Position should be adjusted to avoid collision
        assertTrue(Math.abs(player.getX() - 155.0) > PLAYER_RADIUS || 
                  Math.abs(player.getY() - 155.0) > PLAYER_RADIUS);
    }

    @Test
    void testMoveWithBoundaries() {
        Pair<Double, Double> movement = new Pair<>(BORDER_X + 100.0, BORDER_Y + 100.0);
        List<Entity> otherPlayers = new ArrayList<>();
        
        player.move(BORDER_X, BORDER_Y, movement, otherPlayers);
        
        // Position should be adjusted to stay within boundaries
        assertTrue(player.getX() >= PLAYER_RADIUS);
        assertTrue(player.getY() >= PLAYER_RADIUS);
        assertTrue(player.getX() <= BORDER_X - PLAYER_RADIUS);
        assertTrue(player.getY() <= BORDER_Y - PLAYER_RADIUS);
    }

    @Test
    void testSetTeam() {
        player.setTeam(1);
        assertEquals(1, player.getTeam());
    }

    @Test
    void testSetRadius() {
        double newRadius = 40.0;
        player.setRadius(newRadius);
        assertEquals(newRadius, player.getRadius());
    }
} 