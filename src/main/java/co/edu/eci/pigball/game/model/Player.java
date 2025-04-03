package co.edu.eci.pigball.game.model;

import java.util.List;

import co.edu.eci.pigball.game.java.Pair;
import lombok.Getter;

@Getter
public class Player extends Entity {
    private String name;
    private String sessionId;
    private Integer team;
    public static final int RADIUS = 20;

    public Player(String name, String sessionId, int x, int y) {
        super(x, y);
        this.name = name;
        this.sessionId = sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public void setTeam(Integer team) {
        this.team = team;
    }

    public void setPosition(int borderX, int borderY, Pair<Double, Double> coordinates, List<Player> players) {
        Pair<Double, Double> validatedCoordinates = validateCoordinates(borderX, borderY, coordinates, players);
        super.setX(validatedCoordinates.getFirst());
        super.setY(validatedCoordinates.getSecond());
    }

    public void move(int borderX, int borderY, Pair<Double, Double> movement, List<Player> players) {
        Pair<Double, Double> newCoordinates = new Pair<>(super.getX() + movement.getFirst(), super.getY() + movement.getSecond());
        Pair<Double, Double> validatedCoordinates = validateCoordinates(borderX, borderY, newCoordinates, players);
        super.setX(validatedCoordinates.getFirst());
        super.setY(validatedCoordinates.getSecond());
    }

    public Pair<Double, Double> validateCoordinates(int borderX, int borderY, Pair<Double, Double> coordinates,
            List<Player> players) {
        double newX = coordinates.getFirst();
        double newY = coordinates.getSecond();

        // Check for collisions with other players
        for (Player player : players) {
            if (player != this) {
                double px = player.getX();
                double py = player.getY();
                double distance = Math.sqrt(Math.pow((double)px - newX, 2) + Math.pow((double)py - newY, 2));
                if (distance < 2 * RADIUS) {
                    // Stop the movement as close as possible to the other player
                    double angle = Math.atan2((double) newY - py, (double) newX - px);
                    newX = px + (int) (Math.cos(angle) * 2 * RADIUS);
                    newY = py + (int) (Math.sin(angle) * 2 * RADIUS);
                    break;
                }
            }
        }

        // Check if the player is colliding with the walls
        Pair<Double, Double> validatedCoordinates = new Pair<>(super.getX(), super.getY());
        double minX = RADIUS;
        double maxX = borderX - RADIUS;
        validatedCoordinates.setFirst(Math.clamp(newX, minX, maxX));
        double minY = RADIUS;
        double maxY = borderY - RADIUS;
        validatedCoordinates.setSecond(Math.clamp(newY, minY, maxY));

        return validatedCoordinates;
    }
}
