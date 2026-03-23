package ru.shift.model;

public interface GameModel {
    void newGame(GameConfig gameConfig);
    void action(int row, int column, CellAction cellAction);

    void addGameStateListener(GameStateListener gameStateListener);

    void addCellsChangedListener(CellsChangedListener cellsChangedListener);

    void addNewGameListener(NewGameListener newGameListener);

}