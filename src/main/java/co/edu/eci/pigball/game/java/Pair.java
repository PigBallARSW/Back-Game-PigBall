package co.edu.eci.pigball.game.java;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Pair<T, U> {
    private T first;
    private U second;

    public Pair(T first, U second) {
        this.first = first;
        this.second = second;
    }
}
