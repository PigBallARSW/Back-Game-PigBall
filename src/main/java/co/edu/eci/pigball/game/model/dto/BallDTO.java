package co.edu.eci.pigball.game.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BallDTO {
    private double x;
    private double y;
    private double velocityX;
    private double velocityY;
}
