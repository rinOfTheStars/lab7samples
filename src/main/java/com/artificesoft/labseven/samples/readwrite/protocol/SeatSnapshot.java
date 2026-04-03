package com.artificesoft.labseven.samples.readwrite.protocol;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public record SeatSnapshot(int row, int column, int cost, int minCost, int maxCost, boolean isPurchased) {
    @Contract("_, _, _ -> new")
    public static @NotNull SeatSnapshot fromData(int row, int column, @NotNull SeatData data) {
        return new SeatSnapshot(row, column, data.getCurrentCost(), data.minCost, data.maxCost, data.isPurchased());
    }
}
