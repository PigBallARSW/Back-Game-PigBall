package co.edu.eci.pigball.game.config;

import java.util.Collection;


import co.edu.eci.pigball.game.exception.GameException;
import co.edu.eci.pigball.game.model.Game;
import co.edu.eci.pigball.game.model.dto.GameDTO;
import co.edu.eci.pigball.game.model.store.IGameStore;
import co.edu.eci.pigball.game.model.store.InMemoryGameStore;
import co.edu.eci.pigball.game.model.store.RedisGameStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class GameStoreConfig implements IGameStore {

    private final IGameStore memoryStore;
    private final IGameStore redisStore;
    private final IGameStore delegate;

    public GameStoreConfig(
            InMemoryGameStore memoryStore,
            RedisGameStore redisStore,
            @Value("${APP_STORE_TYPE}") String storeType
    ) {
        this.memoryStore = memoryStore;
        this.redisStore  = redisStore;
        this.delegate    = "redis".equalsIgnoreCase(storeType)
                ? this.redisStore
                : this.memoryStore;
    }

    @Override
    public void save(Game game) throws GameException {
        delegate.save(game);
    }

    @Override
    public boolean exists(String gameId) {
        return delegate.exists(gameId);
    }

    @Override
    public GameDTO findMeta(String gameId) throws GameException {
        return delegate.findMeta(gameId);
    }

    @Override
    public Collection<GameDTO> findAllMeta() {
        return delegate.findAllMeta();
    }

    @Override
    public void deleteMeta(String gameId) {
        delegate.deleteMeta(gameId);
    }
}
