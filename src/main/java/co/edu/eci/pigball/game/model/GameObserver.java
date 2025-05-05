package co.edu.eci.pigball.game.model;

import java.util.List;

import co.edu.eci.pigball.game.model.entity.impl.Player;

public interface GameObserver {
    void onGoalScored(int team, List<Player> players);
} 