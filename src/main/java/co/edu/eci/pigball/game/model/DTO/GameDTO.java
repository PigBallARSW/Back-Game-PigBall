package co.edu.eci.pigball.game.model.DTO;

import java.util.Collection;
import java.util.List;

import co.edu.eci.pigball.game.model.Game;
import co.edu.eci.pigball.game.model.Player;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class GameDTO {

    private Long id;
    private String name;
    private List<Player> players;
    
    public GameDTO(Collection<Player> players,Long id, String name) {
        this.players = List.copyOf(players);
        this.id = id;
        this.name = name;
    }

    public static GameDTO toDTO(Game game) {
        Collection<Player> players = game.getPlayers().values();
        return new GameDTO(players, game.getGameId(), game.getGameName());
    }

    public static Collection<GameDTO> toDTO(Collection<Game> games) {
        return games.stream().map(GameDTO::toDTO).toList();
    }
}
