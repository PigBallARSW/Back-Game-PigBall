package co.edu.eci.pigball.game.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import co.edu.eci.pigball.game.service.GameService;
import co.edu.eci.pigball.game.utility.Pair;

import java.util.concurrent.*;

@Component
public class WebSocketEventListener {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);

    private final GameService gameService;
    // Mapea el nombre del jugador a su sessionId
    private final ConcurrentHashMap<String, String> nameToSession = new ConcurrentHashMap<>();
    // Mapea el sessionId a un par (gameId, playerName)
    private final ConcurrentHashMap<String, Pair<String, String>> sessionToPlayer = new ConcurrentHashMap<>();

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final ConcurrentHashMap<String, ScheduledFuture<?>> disconnectTimers = new ConcurrentHashMap<>();

    public WebSocketEventListener(GameService gameService) {
        this.gameService = gameService;
    }

    /**
     * Detectar nueva conexión STOMP
     */
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();

        if (sessionId != null) {
            logger.info("Nueva conexion STOMP detectada - Session ID: {}", sessionId);
        } else {
            logger.warn("No se pudo obtener Session ID en conexión STOMP");
        }
    }

    /**
     * Asignar una nueva sesión a un jugador.
     * Si tenía una sesión anterior, se cancela su desconexión.
     */
    public void setANewConnection(String sessionId, String gameId, String playerName) {
        // Intentamos insertar el sessionId solo si el jugador no tenía una sesión
        String existingSession = nameToSession.putIfAbsent(playerName, sessionId);
        if (existingSession == null) {
            logger.info("Jugador {} conectado con sesion: {}", playerName, sessionId);
        } else {
            // Si ya había una sesión, actualizamos el mapping y cancelamos la eliminación
            nameToSession.put(playerName, sessionId);
            sessionToPlayer.remove(existingSession);
            logger.info("Jugador {} cambió de sesion: {} -> {}", playerName, existingSession, sessionId);
            cancelDisconnection(playerName);
        }
        // Actualizamos o insertamos la nueva relación sessionId -> (gameId, playerName)
        sessionToPlayer.put(sessionId, new Pair<>(gameId, playerName));
    }

    /**
     * Manejar desconexión de un usuario STOMP.
     * Se programa la eliminación del jugador después de 30 segundos.
     */
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        if (sessionId != null) {
            Pair<String, String> gamePlayerInfo = sessionToPlayer.remove(sessionId);
            if (gamePlayerInfo != null) {
                String gameId = gamePlayerInfo.getFirst();
                String playerName = gamePlayerInfo.getSecond();

                logger.info("Sesion desconectada: {} (Jugador: {}) esperando 30 segundos por posible reconexion", sessionId, playerName);

                // Programar eliminación del jugador después de 30 segundos
                ScheduledFuture<?> scheduledTask = scheduler.schedule(() -> removePlayerFromGame(gameId, playerName),
                        30, TimeUnit.SECONDS);

                disconnectTimers.put(playerName, scheduledTask);
            }
        } else {
            logger.warn("No se pudo obtener Session ID en desconexión STOMP");
        }
    }

    /**
     * Método para eliminar al jugador del juego si no se reconecta a tiempo.
     */
    private void removePlayerFromGame(String gameId, String playerName) {
        try {
            gameService.removePlayerFromGame(gameId, playerName);
            logger.info("Jugador eliminado por inactividad: {}", playerName);
            String removedSession = nameToSession.remove(playerName);
            sessionToPlayer.remove(removedSession);
            if (removedSession != null) {
                logger.info("Eliminando mapeo de sesion para {}", playerName);
            }
        } catch (Exception e) {
            logger.warn("No se pudo eliminar al jugador {}", playerName);
        }
    }

    /**
     * Cancelar eliminación si el usuario se reconecta antes del tiempo límite.
     */
    private void cancelDisconnection(String playerName) {
        ScheduledFuture<?> scheduledTask = disconnectTimers.remove(playerName);
        if (scheduledTask != null) {
            scheduledTask.cancel(false);
            logger.info("Reconexion detectada, cancelando eliminacion de {}", playerName);
        }
    }
}
