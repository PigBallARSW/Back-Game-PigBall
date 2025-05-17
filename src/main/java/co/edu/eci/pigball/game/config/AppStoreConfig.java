package co.edu.eci.pigball.game.config;

import co.edu.eci.pigball.game.model.store.IGameStore;
import co.edu.eci.pigball.game.model.store.InMemoryGameStore;
import co.edu.eci.pigball.game.model.store.RedisGameStore;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppStoreConfig {

    @Value("${app.store.type:memory}")
    private String storeType;

    @Bean
    public IGameStore gameStore(
            @Value("${app.store.type:memory}") String storeType,
            @Autowired(required = false) RedissonClient redissonClient) {

        return switch (storeType.toLowerCase()) {
            case "redis" -> {
                if (redissonClient == null) {
                    throw new IllegalStateException("RedissonClient not configured but app.store.type is redis");
                }
                yield new RedisGameStore(redissonClient);
            }
            case "memory" -> new InMemoryGameStore();
            default -> throw new IllegalArgumentException("Unknown store type: " + storeType);
        };
    }
}