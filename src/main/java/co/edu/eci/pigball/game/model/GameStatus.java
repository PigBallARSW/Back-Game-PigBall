package co.edu.eci.pigball.game.model;

public enum GameStatus {
    WAITING_FOR_PLAYERS, // Esperando jugadores
    WAITING_FULL, // Esperando a empezar con el juego lleno
    STARTING, // Juego en proceso de inicio
    IN_PROGRESS, // Juego en curso
    IN_PROGRESS_FULL, // Juego en curso lleno
    PAUSED, // Juego en pausa
    FINISHED, // Juego terminado
    ABANDONED; // Juego abandonado

    @Override
    public String toString() {
        // Convierte el nombre del enum en un formato más legible
        return name().replace("_", " ").toLowerCase();
    }
}
