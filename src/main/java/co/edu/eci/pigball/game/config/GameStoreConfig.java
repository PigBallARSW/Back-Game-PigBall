package co.edu.eci.pigball.game.config;

import java.util.Collection;


import co.edu.eci.pigball.game.exception.GameException;
import co.edu.eci.pigball.game.model.Game;
import co.edu.eci.pigball.game.model.dto.GameDTO;
import co.edu.eci.pigball.game.model.store.IGameStore;
import co.edu.eci.pigball.game.model.store.InMemoryGameStore;
import co.edu.eci.pigball.game.model.store.RedisGameStore;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
    private final String storeType;

    public GameStoreConfig(
            InMemoryGameStore memoryStore,
            @Value("${app.store.type:memory}") String storeType
    ) {
        this.memoryStore = memoryStore;
        this.storeType = storeType;
    }

    /** Solo se crea este bean si app.store.type=redis */
    @Bean
    @ConditionalOnProperty(name = "app.store.type", havingValue = "redis")
    public RedisGameStore redisGameStore(RedissonClient redisson) {
        return new RedisGameStore(redisson);
    }

    /** Bean primario de IGameStore: Redis (si existe) o memoria */
    @Bean
    @Primary
    public IGameStore gameStore(
            @Autowired(required = false) RedisGameStore redisStore
    ) {
        return (redisStore != null) ? redisStore : memoryStore;
    }
}

