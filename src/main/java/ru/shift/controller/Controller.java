package ru.shift.controller;

import ru.shift.model.CellAction;
import ru.shift.model.GameConfig;
import ru.shift.model.GameModel;
import ru.shift.view.ButtonType;
import ru.shift.view.CellEventListener;
import ru.shift.view.GameType;
import ru.shift.view.GameTypeListener;

public final class Controller implements CellEventListener, GameTypeListener {

    private final GameModel model;
    private GameType currentGameType = GameType.NOVICE;

    public Controller(GameModel model) {
        this.model = model;
    }

    @Override
    public void onMouseClick(int x, int y, ButtonType btn) {
        int r = y;
        int c = x;
        switch (btn) {
            case LEFT_BUTTON   -> model.action(r, c, CellAction.OPEN);
            case RIGHT_BUTTON  -> model.action(r, c, CellAction.FLAG_TOGGLE);
            case MIDDLE_BUTTON -> model.action(r, c, CellAction.CHORD);
        }
    }

    public void onNewGameCreate() {
        GameConfig cfg = configByType(currentGameType);
        model.newGame(cfg);
    }

    @Override
    public void onGameTypeChanged(GameType gameType) {
        currentGameType = gameType;
        GameConfig cfg = configByType(gameType);
        model.newGame(cfg);
    }

    public GameType getCurrentGameType() {
        return currentGameType;
    }

    private GameConfig configByType(GameType t) {
        return switch (t) {
            case NOVICE -> new GameConfig(9, 9, 10);
            case MEDIUM -> new GameConfig(16, 16, 40);
            case EXPERT -> new GameConfig(16, 30, 99);
        };
    }
}
