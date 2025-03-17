package co.edu.eci.pigball.game.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.eci.pigball.game.exception.GameException;
import co.edu.eci.pigball.game.model.DTO.GameDTO;
import co.edu.eci.pigball.game.service.GameService;

@RestController
public class LobbyController {
    
    @Autowired
    private GameService gameService;

    @PostMapping("/createGame/{gameName}")
    public ResponseEntity<?> createGame(@PathVariable String gameName) {
        try {
            return new ResponseEntity<GameDTO>(gameService.createGame(gameName), HttpStatus.CREATED);
        } catch (GameException e) {
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    
}
