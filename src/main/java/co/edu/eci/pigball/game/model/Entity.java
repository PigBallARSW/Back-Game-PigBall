package co.edu.eci.pigball.game.model;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import co.edu.eci.pigball.game.java.Pair;

public abstract class Entity {
    private AtomicReference<Double> x;
    private AtomicReference<Double> y;

    public Entity(double x, double y) {
        this.x = new AtomicReference<>(x);
        this.y = new AtomicReference<>(y);
    }

    public double getX() {
        return x.get();
    }

    public double getY() {
        return y.get();
    }

    public void setX(double x) {
        this.x.set(x);
    }

    public void setY(double y) {
        this.y.set(y);
    }

    public abstract void setPosition(int borderX, int borderY, Pair<Double, Double> coordinates, List<Player> players);
    public abstract void move(int borderX, int borderY, Pair<Double, Double> movement, List<Player> players);

    public abstract Pair<Double, Double> validateCoordinates(int borderX, int borderY, Pair<Double, Double> coordinates, List<Player> players);
}
