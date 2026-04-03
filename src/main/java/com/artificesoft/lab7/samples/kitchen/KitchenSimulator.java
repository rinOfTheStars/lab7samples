package com.artificesoft.lab7.samples.kitchen;

import java.util.TreeSet;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class KitchenSimulator {

    public static final KitchenSimulator INSTANCE = new KitchenSimulator();
    public static boolean IS_SET_UP_COMPLETE = false;

    private static final int NUMBER_OF_COOKS = 4;
    private static final int NUMBER_OF_WAITERS = 6;
    private static final int TOTAL_ORDERS = 64;
    private static final int ORDER_SLOTS = 12;

    private final ReentrantLock queueLock = new ReentrantLock(true);
    private final TreeSet<Sealable<Object>> queue = new TreeSet<>();

    private final Semaphore full = new Semaphore(ORDER_SLOTS, true);
    private final Semaphore empty = new Semaphore(ORDER_SLOTS, true);

    public final AtomicInteger remainingToMake = new AtomicInteger(TOTAL_ORDERS);
    public final AtomicInteger remainingToServe = new AtomicInteger(TOTAL_ORDERS);


    private KitchenSimulator() {

    }

    static void main() {
        try (ExecutorService e = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < NUMBER_OF_COOKS; i++) {
                ChefThread c = new ChefThread(i);
                e.execute(c);
                System.out.println("Made a chef!");
            }
            for (int i = 0; i < NUMBER_OF_WAITERS; i++) {
                WaiterThread w = new WaiterThread(i);
                e.execute(w);
                System.out.println("Made a waiter!");
            }
            for (int i = 0; i < ORDER_SLOTS; i++) {
                INSTANCE.queue.add(new Sealable<>(new Object()));
                System.out.println("Prepared a slot!");
            }
            System.out.println("Setting up empty semaphore");
            INSTANCE.empty.drainPermits();
            System.out.println("Set up complete!");
            IS_SET_UP_COMPLETE = true;
            while (INSTANCE.remainingToServe.getAcquire() > 0) {
                if (INSTANCE.remainingToServe.getPlain() <= 0) break;
            }
            System.out.println("Done!");
        }
    }

    public boolean plateMeal(int id) throws InterruptedException {
        // "take" a full slot token, "release" an empty slot one
        if (full.tryAcquire(500, TimeUnit.MILLISECONDS)) {
            System.out.println("ChefThread " + id + " starting plate attempt");
            empty.release();
            // actually update an object in the queue
            queueLock.lock();
            Sealable<Object> s = queue.pollLast();
            if (s == null) throw new AssertionError("Queue failure (plating) @ " + id);
            if (s.isSealed()) throw new AssertionError("Seal state error (plating) @ " + id);
            s.seal();
            queue.add(s);
            queueLock.unlock();
            int r = remainingToMake.getAndDecrement();
            System.out.println("Success for ChefThread " + id + ", " + r + " remaining");
            return true;
        } else {
            return false;
        }
    }

    public boolean serveMeal(int id) throws InterruptedException {
        // "take" an empty slot token, "release" a full slot one
        if (empty.tryAcquire(500, TimeUnit.MILLISECONDS)) {
            System.out.println("WaiterThread " + id + " starting serve attempt");
            full.release();
            // actually update an object in the queue
            queueLock.lock();
            Sealable<Object> s = queue.pollFirst();
            if (s == null) throw new AssertionError("Queue failure (serving) @ " + id);
            if (!s.isSealed()) throw new AssertionError("Seal state error (serving) @" + id);
            queue.add(new Sealable<>(new Object()));
            if (queue.size() != ORDER_SLOTS) throw new AssertionError("Queue failure (restocking) @ " + id);
            queueLock.unlock();
            int r = remainingToServe.getAndDecrement();
            System.out.println("Success for WaiterThread " + id + ", " + r + " remaining");
            return true;
        } else {
            return false;
        }
    }
}
