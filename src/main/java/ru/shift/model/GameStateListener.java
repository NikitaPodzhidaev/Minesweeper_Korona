package ru.shift.model;

@FunctionalInterface
public interface GameStateListener {
    void onGameStateChanged(GameState gameState);
}
