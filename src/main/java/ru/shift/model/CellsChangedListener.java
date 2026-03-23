package ru.shift.model;

import java.util.List;

@FunctionalInterface
public interface CellsChangedListener {
    void onCellsChanged(List<CellUpdate> cellUpdateList);
}
