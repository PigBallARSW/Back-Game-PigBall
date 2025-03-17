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

    private List<Player> players;
    
    public GameDTO(Collection<Player> players) {
        this.players = List.copyOf(players);    
    }

    public static GameDTO toDTO(Game game) {
        Collection<Player> players = game.getPlayers().values();
        return new GameDTO(players);
    }

    public static Collection<GameDTO> toDTO(Collection<Game> games) {
        return games.stream().map(GameDTO::toDTO).toList();
    }
}
