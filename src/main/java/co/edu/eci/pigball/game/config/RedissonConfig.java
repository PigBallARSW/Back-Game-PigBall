package co.edu.eci.pigball.game.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class RedissonConfig {

    @Bean(destroyMethod="shutdown")
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useClusterServers()
            .addNodeAddress("redis://clustercfg.pigball-redis.lmdnlx.use1.cache.amazonaws.com:6379")
            // Si usas contraseña:
            //.setPassword("tu_contraseña")
            ;
        return Redisson.create(config);
    }
}