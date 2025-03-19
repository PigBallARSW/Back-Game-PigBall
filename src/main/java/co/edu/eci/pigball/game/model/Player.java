package co.edu.eci.pigball.game.model;

import java.util.concurrent.atomic.AtomicInteger;

import lombok.Getter;

@Getter
public class Player {
    private String name;
    private AtomicInteger x;
    private AtomicInteger y;

    public Player(String name, int x, int y) {
        this.name = name;
        this.x = new AtomicInteger(x);
        this.y = new AtomicInteger(y);
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
