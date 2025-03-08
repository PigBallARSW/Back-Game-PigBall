package co.edu.eci.pigball.game.model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import co.edu.eci.pigball.game.model.DTO.GameDTO;
import lombok.Getter;

@Component
@Getter
public class Game {
    private ConcurrentHashMap<String, Player> players;
    private GameDTO gameDTO;

    private static final int velocity = 5;

    public Game(){
        players = new ConcurrentHashMap<>();
    }

    public void addPlayer(String name, Player player){
        players.put(name, player);
    }

    public List<Player> getAllPlayers(){
        return new ArrayList<>(players.values());
    }

    public void startGame(){
        gameDTO = new GameDTO(players.values());
        for(Player player : players.values() ){
            Thread thread = new Thread(player);
            thread.start();
        }
    }

    public void makeAMove(String name, int dx, int dy){
        gameDTO = new GameDTO(players.values());
        if (!players.containsKey(name)){
            addPlayer(name, new Player(name, 0,0));
        }
        Player player = players.get(name);
        player.moveInX(dx *  Game.velocity);
        player.moveInY(dy * Game.velocity);
    }

}
