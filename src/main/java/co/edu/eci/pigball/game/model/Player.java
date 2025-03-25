package co.edu.eci.pigball.game.model;

import java.util.concurrent.atomic.AtomicInteger;

import lombok.Getter;

@Getter
public class Player {
    private String name;
    private String sessionId;
    private Integer team;
    private AtomicInteger x;
    private AtomicInteger y;

    public Player(String name, String sessionId,int x, int y) {
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

    public void setX(int x) {
        this.x.set(x);
    }

    public void setY(int y) {
        this.y.set(y);
    }

    public void moveInX(int dx) {
        x.addAndGet(dx);
    }

    public void moveInY(int dy) {
        y.addAndGet(dy);
    }

    public int getX() {
        return x.get();
    }

    public int getY() {
        return y.get();
    }
}
