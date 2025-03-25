package co.edu.eci.pigball.game.model;

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

public class Team {
    private AtomicInteger score;
    private AtomicInteger players;
    private LinkedList<String> events;
    
    public Team(){
        score = new AtomicInteger(0);
        players = new AtomicInteger(0);
        events = new LinkedList<>();
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

    public void addEvent(String event){
        events.add(event);
    }

    public LinkedList<String> getEvents() {
        return events;
    }
}
