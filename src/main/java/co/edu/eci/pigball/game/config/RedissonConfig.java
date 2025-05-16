package co.edu.eci.pigball.game.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer()
              .setAddress("redis://172.31.39.198:6379")
              .setConnectionPoolSize(64)
              .setConnectionMinimumIdleSize(24);
        return Redisson.create(config);
    }
}
