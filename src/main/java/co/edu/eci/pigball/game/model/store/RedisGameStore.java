package co.edu.eci.pigball.game.model.store;

import java.util.Collection;
import java.util.stream.Collectors;

import co.edu.eci.pigball.game.model.dto.GameDTO;
import co.edu.eci.pigball.game.model.mapper.GameMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.redisson.api.RMap;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.Codec;
import org.redisson.codec.JsonJacksonCodec;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import co.edu.eci.pigball.game.exception.GameException;
import co.edu.eci.pigball.game.model.Game;

@Component
//@ConditionalOnBean(RedissonClient.class)
public class RedisGameStore implements IGameStore {

    private final RMap<String, GameDTO> metaMap;
    private final RTopic updates;

    public RedisGameStore(RedissonClient redisson) {
        ObjectMapper mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        Codec codec = new JsonJacksonCodec(mapper);

        this.metaMap = redisson.<String,GameDTO>getMap("games:meta", codec);
        this.updates = redisson.getTopic("games:updates", codec);
        updates.addListener(GameDTO.class, (channel, dto) -> {
            metaMap.put(dto.getId(), dto);
        });
    }
    public RTopic getUpdateTopic() {
        return updates;
    }

    @Override
    public void save(Game game) throws GameException {
        GameDTO dto = GameMapper.toDTO(game);
        metaMap.put(dto.getId(), dto);
        //updates.publish(dto);

    }
    /** Este método se ejecuta cada 20 000 ms y vuelve a publicar todos los DTOs */
    @Scheduled(fixedRate = 20_000)
    public void rebroadcastAllGames() {
        for (GameDTO dto : metaMap.readAllValues()) {
            updates.publish(dto);
        }
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
        return metaMap.readAllValues();
    }

    @Override
    public void deleteMeta(String gameId) {
        metaMap.remove(gameId);
    }
}