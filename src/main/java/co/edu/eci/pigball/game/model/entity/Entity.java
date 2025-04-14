package co.edu.eci.pigball.game.model.entity;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import co.edu.eci.pigball.game.java.Pair;

public abstract class Entity {
    private AtomicReference<Double> x;
    private AtomicReference<Double> y;
    protected double radius;

    protected Entity(double x, double y, double radius) {
        this.x = new AtomicReference<>(x);
        this.y = new AtomicReference<>(y);
        this.radius = radius;
    }

    public double getX() {
        return x.get();
    }

    public double getY() {
        return y.get();
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public void setX(double x) {
        this.x.set(x);
    }

    public void setY(double y) {
        this.y.set(y);
    }

    public void setPosition(int borderX, int borderY, Pair<Double, Double> coordinates, List<Entity> entities){
        Pair<Double, Double> validatedCoordinates = validateCoordinates(borderX, borderY, coordinates, entities);
        setX(validatedCoordinates.getFirst());
        setY(validatedCoordinates.getSecond());
    }

    public void move(int borderX, int borderY, Pair<Double, Double> movement, List<Entity> entities){
        Pair<Double, Double> newCoordinates = new Pair<>(getX() + movement.getFirst(), getY() + movement.getSecond());
        Pair<Double, Double> validatedCoordinates = validateCoordinates(borderX, borderY, newCoordinates, entities);
        setX(validatedCoordinates.getFirst());
        setY(validatedCoordinates.getSecond());
    }

    public abstract Pair<Double, Double> validateCoordinates(int borderX, int borderY, Pair<Double, Double> coordinates, List<Entity> entities);
}
