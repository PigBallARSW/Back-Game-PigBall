package co.edu.eci.pigball.game.model.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BasicGameDTO {
    private String id;
    private String gameName;
    private List<PlayerDTO> players;
    private BallDTO ball;
}
