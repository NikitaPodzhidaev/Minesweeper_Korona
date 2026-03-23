package ru.shift.model;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public final class MinerFieldExecutor {

    static Cell[][] createEmptyField(GameConfig gameConfig) {
        Cell[][] cellField = new Cell[gameConfig.rows()][gameConfig.cols()];
        for (int r = 0; r < gameConfig.rows(); r++)
            for (int c = 0; c < gameConfig.cols(); c++)
                cellField[r][c] = new Cell();

        return cellField;
    }

    static void placeMinesAvoiding(GameConfig cfg, Cell[][] field, int safeRow, int safeCol) {
        Random rnd = new Random();
        int rows = cfg.rows();
        int cols = cfg.cols();
        int total = rows * cols;
        int mines = cfg.mines();
        int safeId = safeRow * cols + safeCol;

        Set<Integer> used = new HashSet<>();
        while (used.size() < mines) {
            int id = rnd.nextInt(total);
            if (id == safeId) continue;
            if (used.add(id)) {
                int r = id / cols;
                int c = id % cols;
                field[r][c].setMine(true);
            }
        }
    }

    static void recomputeNumbers(GameConfig cfg, Cell[][] field) {
        int rows = cfg.rows(), cols = cfg.cols();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Cell cell = field[r][c];
                if (cell.getIsMine()) {
                    cell.setNeighboursMines(0);
                    continue;
                }
                int cnt = 0;
                for (Direction d : Direction.values()) {
                    int nr = r + d.getDx(), nc = c + d.getDy();
                    if (0 <= nr && nr < rows && 0 <= nc && nc < cols && field[nr][nc].getIsMine()) cnt++;
                }
                cell.setNeighboursMines(cnt);
            }
        }
    }

    static Cell[][] createInitializedFieldAvoiding(GameConfig cfg, int safeRow, int safeCol) {
        Cell[][] cellField = createEmptyField(cfg);
        placeMinesAvoiding(cfg, cellField, safeRow, safeCol);
        recomputeNumbers(cfg, cellField);
        return cellField;
    }




}
