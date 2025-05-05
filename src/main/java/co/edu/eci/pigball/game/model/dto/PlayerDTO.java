package co.edu.eci.pigball.game.model.dto;

import java.util.Collection;

import co.edu.eci.pigball.game.model.entity.impl.Player;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlayerDTO {

    private String name;
    private String sessionId;
    private Integer team;
    private double x;
    private double y;
    private boolean isKicking;
    private String id;

    public static PlayerDTO toDTO(Player player) {
        PlayerDTO playerDTO = new PlayerDTO();
        playerDTO.setName(player.getName());
        playerDTO.setSessionId(player.getSessionId());
        playerDTO.setTeam(player.getTeam());
        playerDTO.setX(player.getX());
        playerDTO.setY(player.getY());
        playerDTO.setKicking(player.isKicking());
        playerDTO.setId(player.getId());
        return playerDTO;
    }

    public static Collection<PlayerDTO> toDTO(Collection<Player> players) {
        return players.stream().map(PlayerDTO::toDTO).toList();
    }
}
