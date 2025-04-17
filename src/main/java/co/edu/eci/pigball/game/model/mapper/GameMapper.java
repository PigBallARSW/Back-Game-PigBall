package co.edu.eci.pigball.game.model.mapper;

import co.edu.eci.pigball.game.java.Pair;
import co.edu.eci.pigball.game.model.Game;
import co.edu.eci.pigball.game.model.dto.GameDTO;
import co.edu.eci.pigball.game.model.dto.PlayerDTO;
import co.edu.eci.pigball.game.model.dto.BallDTO;
import co.edu.eci.pigball.game.model.dto.BasicGameDTO;

import java.util.Collection;
import java.util.List;

public class GameMapper {

    private GameMapper(){
        // Hiding explicit Constructor
    }
    public static GameDTO toDTO(Game game) {
        List<PlayerDTO> playersDTO = game.getPlayers().values().stream()
                .map(PlayerDTO::toDTO).toList();
        BallDTO ballDTO = new BallDTO(game.getBall().getX(), game.getBall().getY(), game.getBall().getVelocityX(), game.getBall().getVelocityY());
        Pair<Integer, Integer> teams = new Pair<>(game.getTeams().getFirst().getScore(), game.getTeams().getSecond().getScore());           
        return new GameDTO(game.getGameId(), game.getGameName(), 
                game.getCreatorName(), game.getMaxPlayers(), game.getStatus(),
                game.isPrivateGame(), game.getCreationTime(), game.getStartTime(), game.getBorderX(), game.getBorderY(),
                playersDTO, ballDTO, game.getEvents() ,teams );
    }

    public static Collection<GameDTO> toDTO(Collection<Game> games) {
        return games.stream()
                .map(GameMapper::toDTO).toList();
    }

    public static BasicGameDTO toBasicDTO(Game game) {
        List<PlayerDTO> playersDTO = game.getPlayers().values().stream()
                .map(PlayerDTO::toDTO).toList();
        BallDTO ballDTO = new BallDTO(game.getBall().getX(), game.getBall().getY(), game.getBall().getVelocityX(), game.getBall().getVelocityY());
        return new BasicGameDTO(game.getGameId(), game.getGameName(), playersDTO, ballDTO);
    }
}
