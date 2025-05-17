package co.edu.eci.pigball.game.model.mapper;

import co.edu.eci.pigball.game.model.Game;
import co.edu.eci.pigball.game.model.dto.GameDTO;
import co.edu.eci.pigball.game.model.dto.PlayerDTO;
import co.edu.eci.pigball.game.model.entity.impl.Player;
import co.edu.eci.pigball.game.utility.Pair;
import co.edu.eci.pigball.game.model.dto.BallDTO;
import co.edu.eci.pigball.game.model.dto.BasicGameDTO;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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
                playersDTO, ballDTO, game.getEvents() ,teams, game.getStyle());
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

    /**
     * Restaura el estado de una instancia Game vacía a partir de un DTO plano.
     */
    public static void restoreState(Game game, GameDTO dto) {
        // 1) ID, tiempos y status
        game.setIdForTest(dto.getId());
        // game.setCreationTime y setStartTime no existen públicos: si los necesitas,
        // añade setters o usa reflexión.
        // Supondremos que tienes setters adecuados:
        game.setStatus(dto.getStatus());
        game.setCreationTime(dto.getCreationTime());
        game.setStartTime(dto.getStartTime());

        // 2) Equipos y puntuaciones
        Pair<Integer,Integer> scores = dto.getTeams();
        game.getTeams().getFirst().setScore(new AtomicInteger(scores.getFirst()));
        game.getTeams().getSecond().setScore(new AtomicInteger(scores.getSecond()));

        // 3) Jugadores
        game.getPlayers().clear();
        for (PlayerDTO pd : dto.getPlayers()) {
            Player p = new Player(pd.getName(), pd.getId(),pd.getSessionId(), (int) pd.getX(), (int) pd.getY(), pd.getRadius());
            p.setTeam(pd.getTeam());
            p.setPosition(
                    dto.getBorderX(), dto.getBorderY(),
                    new Pair<>(pd.getX(), pd.getY()),
                    new ArrayList<>()
            );
            game.getPlayers().put(p.getName(), p);
        }

        // 4) Pelota
        BallDTO bd = dto.getBall();
        game.getBall().setPosition(
                dto.getBorderX(), dto.getBorderY(),
                new Pair<>(bd.getX(), bd.getY()),
                new ArrayList<>()
        );
        game.getBall().setVelocity(bd.getVelocityX(), bd.getVelocityY());

        // 5) Eventos
        game.getEvents().clear();
        game.getEvents().addAll(dto.getEvents());
    }
}
