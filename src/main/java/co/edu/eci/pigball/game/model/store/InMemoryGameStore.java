package co.edu.eci.pigball.game.model.store;


import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

import co.edu.eci.pigball.game.model.dto.GameDTO;
import co.edu.eci.pigball.game.model.mapper.GameMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import co.edu.eci.pigball.game.exception.GameException;
import co.edu.eci.pigball.game.model.Game;

//@Component("memoryGameStore")
//@ConditionalOnProperty(
//        name = "app.store.type",
//        havingValue = "memory",
//        matchIfMissing = true    // crea este bean también si no hay propiedad
//)
public class InMemoryGameStore implements IGameStore {

    // Ahora guardamos DTOs planos
    private final ConcurrentHashMap<String, GameDTO> metaMap = new ConcurrentHashMap<>();

    @Override
    public void save(Game game) throws GameException {
        GameDTO dto = GameMapper.toDTO(game);
        metaMap.put(dto.getId(), dto);
    }

    @Override
    public boolean exists(String gameId) {
        return metaMap.containsKey(gameId);
    }

    @Override
    public GameDTO findMeta(String gameId) throws GameException {
        GameDTO dto = metaMap.get(gameId);
        if (dto == null) {
            throw new GameException(GameException.GAME_NOT_FOUND);
        }
        return dto;
    }

    @Override
    public Collection<GameDTO> findAllMeta() {
        return metaMap.values();
    }

    @Override
    public void deleteMeta(String gameId) {
        metaMap.remove(gameId);
    }
}