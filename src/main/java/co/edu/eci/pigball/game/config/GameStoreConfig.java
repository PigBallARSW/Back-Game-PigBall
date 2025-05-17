package co.edu.eci.pigball.game.config;

import java.util.Collection;


import co.edu.eci.pigball.game.exception.GameException;
import co.edu.eci.pigball.game.model.Game;
import co.edu.eci.pigball.game.model.dto.GameDTO;
import co.edu.eci.pigball.game.model.store.IGameStore;
import co.edu.eci.pigball.game.model.store.InMemoryGameStore;
import co.edu.eci.pigball.game.model.store.RedisGameStore;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Configuration
public class GameStoreConfig {

    @Bean
    @ConditionalOnProperty(name = "APP_STORE_TYPE", havingValue = "memory", matchIfMissing = true)
    public IGameStore inMemoryGameStore() {
        return new InMemoryGameStore();
    }

    @Bean
    @ConditionalOnProperty(name = "APP_STORE_TYPE", havingValue = "redis")
    public IGameStore redisGameStore(RedissonClient redisson) {
        return new RedisGameStore(redisson);
    }
}

