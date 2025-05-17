package co.edu.eci.pigball.game.config;


import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "app.store.type", havingValue = "redis")
public class RedissonConfig {

    @Value("${REDIS_HOST}")
    private String redisHost;

    @Value("${REDIS_PORT}")
    private int redisPort;

    @Value("${SSL_ENABLED}")
    private boolean redisSSL;

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        String protocol = redisSSL ? "rediss" : "redis";
        config.useSingleServer()
                .setAddress(protocol + "://" + redisHost + ":" + redisPort);
        return Redisson.create(config);
    }
}