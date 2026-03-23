package ru.shift.model;

final class Cell {
    private boolean isMine;
    private int neighboursMines;
    private CellState state = CellState.CLOSED;

    public void setMine(boolean mine) {
        isMine = mine;
    }

    public void setNeighboursMines(int neighboursMines) {
        this.neighboursMines = neighboursMines;
    }

    public void setState(CellState state) {
        this.state = state;
    }

    public CellState getState() {
        return state;
    }
    public int getNeighboursMines() {
        return neighboursMines;
    }
    public boolean getIsMine(){
        return isMine;
    }
}
