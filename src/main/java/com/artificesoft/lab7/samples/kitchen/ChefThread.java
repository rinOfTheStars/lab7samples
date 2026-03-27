package com.artificesoft.lab7.samples.kitchen;

import com.artificesoft.lab7.samples.AbstractNumberedThread;

import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;

public class ChefThread extends AbstractNumberedThread {
    public ChefThread(int id) {
        super(id);
    }

    @Override
    public void run() {
        while (!KitchenSimulator.IS_SET_UP_COMPLETE) {
            continue;
        }
        while (KitchenSimulator.INSTANCE.remaining.getAcquire() > 0) {
            KitchenSimulator.INSTANCE.prodLock.lock();
            int remainingOrders = KitchenSimulator.INSTANCE.remaining.getAndDecrement();
            KitchenSimulator.INSTANCE.prodLock.unlock();
            if (remainingOrders > 0) {
                int r = ThreadLocalRandom.current().nextInt(0, 501);
                try {
                    // simulate some amount of production delay
                    Thread.sleep(r);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                Meal m = new Meal(remainingOrders, Instant.now());
                System.out.println("Thread " + this + " created meal " + m);
                boolean success = false;
                while (!success) {
                    success = KitchenSimulator.INSTANCE.putMeal(m);
                    System.out.println("Thread " + this + " successfully submitted meal " + m + " to queue");
                }
            }
        }
        System.out.println("Thread " + this + " done!");
    }
}
