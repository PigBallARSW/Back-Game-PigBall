package co.edu.eci.pigball.game.utility;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Pair<T, U> {
    private T first;
    private U second;

    public Pair() {}

    @JsonCreator
    public Pair(
            @JsonProperty("first") T first,
            @JsonProperty("second") U second
    ) {
        this.first = first;
        this.second = second;
    }

}
