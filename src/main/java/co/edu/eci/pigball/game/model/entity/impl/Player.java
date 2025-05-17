package co.edu.eci.pigball.game.model.entity.impl;

import java.util.List;

import co.edu.eci.pigball.game.model.entity.Entity;
import co.edu.eci.pigball.game.utility.Pair;
import lombok.Getter;

@Getter
public class Player extends Entity {
    private String name;
    private String sessionId;
    private Integer team;
    private String id;
    private boolean isKicking;
    private int lastDx;
    private int lastDy;
    private boolean lastIsKicking;

    public Player(String name, String id, String sessionId, int x, int y, double radius) {
        super(x, y, radius);
        this.name = name;
        this.sessionId = sessionId;
        this.isKicking = false;
        this.id = id;
        this.lastDx = 0;
        this.lastDy = 0;
        this.lastIsKicking = false;
    }

    public void updatePlayerLastMovement(int dx, int dy, boolean isKicking) {
        this.lastDx = dx;
        this.lastDy = dy;
        this.lastIsKicking = isKicking;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public void setTeam(Integer team) {
        this.team = team;
    }

    public void setIsKicking(boolean isKicking) {
        this.isKicking = isKicking;
    }

    @Override
    public Pair<Double, Double> validateCoordinates(int borderX, int borderY, Pair<Double, Double> coordinates,
            List<Entity> entities) {
        double newX = coordinates.getFirst();
        double newY = coordinates.getSecond();

        // 1. Ajustar posición si existe colisión con otro jugador
        Pair<Double, Double> collisionAdjusted = adjustForPlayerCollision(newX, newY, entities);
        newX = collisionAdjusted.getFirst();
        newY = collisionAdjusted.getSecond();

        // 2. Ajustar la posición según la zona del campo
        Pair<Double, Double> goalAdjusted = handleGoalArea(newX, newY, borderX, borderY);
        if (goalAdjusted != null) {
            return goalAdjusted;
        }

        // 3. Ajustar la posición según las paredes del campo
        return adjustForWallCollisions(newX, newY, borderX, borderY);
    }

    /**
     * Verifica colisiones con otros jugadores. Si se detecta una colisión, se
     * recalcula
     * la posición para evitar el solapamiento, dejando la pelota lo más cerca
     * posible del jugador.
     */
    private Pair<Double, Double> adjustForPlayerCollision(double x, double y, List<Entity> entities) {
        for (Entity entity : entities) {
            // Se ignora la entidad actual
            if (entity != this) {
                double px = entity.getX();
                double py = entity.getY();
                double distance = Math.sqrt(Math.pow(px - x, 2) + Math.pow(py - y, 2));
                if (distance < 2 * radius) {
                    double angle = Math.atan2(y - py, x - px);
                    // Se utiliza (int) para truncar el valor calculado si es necesario
                    x = px + (int) (Math.cos(angle) * 2 * radius);
                    y = py + (int) (Math.sin(angle) * 2 * radius);
                    break;
                }
            }
        }
        return new Pair<>(x, y);
    }

    private Pair<Double, Double> handleGoalArea(double x, double y, int borderX, int borderY) {
        int middleY = borderY / 2;

        double extension = borderX * 0.03;

        double minY = middleY - (borderY * 0.09) + radius;
        double maxY = middleY + (borderY * 0.09) - radius;

        boolean isInXRange = (x - radius <= 0) || (x + radius >= borderX);

        // Rango vertical original (igual que antes)
        boolean isInYRange = (y - radius + 10 > middleY - (borderY * 0.09)) &&
                (y + radius - 10 < middleY + (borderY * 0.09));

        if (isInXRange && isInYRange) {
            // Límites reales para que el círculo nunca sobrepase ese 3 %
            double minXAllowed = -extension + radius;
            double maxXAllowed = borderX + extension - radius;

            x = Math.clamp(x, minXAllowed, maxXAllowed);
            y = Math.clamp(y, minY, maxY);

            return new Pair<>(x, y);
        }

        return null;
    }

    /**
     * Ajusta las coordenadas en caso de colisión con las paredes.
     * Se parte de la posición previa (super.getX(), super.getY()) y se asegura que
     * la posición final quede dentro de los límites del campo.
     */
    private Pair<Double, Double> adjustForWallCollisions(double x, double y, int borderX, int borderY) {
        Pair<Double, Double> adjusted = new Pair<>(super.getX(), super.getY());
        double minX = radius;
        double maxX = borderX - radius;
        double minY = radius;
        double maxY = borderY - radius;
        adjusted.setFirst(Math.clamp(x, minX, maxX));
        adjusted.setSecond(Math.clamp(y, minY, maxY));
        return adjusted;
    }

}
