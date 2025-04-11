package co.edu.eci.pigball.game.model;

import java.util.List;
import java.util.ArrayList;

import co.edu.eci.pigball.game.java.Pair;

public class Ball extends Entity {

    public static final double RADIUS = 20.0;
    // Variables para almacenar la dirección de movimiento
    private double velocityX = 0;
    private double velocityY = 0;
    private List<GameObserver> observers;
    private int lastGoalTeam = -1;

    public Ball(int x, int y, int velocityX, int velocityY) {
        super(x, y);
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.observers = new ArrayList<>();
    }

    public void addObserver(GameObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(GameObserver observer) {
        observers.remove(observer);
    }

    private void notifyGoalScored(int team) {
        if (team != lastGoalTeam) { // Prevent multiple notifications for the same goal
            lastGoalTeam = team;
            for (GameObserver observer : observers) {
                observer.onGoalScored(team);
            }
        }
    }

    @Override
    public void setPosition(int borderX, int borderY, Pair<Double, Double> coordinates, List<Player> players) {
        Pair<Double, Double> validatedCoordinates = validateCoordinates(borderX, borderY, coordinates, players);
        super.setX(validatedCoordinates.getFirst());
        super.setY(validatedCoordinates.getSecond());
    }

    @Override
    public void move(int borderX, int borderY, Pair<Double, Double> movement, List<Player> players) {
        Pair<Double, Double> newCoordinates = new Pair<>(super.getX() + movement.getFirst(),
                super.getY() + movement.getSecond());
        Pair<Double, Double> validatedCoordinates = validateCoordinates(borderX, borderY, newCoordinates, players);
        super.setX(validatedCoordinates.getFirst());
        super.setY(validatedCoordinates.getSecond());
    }

    @Override
    public Pair<Double, Double> validateCoordinates(int borderX, int borderY, Pair<Double, Double> coordinates,
            List<Player> players) {
        double newX = coordinates.getFirst();
        double newY = coordinates.getSecond();
        double collisionPlayerBoost = 1.3;
        double collisionWallBoost = 0.9;

        // Verificar colisiones con jugadores
        for (Player player : players) {
            double playerX = player.getX();
            double playerY = player.getY();

            // Calcular la distancia entre la pelota y el jugador
            double distance = Math.sqrt(Math.pow(playerX - newX, 2) + Math.pow(playerY - newY, 2));

            // Si hay colisión (la distancia es menor que la suma de los radios)
            if (distance < RADIUS + Player.RADIUS) {
                // Calcular el ángulo de colisión
                double angle = Math.atan2(newY - playerY, newX - playerX);

                // Calcular la magnitud actual y aumentar la velocidad
                double magnitude = Math.sqrt(velocityX * velocityX + velocityY * velocityY);
                if (magnitude < 1) {
                    // Si la pelota está en reposo, darle un impulso inicial
                    magnitude = 150.0;
                }
                magnitude *= collisionPlayerBoost;

                // Asignar nueva dirección basada en el ángulo de colisión con velocidad
                // aumentada
                velocityX = Math.cos(angle) * magnitude;
                velocityY = Math.sin(angle) * magnitude;

                // Actualizar posición para evitar solapamiento con el jugador
                newX = playerX + (Math.cos(angle) * (RADIUS + Player.RADIUS));
                newY = playerY + (Math.sin(angle) * (RADIUS + Player.RADIUS));
                break; // Solo consideramos la primera colisión
            }
        }
        int middleY = borderY / 2;
        boolean isInXRange = newX - RADIUS <= 0 || newX + RADIUS >= borderX;
        boolean isInYRange = (newY - RADIUS > middleY - (borderY * 0.09)) && (newY + RADIUS < middleY + (borderY * 0.09));
        if (isInXRange && isInYRange) {
            if (newX - RADIUS < -2 * RADIUS) {
                newX = borderX / 2;
                newY = borderY / 2;
                notifyGoalScored(0);
            }
            else if (newX + RADIUS > borderX + 2 * RADIUS) {
                newX = borderX / 2;
                newY = borderY / 2;
                notifyGoalScored(1);
            }
            else {
            }
        }
        else {
            // Verificar colisiones con paredes (límites del campo)
            // Colisión con pared izquierda o derecha
            if (newX - RADIUS <= 0) {
                newX = RADIUS;
                velocityX = -velocityX * collisionWallBoost;
            } else if (newX + RADIUS >= borderX) {
                newX = borderX - RADIUS;
                velocityX = -velocityX * collisionWallBoost;
            }

            // Colisión con pared superior o inferior
            if (newY - RADIUS <= 0) {
                newY = RADIUS;
                velocityY = -velocityY * collisionWallBoost;
            } else if (newY + RADIUS >= borderY) {
                newY = borderY - RADIUS;
                velocityY = -velocityY * collisionWallBoost;
            }
            newX = Math.max(RADIUS, Math.min(borderX - RADIUS, newX));
            newY = Math.max(RADIUS, Math.min(borderY - RADIUS, newY));
        }

        return new Pair<>(newX, newY);
    }

    public double getVelocityX() {
        return velocityX;
    }

    public double getVelocityY() {
        return velocityY;
    }

    public void setVelocity(double velocityX, double velocityY) {
        this.velocityX = velocityX;
        this.velocityY = velocityY;
    }

    public void setLastGoalTeam(int lastGoalTeam) {
        this.lastGoalTeam = lastGoalTeam;
    }
}