package co.edu.eci.pigball.game.model.DTO;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

import co.edu.eci.pigball.game.model.Game;
import co.edu.eci.pigball.game.model.GameStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class GameDTO {

    private String id;
    private String gameName;
    private String creatorName;
    private int maxPlayers;
    private GameStatus status;
    private boolean privateGame;
    private Instant creationTime;
    private int borderX;
    private int borderY;
    private List<PlayerDTO> players;
    
    public GameDTO(Game game) {
        this.id = game.getGameId();
        this.gameName = game.getGameName();
        this.creatorName = game.getCreatorName();
        this.maxPlayers = game.getMaxPlayers();
        this.status = game.getStatus();
        this.privateGame = game.isPrivateGame();
        this.creationTime = game.getCreationTime();
        this.borderX = game.getBorderX();
        this.borderY = game.getBorderY();
        Collection<PlayerDTO> playersDTO = PlayerDTO.toDTO(game.getPlayers().values());
        this.players = (List<PlayerDTO>) playersDTO;
    }

    public static GameDTO toDTO(Game game) {
        return new GameDTO(game);
    }

    public static Collection<GameDTO> toDTO(Collection<Game> games) {
        return games.stream().map(GameDTO::toDTO).toList();
    }
}
