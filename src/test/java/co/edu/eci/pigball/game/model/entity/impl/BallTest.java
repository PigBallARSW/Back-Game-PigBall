package co.edu.eci.pigball.game.model.entity.impl;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import co.edu.eci.pigball.game.model.GameObserver;
import co.edu.eci.pigball.game.model.entity.Entity;
import co.edu.eci.pigball.game.utility.Pair;

class BallTest {

    private Ball ball;
    private static final int BORDER_X = 1200;
    private static final int BORDER_Y = 900;
    private static final double BALL_RADIUS = 10.0;
    private static final int INITIAL_VELOCITY_X = 100;
    private static final int INITIAL_VELOCITY_Y = 100;

    @BeforeEach
    void setUp() {
        ball = new Ball(BORDER_X / 2, BORDER_Y / 2, INITIAL_VELOCITY_X, INITIAL_VELOCITY_Y, BALL_RADIUS);
    }

    @Test
    void testBallInitialization() {
        assertEquals(BORDER_X / 2, ball.getX());
        assertEquals(BORDER_Y / 2, ball.getY());
        assertEquals(INITIAL_VELOCITY_X, ball.getVelocityX());
        assertEquals(INITIAL_VELOCITY_Y, ball.getVelocityY());
        assertEquals(BALL_RADIUS, ball.getRadius());
    }

    @Test
    void testSetVelocity() {
        double newVelocityX = 200.0;
        double newVelocityY = 150.0;
        ball.setVelocity(newVelocityX, newVelocityY);
        
        assertEquals(newVelocityX, ball.getVelocityX());
        assertEquals(newVelocityY, ball.getVelocityY());
    }

    @Test
    void testMove() {
        Pair<Double, Double> movement = new Pair<>(50.0, 50.0);
        List<Entity> entities = new ArrayList<>();
        
        ball.move(BORDER_X, BORDER_Y, movement, entities);
        
        assertEquals(BORDER_X / 2 + 50.0, ball.getX());
        assertEquals(BORDER_Y / 2 + 50.0, ball.getY());
    }

    @Test
    void testMoveWithCollision() {
        Player player = new Player("TestPlayer", "session123", BORDER_X / 2, BORDER_Y / 2 + 25, 10.0);
        List<Entity> entities = new ArrayList<>();
        entities.add(player);
        Pair<Double, Double> position = new Pair<>(BORDER_X / 2.0, BORDER_Y / 2.0);
        ball.setPosition(BORDER_X, BORDER_Y, position, entities);
        Pair<Double, Double> movement = new Pair<>(0.0, 10.0);
        ball.move(BORDER_X, BORDER_Y, movement, entities);
        
        boolean isInCollisionInXWithPlayer = Math.abs(ball.getX() - player.getX()) <= BALL_RADIUS;
        boolean isInCollisionInYWithPlayer = Math.abs(ball.getY() - player.getY()) <= BALL_RADIUS;
        boolean isInCollision = isInCollisionInXWithPlayer && isInCollisionInYWithPlayer;
        assertTrue(!isInCollision);
        
        // Velocity should be updated after collision
        assertNotEquals(INITIAL_VELOCITY_X, ball.getVelocityX());
        assertNotEquals(INITIAL_VELOCITY_Y, ball.getVelocityY());
    }

    @Test
    void testMoveWithWallCollision() {
        Pair<Double, Double> movement = new Pair<>((double)BORDER_X, (double)BORDER_Y);
        List<Entity> entities = new ArrayList<>();
        
        ball.move(BORDER_X, BORDER_Y, movement, entities);
        
        // Ball should not go beyond boundaries
        assertTrue(ball.getX() >= BALL_RADIUS);
        assertTrue(ball.getY() >= BALL_RADIUS);
        assertTrue(ball.getX() <= BORDER_X - BALL_RADIUS);
        assertTrue(ball.getY() <= BORDER_Y - BALL_RADIUS);
        
        // Velocity should be reversed after wall collision
        assertTrue(ball.getVelocityX() < 0 || ball.getVelocityX() > INITIAL_VELOCITY_X);
        assertTrue(ball.getVelocityY() < 0 || ball.getVelocityY() > INITIAL_VELOCITY_Y);
    }

    @Test
    void testGoalDetection() {
        // Create a mock observer to track goal notifications
        TestGameObserver observer = new TestGameObserver();
        ball.addObserver(observer);
        
        // Move ball to goal position
        List<Entity> entities = new ArrayList<>();
        Pair<Double, Double> position = new Pair<>(0.0, BORDER_Y / 2.0);
        ball.setPosition(BORDER_X, BORDER_Y, position, entities);
        Pair<Double, Double> movement = new Pair<>(-2 * BALL_RADIUS, 0.0);
        ball.move(BORDER_X, BORDER_Y, movement, entities);


        // Check if goal was detected
        assertTrue(observer.wasGoalScored());
        assertEquals(1, observer.getLastGoalTeam());
    }

    @Test
    void testObserverManagement() {
        TestGameObserver observer1 = new TestGameObserver();
        TestGameObserver observer2 = new TestGameObserver();
        
        // Add observers
        ball.addObserver(observer1);
        ball.addObserver(observer2);
        
        // Remove one observer
        ball.removeObserver(observer1);
        
        // Trigger goal
        List<Entity> entities = new ArrayList<>();
        Pair<Double, Double> movement = new Pair<>((double)-BORDER_X, 0.0);
        ball.move(BORDER_X, BORDER_Y, movement, entities);
        
        // Only observer2 should be notified
        assertFalse(observer1.wasGoalScored());
        assertTrue(observer2.wasGoalScored());
    }

    @Test
    void testVelocityLimits() {
        // Set very high velocity
        ball.setVelocity(3000.0, 3000.0);
        
        // Move ball
        List<Entity> entities = new ArrayList<>();
        Pair<Double, Double> movement = new Pair<>(100.0, 100.0);
        ball.move(BORDER_X, BORDER_Y, movement, entities);
        
        // Check if velocity was capped
        double magnitude = Math.sqrt(ball.getVelocityX() * ball.getVelocityX() + 
                                   ball.getVelocityY() * ball.getVelocityY());
        assertTrue(magnitude <= Ball.MAX_MAGNITUDE);
    }

    // Helper class to test goal notifications
    private static class TestGameObserver implements GameObserver {
        private boolean goalScored = false;
        private int lastGoalTeam = -1;
        
        @Override
        public void onGoalScored(int team, List<Player> players) {
            goalScored = true;
            lastGoalTeam = team;
        }
        
        public boolean wasGoalScored() {
            return goalScored;
        }
        
        public int getLastGoalTeam() {
            return lastGoalTeam;
        }
    }
} 