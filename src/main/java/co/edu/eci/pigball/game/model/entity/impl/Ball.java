package co.edu.eci.pigball.game.model.entity.impl;

import java.util.List;
import java.util.ArrayList;

import co.edu.eci.pigball.game.model.GameObserver;
import co.edu.eci.pigball.game.model.entity.Entity;
import co.edu.eci.pigball.game.utility.FixedSizeStackLikeQueue;
import co.edu.eci.pigball.game.utility.Pair;

public class Ball extends Entity {

    public static final double COLLISON_PLAYER_BOOST = 1.3;
    public static final double COLLISON_WALL_BOOST = 0.9;
    public static final double MIN_MAGNITUDE = 100;
    public static final double MAX_MAGNITUDE = 800;
    
    // Variables para almacenar la dirección de movimiento
    private double velocityX = 0;
    private double velocityY = 0;
    private List<GameObserver> observers;
    private int lastGoalTeam = -1;
    //Cola de prioridad con los ultimos jugadores que han tocado la pelota
    private FixedSizeStackLikeQueue<Player> lastPlayers;

    public Ball(int x, int y, int velocityX, int velocityY, double radius) {
        super(x, y, radius);
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.observers = new ArrayList<>();
        this.lastPlayers = new FixedSizeStackLikeQueue<>(5);
    }

    public void addObserver(GameObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(GameObserver observer) {
        observers.remove(observer);
    }

    private void notifyGoalScored(int team, List<Player> players) {
        if (team != lastGoalTeam) { // Prevent multiple notifications for the same goal
            lastGoalTeam = team;
            for (GameObserver observer : observers) {
                observer.onGoalScored(team, players);
            }
        }
    }

    @Override
    public Pair<Double, Double> validateCoordinates(int borderX, int borderY, Pair<Double, Double> coordinates,
            List<Entity> entities) {
        double newX = coordinates.getFirst();
        double newY = coordinates.getSecond();
        

        // 1. Procesar colisiones con jugadores
        Pair<Double, Double> collisionResult = handlePlayerCollision(newX, newY, entities);
        if (collisionResult != null) {
            newX = collisionResult.getFirst();
            newY = collisionResult.getSecond();
            limitVelocity();
            return new Pair<>(newX, newY);
        }

        // 2. Verificar área de gol
        Pair<Double, Double> goalResult = handleGoalArea(newX, newY, borderX, borderY);
        if (goalResult != null) {
            return goalResult;
        }

        // 3. Procesar colisiones con paredes (límites del campo)
        Pair<Double, Double> wallResult = handleWallCollisions(newX, newY, borderX, borderY, COLLISON_WALL_BOOST);
        newX = wallResult.getFirst();
        newY = wallResult.getSecond();

        limitVelocity();

        // 4. Asegurar que la posición se encuentre dentro de los límites
        newX = Math.clamp(newX, radius, borderX - radius);
        newY = Math.clamp(newY, radius, borderY - radius);
        return new Pair<>(newX, newY);
    }

    /**
     * Verifica colisiones con los jugadores. Si se detecta colisión, actualiza la
     * posición y la velocidad de la pelota según el ángulo de colisión y el
     * impulso.
     * Retorna un Pair con la nueva posición o null si no hubo colisión.
     */
    private Pair<Double, Double> handlePlayerCollision(double x, double y, List<Entity> entities) {
        for (Entity entity : entities) {
            double entityX = entity.getX();
            double entityY = entity.getY();
            double distance = Math.sqrt(Math.pow(entityX - x, 2) + Math.pow(entityY - y, 2));
            if (distance < radius + entity.getRadius()) {
                boolean isKicking = ((Player) entity).isKicking();
                double angle = Math.atan2(y - entityY, x - entityX);
                double magnitude = Math.sqrt(velocityX * velocityX + velocityY * velocityY);
                if (magnitude < MIN_MAGNITUDE) {
                    magnitude = 200.0;
                }
                if (isKicking) {
                    magnitude = 800;
                }
                // Asigna nueva velocidad según el ángulo de colisión
                velocityX = Math.cos(angle) * magnitude;
                velocityY = Math.sin(angle) * magnitude;

                // Actualiza la posición para evitar solapamientos
                x = entityX + Math.cos(angle) * (radius + entity.getRadius());
                y = entityY + Math.sin(angle) * (radius + entity.getRadius());
                lastPlayers.add((Player) entity);
                return new Pair<>(x, y);
            }
        }
        return null;
    }

    /**
     * Verifica si la pelota se encuentra en el área de gol. Si se detecta gol,
     * notifica el evento y retorna la posición reiniciada (centro del campo).
     * Si no hay gol, retorna null.
     */
    private Pair<Double, Double> handleGoalArea(double x, double y, int borderX, int borderY) {
        int middleY = borderY / 2;
        boolean isInXRange = x - radius <= 0 || x + radius >= borderX;
        boolean isInYRange = (y - radius + 10 > middleY - (borderY * 0.09)) && (y + radius - 10 < middleY + (borderY * 0.09));
        boolean isInGoalArea = isInXRange && isInYRange;
        if (isInGoalArea) {
            if (x - radius < -2 * radius) {
                notifyGoalScored(1, lastPlayers.getElements());
                lastPlayers.reset();
                System.out.println("Gol del equipo 1");
                return new Pair<>(borderX / 2.0, borderY / 2.0);
            } else if (x + radius > borderX + 2 * radius) {
                notifyGoalScored(2, lastPlayers.getElements());
                lastPlayers.reset();
                System.out.println("Gol del equipo 2");
                return new Pair<>(borderX / 2.0, borderY / 2.0);
            }
            y = Math.clamp(y, middleY - (borderY * 0.09) + radius, middleY + (borderY * 0.09) - radius);
            return new Pair<>(x, y);
        }
        return null;
    }

    /**
     * Verifica las colisiones con las paredes y ajusta la posición y la velocidad
     * de
     * la pelota. Retorna la posición actualizada.
     */
    private Pair<Double, Double> handleWallCollisions(double x, double y, int borderX, int borderY, double boost) {
        // Colisión con paredes laterales
        if (x - radius <= 0) {
            x = radius;
            velocityX = -velocityX * boost;
        } else if (x + radius >= borderX) {
            x = borderX - radius;
            velocityX = -velocityX * boost;
        }

        // Colisión con paredes superior e inferior
        if (y - radius <= 0) {
            y = radius;
            velocityY = -velocityY * boost;
        } else if (y + radius >= borderY) {
            y = borderY - radius;
            velocityY = -velocityY * boost;
        }
        return new Pair<>(x, y);
    }

    /**
     * Limita la magnitud de la velocidad para que no exceda el valor máximo.
     */
    private void limitVelocity() {
        double actualMagnitude = Math.sqrt(velocityX * velocityX + velocityY * velocityY);
        if (actualMagnitude > MAX_MAGNITUDE) {
            // Se usa paréntesis para asegurar la operación correcta
            double scale = (MAX_MAGNITUDE - 1) / actualMagnitude;
            velocityX *= scale;
            velocityY *= scale;
        }
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
        limitVelocity();
    }

    public void setLastGoalTeam(int lastGoalTeam) {
        this.lastGoalTeam = lastGoalTeam;
    }
}