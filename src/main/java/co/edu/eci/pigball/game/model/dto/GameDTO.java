package co.edu.eci.pigball.game.model.dto;

import java.time.Instant;
import java.util.List;

import co.edu.eci.pigball.game.model.Event;
import co.edu.eci.pigball.game.model.GameStatus;
import co.edu.eci.pigball.game.utility.Pair;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GameDTO {

    private String id;
    private String gameName;
    private String creatorName;
    private Integer maxPlayers;
    private GameStatus status;
    private Boolean privateGame;
    private Instant creationTime;
    private Instant startTime;
    private int borderX;
    private int borderY;
    private List<PlayerDTO> players;
    private BallDTO ball;
    private List<Pair<String,Event>> events;
    private Pair<Integer, Integer> teams;
    private String style;

    public int getMaxPlayers() {
        if (maxPlayers == null)
            return 4;
        return maxPlayers;
    }

    public boolean isPrivateGame() {
        if (privateGame == null)
            return false;
        return privateGame;
    }

    @Override
    public String toString() {
        return "GameDTO [id=" + id + ", gameName=" + gameName + ", creatorName=" + creatorName + ", maxPlayers="
                + maxPlayers + ", status=" + status + ", privateGame=" + privateGame + ", creationTime="
                + creationTime + ", startTime=" + startTime + ", borderX=" + borderX + ", borderY=" + borderY
                + ", players=" + players + ", ball=" + ball + "]";
    }
}
