package co.edu.eci.pigball.game.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class Movement {

    private String player;
    private int dx;
    private int dy;

}
