package com.artificesoft.labseven.samples.kitchen;

import com.artificesoft.labseven.samples.AbstractNumberedThread;

import java.time.Instant;

public class WaiterThread extends AbstractNumberedThread {
    public WaiterThread(int id) {
        super(id);
    }

    private Instant lastFailureTime = null;

    @Override
    public void run() {
        while (!KitchenSimulator.IS_SET_UP_COMPLETE) {
            continue;
        }
        while (KitchenSimulator.INSTANCE.remainingToServe.getAcquire() > 0) {
            boolean success = false;
            try {
                success = KitchenSimulator.INSTANCE.serveMeal(getId());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if (!success) {
                Instant now = Instant.now();
                if (lastFailureTime == null) {
                    lastFailureTime = now;
                } else {
                    if (KitchenSimulator.INSTANCE.remainingToServe.getPlain() == 0) {
                        System.out.println("No work for WaiterThread " + getId() + " to do but didn't stop properly, stopping now instead...");
                        break;
                    }
                    Instant durationCheck = lastFailureTime.plusMillis(10000);
                    if (now.compareTo(durationCheck) >= 0) {
                        System.err.println("An unusually long time has passed since WaiterThread " + getId()
                                + " has successfully served a meal. Something is probably wrong!" +
                                " Supposedly, " + KitchenSimulator.INSTANCE.remainingToServe.getPlain() + " serving jobs remain?");
                        lastFailureTime = now;
                    }
                }
            } else {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

        }
    }
}
