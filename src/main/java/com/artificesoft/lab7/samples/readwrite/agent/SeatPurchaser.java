package com.artificesoft.lab7.samples.readwrite.agent;

import com.artificesoft.lab7.samples.readwrite.ReadWriteSim;
import com.artificesoft.lab7.samples.readwrite.protocol.SeatSnapshot;
import com.artificesoft.lab7.samples.readwrite.protocol.TransactionResult;
import com.artificesoft.lab7.samples.readwrite.protocol.TransactionState;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public record SeatPurchaser(SeatSnapshot lastSnapshotOfTarget, int money) implements Callable<TransactionResult> {

    @Override
    public TransactionResult call() throws Exception {
        boolean acquiredLock = ReadWriteSim.INSTANCE.lock.writeLock().tryLock(1, TimeUnit.SECONDS);
        if (acquiredLock) {
            SeatSnapshot currentState = ReadWriteSim.INSTANCE.readFromPos(lastSnapshotOfTarget.row(), lastSnapshotOfTarget.column());
            if (!currentState.equals(lastSnapshotOfTarget)) {
                // a state change occurred; release the write lock
                ReadWriteSim.INSTANCE.lock.writeLock().unlock();
                int currentCost = currentState.cost();
                boolean currentlyPurchased = currentState.isPurchased();
                if (currentCost != money) {
                    return new TransactionResult(TransactionState.FAILURE_COST_CHANGED, currentState);
                } else if (currentlyPurchased) {
                    return new TransactionResult(TransactionState.FAILURE_ALREADY_PURCHASED, currentState);
                } else {
                    // a seat got replaced at runtime, something that shouldn't happen
                    throw new AssertionError("An impossible state change occurred for a seat between two snapshots!\n" +
                            "Prev: " + lastSnapshotOfTarget + "\nCurrent: " + currentState);
                }
            } else if (currentState.cost() > money) {
                // we don't have the money lol
                // release the write lock!!
                ReadWriteSim.INSTANCE.lock.writeLock().unlock();
                return new TransactionResult(TransactionState.FAILURE_COSTS_TOO_MUCH, currentState);
            } else {
                // we're in a valid state to actually make the purchase! honestly quite incredible
                SeatSnapshot postPurchaseState = ReadWriteSim.INSTANCE.flagPurchasedAtPos(lastSnapshotOfTarget.row(), lastSnapshotOfTarget.column());
                ReadWriteSim.INSTANCE.lock.writeLock().unlock();
                return new TransactionResult(TransactionState.SUCCESS, postPurchaseState);

            }
        } else {
            // since we never require a new snapshot if the request times out, we have to pass null here
            return new TransactionResult(TransactionState.FAILURE_TIMED_OUT, null);
        }
    }
}
