package co.edu.eci.pigball.game.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import co.edu.eci.pigball.game.exception.GameException;
import co.edu.eci.pigball.game.model.temp_change.GameDTO;
import co.edu.eci.pigball.game.service.GameService;

@RestController
public class LobbyController {

    private final GameService gameService;

    public LobbyController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping("/createGame")
    public ResponseEntity<Object> createGame(
            @RequestBody GameDTO game) {
        try {
            return new ResponseEntity<>(gameService.createGame(game), HttpStatus.CREATED);
        } catch (GameException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/getGame/{gameId}")
    public ResponseEntity<Object> getGame(@PathVariable String gameId) {
        try {
            return new ResponseEntity<>(gameService.getGame(gameId), HttpStatus.OK);
        } catch (GameException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/getAllGames")
    public ResponseEntity<Object> getAllGames() {
        return new ResponseEntity<>(gameService.getAllGames(), HttpStatus.OK);
    }

    @DeleteMapping("/removeGame/{gameId}")
    public ResponseEntity<Object> removeGame(@PathVariable String gameId) {
        try {
            gameService.removeGame(gameId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (GameException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

}