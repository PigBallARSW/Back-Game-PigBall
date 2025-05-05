package co.edu.eci.pigball.game.model;

import java.util.concurrent.atomic.AtomicInteger;

public class Team {
    private AtomicInteger score;
    private AtomicInteger players;

    public Team() {
        score = new AtomicInteger(0);
        players = new AtomicInteger(0);
    }

    public int getScore() {
        return score.get();
    }

    public void increaseScore() {
        score.incrementAndGet();
    }

    public int getPlayers() {
        return players.get();
    }

    public int addPlayer() {
        return players.incrementAndGet();
    }

    public int removePlayer() {
        return players.decrementAndGet();
    }
}
