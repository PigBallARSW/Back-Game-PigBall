package co.edu.eci.pigball.game.model;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import co.edu.eci.pigball.game.java.Pair;
import co.edu.eci.pigball.game.model.dto.GameDTO;
import lombok.Getter;

@Getter
public class Player {
    private String name;
    private String sessionId;
    private Integer team;
    private AtomicInteger x;
    private AtomicInteger y;
    private GameDTO game;  

    private static final int RADIUS = 20;

    public Player(String name, String sessionId, int x, int y, GameDTO game) {
        this.name = name;
        this.sessionId = sessionId;
        this.x = new AtomicInteger(x);
        this.y = new AtomicInteger(y);
        this.game = game;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public void setTeam(Integer team) {
        this.team = team;
    }

    public void setGame(GameDTO game) {
        this.game = game;
    }

    public void setPosition(int x, int y) {
        Pair<Integer, Integer> newCoordinates = new Pair<>(x, y);
        Pair<Integer, Integer> validatedCoordinates = validateCoordinates(newCoordinates);	
        this.x.set(validatedCoordinates.getFirst());
        this.y.set(validatedCoordinates.getSecond());
    }

    public void move(int dx, int dy) {
        Pair<Integer, Integer> newCoordinates = new Pair<>(x.get() + dx, y.get() + dy);
        Pair<Integer, Integer> validatedCoordinates = validateCoordinates(newCoordinates);
        x.set(validatedCoordinates.getFirst());
        y.set(validatedCoordinates.getSecond());
    }

    public void setPosition(int x, int y, List<Player> players) {
        Pair<Integer, Integer> newCoordinates = new Pair<>(x, y);
        Pair<Integer, Integer> validatedCoordinates = validateCoordinates(newCoordinates, players);	
        this.x.set(validatedCoordinates.getFirst());
        this.y.set(validatedCoordinates.getSecond());
    }

    public void move(int dx, int dy, List<Player> players) {
        Pair<Integer, Integer> newCoordinates = new Pair<>(x.get() + dx, y.get() + dy);
        Pair<Integer, Integer> validatedCoordinates = validateCoordinates(newCoordinates, players);
        x.set(validatedCoordinates.getFirst());
        y.set(validatedCoordinates.getSecond());
    }

    public int getX() {
        return x.get();
    }

    public int getY() {
        return y.get();
    }

    public Pair<Integer, Integer> validateCoordinates(Pair<Integer, Integer> coordinates) {
        int x = coordinates.getFirst();
        int y = coordinates.getSecond();

        // Check if the player is colliding with the walls
        Pair<Integer, Integer> validatedCoordinates = new Pair<>(this.x.get(), this.y.get());
        int borderX = game.getBorderX();
        int minX = RADIUS;
        int maxX = borderX - RADIUS;
        validatedCoordinates.setFirst(Math.max(minX, Math.min(maxX, x)));
        int borderY = game.getBorderY();
        int minY = RADIUS;
        int maxY = borderY - RADIUS;
        validatedCoordinates.setSecond(Math.max(minY, Math.min(maxY, y)));
        return validatedCoordinates;
    }

    public Pair<Integer, Integer> validateCoordinates(Pair<Integer, Integer> coordinates, List<Player> players) {
        int x = coordinates.getFirst();
        int y = coordinates.getSecond();
        
        // Check for collisions with other players
        for (Player player : players) {
            if (player == this) continue; // Skip self
            int px = player.getX();
            int py = player.getY();
            double distance = Math.sqrt(Math.pow(px - x, 2) + Math.pow(py - y, 2));
            if (distance < 2 * RADIUS) {
                // Stop the movement as close as possible to the other player
                double angle = Math.atan2(y - py, x - px);
                x = px + (int) (Math.cos(angle) * 2 * RADIUS);
                y = py + (int) (Math.sin(angle) * 2 * RADIUS);
                break;
            }
        }

        // Check if the player is colliding with the walls
        Pair<Integer, Integer> validatedCoordinates = new Pair<>(this.x.get(), this.y.get());
        int borderX = game.getBorderX();
        int minX = RADIUS;
        int maxX = borderX - RADIUS;
        validatedCoordinates.setFirst(Math.max(minX, Math.min(maxX, x)));
        int borderY = game.getBorderY();
        int minY = RADIUS;
        int maxY = borderY - RADIUS;
        validatedCoordinates.setSecond(Math.max(minY, Math.min(maxY, y)));
        
        return validatedCoordinates;
    }
}
