package ru.shift.model;

public record CellUpdate(int row, int column, CellState cellState, boolean isMine, int neighbourMines) {
}
