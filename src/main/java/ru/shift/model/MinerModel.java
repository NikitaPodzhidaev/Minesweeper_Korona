package ru.shift.model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

public final class MinerModel implements GameModel {

    private GameConfig gameConfig;
    private GameState gameState = GameState.INIT;

    private Cell[][] cellField;
    private int flags;
    private int opened;

    private final List<GameStateListener> gameStateListeners = new ArrayList<>();
    private final List<CellsChangedListener> cellsChangedListeners = new ArrayList<>();
    private final List<NewGameListener> newGameListeners = new ArrayList<>();

    @Override
    public void newGame(GameConfig cfg) {
        this.gameConfig = cfg;
        this.cellField = MinerFieldExecutor.createEmptyField(cfg);
        this.flags = 0;
        this.opened = 0;

        notifyNewGame(cfg);
        notifyGameStateChanged(GameState.INIT);
    }

    @Override
    public void action(int row, int col, CellAction action) {
        if (!inField(row, col)) return;
        if (gameState == GameState.WON || gameState == GameState.LOST) return;

        if (gameState == GameState.INIT) {
            if (action == CellAction.OPEN) {
                cellField = MinerFieldExecutor.createInitializedFieldAvoiding(gameConfig, row, col);
                notifyGameStateChanged(GameState.PLAY);
            } else if (action == CellAction.FLAG_TOGGLE) {
                toggleFlag(row, col);
                return;
            } else {
                return;
            }
        }

        switch (action) {
            case OPEN        -> open(row, col);
            case FLAG_TOGGLE -> toggleFlag(row, col);
            case CHORD       -> chord(row, col);
        }
    }

    @Override
    public void addGameStateListener(GameStateListener l) {
        gameStateListeners.add(l);
    }

    @Override
    public void addCellsChangedListener(CellsChangedListener l) {
        cellsChangedListeners.add(l);
    }

    @Override
    public void addNewGameListener(NewGameListener l) {
        newGameListeners.add(l);
    }

    private void open(int row, int col) {
        Cell cell = cellField[row][col];
        if (cell.getState() != CellState.CLOSED) return;

        if (cell.getIsMine()) {
            cell.setState(CellState.OPEN);
            List<CellUpdate> ups = revealAllMinesAsUpdates(row, col);
            notifyCellsChanged(ups);
            notifyGameStateChanged(GameState.LOST);
            return;
        }

        if (cell.getNeighboursMines() > 0) {
            cell.setState(CellState.OPEN);
            opened++;
            notifyCellsChanged(List.of(makeUpdate(row, col)));
            tryWin();
            return;
        }

        List<CellUpdate> ups = openCollectCells(row, col);
        if (!ups.isEmpty()) notifyCellsChanged(ups);
        tryWin();
    }

    private void toggleFlag(int r, int c) {
        Cell cell = cellField[r][c];
        if (cell.getState() == CellState.OPEN) return;

        if (cell.getState() == CellState.CLOSED) {
            if (flags >= gameConfig.mines()) return;
            cell.setState(CellState.FLAGGED);
            flags++;
        } else if (cell.getState() == CellState.FLAGGED) {
            cell.setState(CellState.CLOSED);
            if (flags > 0) flags--;
        }

        notifyCellsChanged(List.of(makeUpdate(r, c)));
    }

    private void chord(int r, int c) {
        Cell center = cellField[r][c];
        if (center.getState() != CellState.OPEN || center.getNeighboursMines() == 0) return;

        int flagged = 0;
        for (Direction d : Direction.values()) {
            int nr = r + d.getDx(), nc = c + d.getDy();
            if (inField(nr, nc) && cellField[nr][nc].getState() == CellState.FLAGGED) flagged++;
        }
        if (flagged != center.getNeighboursMines()) return;

        for (Direction d : Direction.values()) {
            int nr = r + d.getDx(), nc = c + d.getDy();
            if (!inField(nr, nc)) continue;
            Cell ncCell = cellField[nr][nc];
            if (ncCell.getState() == CellState.CLOSED && ncCell.getIsMine()) {
                ncCell.setState(CellState.OPEN);
                List<CellUpdate> ups = revealAllMinesAsUpdates(nr, nc);
                notifyCellsChanged(ups);
                notifyGameStateChanged(GameState.LOST);
                return;
            }
        }

        for (Direction d : Direction.values()) {
            int nr = r + d.getDx(), nc = c + d.getDy();
            if (inField(nr, nc) && cellField[nr][nc].getState() == CellState.CLOSED) open(nr, nc);
        }
    }

    private boolean inField(int r, int c) {
        return r >= 0 && r < gameConfig.rows() && c >= 0 && c < gameConfig.cols();
    }

    private List<CellUpdate> openCollectCells(int startR, int startC) {
        List<CellUpdate> openedNow = new ArrayList<>();
        ArrayDeque<int[]> dq = new ArrayDeque<>();
        dq.add(new int[]{startR, startC});

        while (!dq.isEmpty()) {
            int[] pair = dq.removeFirst();
            int r = pair[0], c = pair[1];

            Cell cc = cellField[r][c];
            if (cc.getState() != CellState.CLOSED || cc.getIsMine()) continue;

            cc.setState(CellState.OPEN);
            opened++;
            openedNow.add(makeUpdate(r, c));

            if (cc.getNeighboursMines() == 0) {
                for (Direction d : Direction.values()) {
                    int nr = r + d.getDx(), nc = c + d.getDy();
                    if (inField(nr, nc) && cellField[nr][nc].getState() == CellState.CLOSED) {
                        dq.addLast(new int[]{nr, nc});
                    }
                }
            }
        }
        return openedNow;
    }

    private List<CellUpdate> revealAllMinesAsUpdates(int explodeR, int explodeC) {
        List<CellUpdate> ups = new ArrayList<>();
        ups.add(makeUpdate(explodeR, explodeC));
        for (int r = 0; r < gameConfig.rows(); r++) {
            for (int c = 0; c < gameConfig.cols(); c++) {
                if (r == explodeR && c == explodeC) continue;
                Cell cell = cellField[r][c];
                if (cell.getIsMine() && cell.getState() != CellState.OPEN) {
                    cell.setState(CellState.OPEN);
                    ups.add(makeUpdate(r, c));
                }
            }
        }
        return ups;
    }

    private void tryWin() {
        int nonMines = gameConfig.rows() * gameConfig.cols() - gameConfig.mines();
        if (opened == nonMines) {
            notifyGameStateChanged(GameState.WON);
        }
    }

    private CellUpdate makeUpdate(int r, int c) {
        Cell cell = cellField[r][c];
        return new CellUpdate(r, c, cell.getState(), cell.getIsMine(), cell.getNeighboursMines());
    }

    private void notifyGameStateChanged(GameState s) {
        gameState = s;
        for (var l : gameStateListeners) l.onGameStateChanged(s);
    }

    private void notifyCellsChanged(List<CellUpdate> updates) {
        if (updates == null || updates.isEmpty()) return;
        for (var l : cellsChangedListeners) l.onCellsChanged(updates);
    }

    private void notifyNewGame(GameConfig cfg) {
        for (var l : newGameListeners) l.onNewGame(cfg);
    }
}
