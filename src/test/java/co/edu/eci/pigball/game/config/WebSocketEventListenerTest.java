package co.edu.eci.pigball.game.config;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import co.edu.eci.pigball.game.exception.GameException;
import co.edu.eci.pigball.game.service.GameService;

@ExtendWith(MockitoExtension.class)
class WebSocketEventListenerTest {

    @Mock
    private GameService gameService;

    @Mock
    private SessionConnectEvent connectEvent;

    @Mock
    private SessionDisconnectEvent disconnectEvent;

    @SuppressWarnings("rawtypes")
    @Mock
    private Message message;

    @Mock
    private MessageHeaders messageHeaders;

    @Mock
    private StompHeaderAccessor accessor;

    @Mock
    private ScheduledExecutorService scheduler;

    @SuppressWarnings("rawtypes")
    @Mock
    private ScheduledFuture scheduledFuture;

    private WebSocketEventListener eventListener;

    @BeforeEach
    void setUp() {
        // Inicializar los mocks necesarios
        eventListener = new WebSocketEventListener(gameService);
        
        // Usar reflection para acceder a los campos privados y reemplazarlos con mocks
        try {
            java.lang.reflect.Field schedulerField = WebSocketEventListener.class.getDeclaredField("scheduler");
            schedulerField.setAccessible(true);
            schedulerField.set(eventListener, scheduler);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    void testHandleWebSocketConnectListener() {
        // Setup
        String sessionId = "test-session-id";
        when(connectEvent.getMessage()).thenReturn(message);
        when(accessor.getSessionId()).thenReturn(sessionId);

        // Use MockedStatic to mock the static method
        try (MockedStatic<StompHeaderAccessor> mockedStatic = mockStatic(StompHeaderAccessor.class)) {
            mockedStatic.when(() -> StompHeaderAccessor.wrap(message)).thenReturn(accessor);

            // Execute
            eventListener.handleWebSocketConnectListener(connectEvent);

            // Verify
            verify(connectEvent).getMessage();
            verify(accessor).getSessionId();
        }
    }

    @Test
    void testSetANewConnection() {
        // Setup
        String sessionId = "test-session-id";
        String gameId = "test-game-id";
        String playerName = "test-player";

        // Execute
        eventListener.setANewConnection(sessionId, gameId, playerName);

        // Verify
        // Note: We can't directly verify the internal state of ConcurrentHashMaps
        // but we can verify that the method executes without throwing exceptions
        assertDoesNotThrow(() -> eventListener.setANewConnection(sessionId, gameId, playerName));
    }

    @SuppressWarnings("unchecked")
    @Test
    void testHandleWebSocketDisconnectListener() {
        // Setup
        String sessionId = "test-session-id";
        String gameId = "test-game-id";
        String playerName = "test-player";
        
        when(disconnectEvent.getSessionId()).thenReturn(sessionId);
        when(scheduler.schedule(any(Runnable.class), eq(30L), eq(TimeUnit.SECONDS))).thenReturn(scheduledFuture);
        
        // Simular que el jugador ya está registrado
        eventListener.setANewConnection(sessionId, gameId, playerName);

        // Execute
        eventListener.handleWebSocketDisconnectListener(disconnectEvent);

        // Verify
        verify(disconnectEvent).getSessionId();
        verify(scheduler).schedule(any(Runnable.class), eq(30L), eq(TimeUnit.SECONDS));
    }

    @Test
    void testHandleWebSocketDisconnectListenerWithNullSessionId() {
        // Setup
        when(disconnectEvent.getSessionId()).thenReturn(null);

        // Execute
        eventListener.handleWebSocketDisconnectListener(disconnectEvent);

        // Verify
        verify(disconnectEvent).getSessionId();
        verify(scheduler, never()).schedule(any(Runnable.class), anyLong(), any(TimeUnit.class));
    }

    @Test
    void testHandleWebSocketDisconnectListenerWithUnknownSession() {
        // Setup
        String sessionId = "unknown-session-id";
        when(disconnectEvent.getSessionId()).thenReturn(sessionId);

        // Execute
        eventListener.handleWebSocketDisconnectListener(disconnectEvent);

        // Verify
        verify(disconnectEvent).getSessionId();
        verify(scheduler, never()).schedule(any(Runnable.class), anyLong(), any(TimeUnit.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    void testCancelDisconnection() {
        // Setup
        String playerName = "test-player";
        String sessionId = "test-session-id";
        String gameId = "test-game-id";
        
        // Registrar un jugador
        eventListener.setANewConnection(sessionId, gameId, playerName);
        
        // Simular una desconexión para crear un timer
        when(disconnectEvent.getSessionId()).thenReturn(sessionId);
        when(scheduler.schedule(any(Runnable.class), eq(30L), eq(TimeUnit.SECONDS))).thenReturn(scheduledFuture);
        eventListener.handleWebSocketDisconnectListener(disconnectEvent);
        
        // Simular una reconexión
        String newSessionId = "new-session-id";
        eventListener.setANewConnection(newSessionId, gameId, playerName);
        
        // Verify
        verify(scheduledFuture).cancel(false);
    }

    @SuppressWarnings("unchecked")
    @Test
    void testRemovePlayerFromGame() throws GameException {
        // Setup
        String gameId = "test-game-id";
        String playerName = "test-player";
        String sessionId = "test-session-id";
        
        // Registrar un jugador
        eventListener.setANewConnection(sessionId, gameId, playerName);
        
        // Simular una desconexión para crear un timer
        when(disconnectEvent.getSessionId()).thenReturn(sessionId);
        when(scheduler.schedule(any(Runnable.class), eq(30L), eq(TimeUnit.SECONDS))).thenReturn(scheduledFuture);
        eventListener.handleWebSocketDisconnectListener(disconnectEvent);
        
        // Ejecutar directamente el método removePlayerFromGame usando reflection
        try {
            java.lang.reflect.Method method = WebSocketEventListener.class.getDeclaredMethod("removePlayerFromGame", String.class, String.class);
            method.setAccessible(true);
            method.invoke(eventListener, gameId, playerName);
            
            // Verify
            verify(gameService).removePlayerFromGame(gameId, playerName);
        } catch (Exception e) {
            if (e.getCause() instanceof GameException) {
                throw (GameException) e.getCause();
            }
            e.printStackTrace();
        }
    }

    @Test
    void testRemovePlayerFromGameWithException() throws GameException {
        // Setup
        String gameId = "test-game-id";
        String playerName = "test-player";
        
        // Simular una excepción al eliminar el jugador
        doThrow(new GameException("Test exception")).when(gameService).removePlayerFromGame(gameId, playerName);
        
        // Ejecutar directamente el método removePlayerFromGame usando reflection
        try {
            java.lang.reflect.Method method = WebSocketEventListener.class.getDeclaredMethod("removePlayerFromGame", String.class, String.class);
            method.setAccessible(true);
            method.invoke(eventListener, gameId, playerName);
            
            // Verify
            verify(gameService).removePlayerFromGame(gameId, playerName);
        } catch (Exception e) {
            if (e.getCause() instanceof GameException) {
                throw (GameException) e.getCause();
            }
            // Other exceptions are not expected and should be propagated
            throw new RuntimeException(e);
        }
    }
}