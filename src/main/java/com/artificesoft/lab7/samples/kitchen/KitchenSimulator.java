package com.artificesoft.lab7.samples.kitchen;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
    private final PriorityQueue<Meal> queue = new PriorityQueue<>();

    public final AtomicInteger empty = new AtomicInteger(ORDER_SLOTS);
    public final AtomicInteger full = new AtomicInteger(0);
    public final AtomicInteger remaining = new AtomicInteger(TOTAL_ORDERS);
    public final ReentrantLock prodLock = new ReentrantLock(true);


    private KitchenSimulator() {

    }

    static void main() {
        try (ExecutorService e = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < NUMBER_OF_COOKS; i++) {
                ChefThread c = new ChefThread(i);
                e.execute(c);
            }
            for (int i = 0; i < NUMBER_OF_WAITERS; i++) {
                WaiterThread w = new WaiterThread(i);
                e.execute(w);
            }
            IS_SET_UP_COMPLETE = true;
        }
    }
    public synchronized Optional<Meal> takeMeal() {
        if (queueLock.isLocked()) {
            return Optional.empty();
        } else if (full.get() == 0) {
            return Optional.empty();
        } else {
            queueLock.lock();
            Meal m = queue.poll();
            if (m == null) throw new AssertionError("A desynchronization occurred between full slots counter and the actual number of full slots");
            full.getAndDecrement();
            empty.getAndIncrement();
            queueLock.unlock();
            return Optional.of(m);
        }
    }

    public synchronized boolean putMeal(Meal m) {
        if (queueLock.isLocked()) {
            return false;
        } else if (empty.get() == 0) {
            return false;
        } else {
            queueLock.lock();
            queue.add(m);
            if (queue.size() > ORDER_SLOTS) throw new AssertionError("A desynchronization occurred between empty slots counter and the actual number of empty slots");
            full.getAndIncrement();
            empty.getAndDecrement();
            queueLock.unlock();
            return true;
        }
    }
}
