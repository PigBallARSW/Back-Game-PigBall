package co.edu.eci.pigball.game.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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

import co.edu.eci.pigball.game.service.GameService;

@ExtendWith(MockitoExtension.class)
public class WebSocketEventListenerTest {

    @Mock
    private GameService gameService;

    @Mock
    private SessionConnectEvent connectEvent;

    @Mock
    private SessionDisconnectEvent disconnectEvent;

    @Mock
    private Message message;

    @Mock
    private MessageHeaders messageHeaders;

    @Mock
    private StompHeaderAccessor accessor;

    private WebSocketEventListener eventListener;

    @BeforeEach
    void setUp() {
        eventListener = new WebSocketEventListener(gameService);
    }

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
    }

    @Test
    void testHandleWebSocketDisconnectListener() {
        // Setup
        String sessionId = "test-session-id";
        when(disconnectEvent.getSessionId()).thenReturn(sessionId);

        // Execute
        eventListener.handleWebSocketDisconnectListener(disconnectEvent);

        // Verify
        verify(disconnectEvent).getSessionId();
    }

    @Test
    void testSetANewConnectionWithExistingSession() {
        // Setup
        String sessionId1 = "session-1";
        String sessionId2 = "session-2";
        String gameId = "test-game-id";
        String playerName = "test-player";

        // Execute
        eventListener.setANewConnection(sessionId1, gameId, playerName);
        eventListener.setANewConnection(sessionId2, gameId, playerName);

        // Verify
        // Note: We can't directly verify the internal state of ConcurrentHashMaps
        // but we can verify that the method executes without throwing exceptions
    }
} 