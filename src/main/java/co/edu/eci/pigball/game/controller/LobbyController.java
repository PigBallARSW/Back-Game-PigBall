package co.edu.eci.pigball.game.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.eci.pigball.game.exception.GameException;
import co.edu.eci.pigball.game.model.DTO.GameDTO;
import co.edu.eci.pigball.game.service.GameService;

@RestController
public class LobbyController {

    @Autowired
    private GameService gameService;

    @PostMapping("/createGame/{gameName}/{creatorName}")
    public ResponseEntity<?> createGame(
            @PathVariable String gameName,
            @PathVariable String creatorName,
            @RequestParam(defaultValue = "4") int maxPlayers,
            @RequestParam(defaultValue = "false") boolean privateGame) {
        try {
            return new ResponseEntity<GameDTO>(gameService.createGame(gameName, creatorName, maxPlayers,privateGame), HttpStatus.CREATED);
        } catch (GameException e) {
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/getGame/{gameId}")
    public ResponseEntity<?> getGame(@PathVariable String gameId) {
        try {
            return new ResponseEntity<GameDTO>(gameService.getGame(gameId), HttpStatus.OK);
        } catch (GameException e) {
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/getAllGames")
    public ResponseEntity<?> getAllGames() {
        return new ResponseEntity<>(gameService.getAllGames(), HttpStatus.OK);
    }

    @DeleteMapping("/removeGame/{gameId}")
    public ResponseEntity<?> removeGame(@PathVariable String gameId) {
        try {
            gameService.removeGame(gameId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (GameException e) {
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

}