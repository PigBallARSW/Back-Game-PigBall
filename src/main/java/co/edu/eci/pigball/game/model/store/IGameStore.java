package co.edu.eci.pigball.game.model.store;
import java.util.Collection;
import co.edu.eci.pigball.game.exception.GameException;
import co.edu.eci.pigball.game.model.Game;
import co.edu.eci.pigball.game.model.dto.GameDTO;

public interface IGameStore {
    void save(Game game) throws GameException;
    boolean exists(String gameId);
    GameDTO findMeta(String gameId) throws GameException;
    Collection<GameDTO> findAllMeta();
    void deleteMeta(String gameId);
}