package com.artificesoft.lab7.samples.kitchen;

import com.artificesoft.lab7.samples.AbstractNumberedThread;

import java.time.Instant;
import java.util.Optional;

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
        while (KitchenSimulator.INSTANCE.remaining.getAcquire() > 0 || KitchenSimulator.INSTANCE.full.getAcquire() > 0) {
            Optional<Meal> res = KitchenSimulator.INSTANCE.takeMeal();
            res.ifPresentOrElse(m -> System.out.println("Thread " + this + " Successfully got meal " + m), () -> {
                // for reasons beyond my comprehension, performing this check with Optional#ifPresent causes a race condition, while using Optional#ifPresentOrElse doesn't?
                // does spawning a very short-lived thread here *really* have that much of an effect???//
                if (lastFailureTime == null) {
                    lastFailureTime = Instant.now();
                }
                Instant now = Instant.now();
                if (now.toEpochMilli() - lastFailureTime.toEpochMilli() >= 5000) {
                    System.out.println("WaiterThread " + getId() + " hasn't acquired anything in a while!");
                    lastFailureTime = now;
                }
            });
        }
    }
}
