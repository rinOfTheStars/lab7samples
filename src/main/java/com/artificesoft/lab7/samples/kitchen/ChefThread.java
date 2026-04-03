package com.artificesoft.lab7.samples.kitchen;

import com.artificesoft.lab7.samples.AbstractNumberedThread;

import java.time.Instant;

public class ChefThread extends AbstractNumberedThread {
    public ChefThread(int id) {
        super(id);
    }
    private Instant lastFailureTime = null;

    @Override
    public void run() {
        while (!KitchenSimulator.IS_SET_UP_COMPLETE) {
            continue;
        }
        while (KitchenSimulator.INSTANCE.remainingToMake.getAcquire() > 0) {
            try {
                boolean success = KitchenSimulator.INSTANCE.plateMeal(getId());
                if (!success) {
                    Instant now = Instant.now();
                    if (lastFailureTime == null) {
                        lastFailureTime = now;
                    } else {
                        if (KitchenSimulator.INSTANCE.remainingToMake.getPlain() == 0) {
                            System.out.println("No work for ChefThread " + getId() + " to do but didn't stop properly, stopping now instead...");
                            break;
                        }
                        Instant durationCheck = lastFailureTime.plusMillis(5000);
                        if (now.compareTo(durationCheck) >= 0) {
                            System.err.println("An unusually long time has passed since ChefThread " + getId() + " has successfully plated a meal. Something is probably wrong!");
                            lastFailureTime = now;
                        }
                    }
                } else {
                    Thread.sleep(500);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
