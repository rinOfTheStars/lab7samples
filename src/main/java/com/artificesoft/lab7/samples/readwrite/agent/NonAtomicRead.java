package com.artificesoft.lab7.samples.readwrite.agent;

import com.artificesoft.lab7.samples.readwrite.ComparisonPredicate;
import com.artificesoft.lab7.samples.readwrite.ReadWriteSim;
import com.artificesoft.lab7.samples.readwrite.protocol.SeatSnapshot;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public record NonAtomicRead(ComparisonPredicate<SeatSnapshot> predicate) implements Callable<SeatSnapshot> {

    @Override
    public @Nullable SeatSnapshot call() throws Exception {
        boolean gotReadLock = ReadWriteSim.INSTANCE.lock.readLock().tryLock(1, TimeUnit.SECONDS);
        if (gotReadLock) {
            SeatSnapshot bestFit = null;
            for (int row = 0; row < ReadWriteSim.ROWS; row++) {
                for (int column = 0; column < ReadWriteSim.COLUMNS; column++) {
                    SeatSnapshot current = ReadWriteSim.INSTANCE.readFromPos(row, column);
                    if (bestFit == null) {
                        bestFit = current;
                    } else {
                        bestFit = predicate.compare(bestFit, current);
                    }
                }
            }
            ReadWriteSim.INSTANCE.lock.readLock().unlock();
            return bestFit;
        } else return null;
    }
}
