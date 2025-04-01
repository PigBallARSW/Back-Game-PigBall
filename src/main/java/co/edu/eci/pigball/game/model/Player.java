package co.edu.eci.pigball.game.model;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import co.edu.eci.pigball.game.java.Pair;
import lombok.Getter;

@Getter
public class Player {
    private String name;
    private String sessionId;
    private Integer team;
    private AtomicInteger x;
    private AtomicInteger y;

    private static final int RADIUS = 20;

    public Player(String name, String sessionId, int x, int y) {
        this.name = name;
        this.sessionId = sessionId;
        this.x = new AtomicInteger(x);
        this.y = new AtomicInteger(y);
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public void setTeam(Integer team) {
        this.team = team;
    }

    public void setPosition(int borderX, int borderY, int x, int y) {
        Pair<Integer, Integer> newCoordinates = new Pair<>(x, y);
        Pair<Integer, Integer> validatedCoordinates = validateCoordinates(borderX, borderY, newCoordinates);
        this.x.set(validatedCoordinates.getFirst());
        this.y.set(validatedCoordinates.getSecond());
    }

    public void move(int borderX, int borderY, int dx, int dy) {
        Pair<Integer, Integer> newCoordinates = new Pair<>(x.get() + dx, y.get() + dy);
        Pair<Integer, Integer> validatedCoordinates = validateCoordinates(borderX, borderY, newCoordinates);
        x.set(validatedCoordinates.getFirst());
        y.set(validatedCoordinates.getSecond());
    }

    public void setPosition(int borderX, int borderY, int x, int y, List<Player> players) {
        Pair<Integer, Integer> newCoordinates = new Pair<>(x, y);
        Pair<Integer, Integer> validatedCoordinates = validateCoordinates(borderX, borderY, newCoordinates, players);
        this.x.set(validatedCoordinates.getFirst());
        this.y.set(validatedCoordinates.getSecond());
    }

    public void move(int borderX, int borderY, int dx, int dy, List<Player> players) {
        Pair<Integer, Integer> newCoordinates = new Pair<>(x.get() + dx, y.get() + dy);
        Pair<Integer, Integer> validatedCoordinates = validateCoordinates(borderX, borderY, newCoordinates, players);
        x.set(validatedCoordinates.getFirst());
        y.set(validatedCoordinates.getSecond());
    }

    public int getX() {
        return x.get();
    }

    public int getY() {
        return y.get();
    }

    public Pair<Integer, Integer> validateCoordinates(int borderX, int borderY, Pair<Integer, Integer> coordinates) {
        int newX = coordinates.getFirst();
        int newY = coordinates.getSecond();

        // Check if the player is colliding with the walls
        Pair<Integer, Integer> validatedCoordinates = new Pair<>(this.x.get(), this.y.get());
        int minX = RADIUS;
        int maxX = borderX - RADIUS;
        validatedCoordinates.setFirst(Math.clamp(newX, minX, maxX));
        int minY = RADIUS;
        int maxY = borderY - RADIUS;
        validatedCoordinates.setSecond(Math.clamp(newY, minY, maxY));
        return validatedCoordinates;
    }

    public Pair<Integer, Integer> validateCoordinates(int borderX, int borderY, Pair<Integer, Integer> coordinates,
            List<Player> players) {
        int newX = coordinates.getFirst();
        int newY = coordinates.getSecond();

        // Check for collisions with other players
        for (Player player : players) {
            if (player != this) {
                int px = player.getX();
                int py = player.getY();
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
        Pair<Integer, Integer> validatedCoordinates = new Pair<>(this.x.get(), this.y.get());
        int minX = RADIUS;
        int maxX = borderX - RADIUS;
        validatedCoordinates.setFirst(Math.clamp(newX, minX, maxX));
        int minY = RADIUS;
        int maxY = borderY - RADIUS;
        validatedCoordinates.setSecond(Math.clamp(newY, minY, maxY));

        return validatedCoordinates;
    }
}
