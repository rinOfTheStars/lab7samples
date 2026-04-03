package com.artificesoft.lab7.samples.readwrite.agent;

import com.artificesoft.lab7.samples.readwrite.ReadWriteSim;
import com.artificesoft.lab7.samples.readwrite.protocol.SeatSnapshot;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public record PriceUpdater(SeatSnapshot lastSnapshotOfTarget, int newCost) implements Callable<SeatSnapshot> {

    @Override
    public @Nullable SeatSnapshot call() throws Exception {
        boolean acquiredLock = ReadWriteSim.INSTANCE.lock.writeLock().tryLock(1, TimeUnit.SECONDS);
        boolean validNewPrice = lastSnapshotOfTarget.minCost() <= newCost
                && lastSnapshotOfTarget.maxCost() >= newCost
                && lastSnapshotOfTarget.cost() != newCost;
        if (acquiredLock && validNewPrice) {
            SeatSnapshot newSnapshot = ReadWriteSim.INSTANCE.updatePriceAtPos(lastSnapshotOfTarget.row(), lastSnapshotOfTarget.column(), newCost);
            ReadWriteSim.INSTANCE.lock.writeLock().unlock();
            return newSnapshot;
        } else {
            return null;
        }
    }
}
