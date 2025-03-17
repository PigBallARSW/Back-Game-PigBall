package co.edu.eci.pigball.game.exception;

public class GameException extends Exception {

    private static final long serialVersionUID = 1L;

    public static final String NOT_EMPTY_ID = "Game id cannot be empty";
    public static final String NOT_EMPTY_NAME = "Game name cannot be empty";
    public static final String GAME_NOT_NULL = "Game name cannot be null";
    public static final String GAME_NOT_FOUND = "Game not found";
    public static final String PLAYER_NOT_FOUND = "Player not found";
    public static final String PLAYER_ALREADY_EXISTS = "Player already exists";
    public static final String GAME_ALREADY_EXISTS = "Game already exists";
    public static final String GAME_NOT_STARTED = "Game not started";
    public static final String GAME_ALREADY_STARTED = "Game already started";
    public static final String GAME_ALREADY_FINISHED = "Game already finished";



    public GameException(String message) {
        super(message);
    }
    
}
