package co.edu.eci.pigball.game.config;

import java.util.Collection;


import co.edu.eci.pigball.game.exception.GameException;
import co.edu.eci.pigball.game.model.Game;
import co.edu.eci.pigball.game.model.dto.GameDTO;
import co.edu.eci.pigball.game.model.store.IGameStore;
import co.edu.eci.pigball.game.model.store.InMemoryGameStore;
import co.edu.eci.pigball.game.model.store.RedisGameStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * Configuración de Spring para exponer un único bean IGameStore
 * seleccionado dinámicamente según la propiedad `app.store.type`.
 */
@Configuration
public class GameStoreConfig {

    private final InMemoryGameStore memoryStore;
    private final RedisGameStore redisStore;
    private final String storeType;

    @Autowired
    public GameStoreConfig(
            InMemoryGameStore memoryStore,
            @Autowired(required = false) RedisGameStore redisStore,
            @Value("${app.store.type:memory}") String storeType
    ) {
        this.memoryStore = memoryStore;
        this.redisStore = redisStore;
        this.storeType = storeType;
    }

    /**
     * Bean primario de IGameStore: delega a Redis si está configurado,
     * o a memoria en cualquier otro caso.
     */
    @Bean
    @Primary
    public IGameStore gameStore() {
        if ("redis".equalsIgnoreCase(storeType) && redisStore != null) {
            return redisStore;
        }
        return memoryStore;
    }
}

