package com.artificesoft.lab7.samples.readwrite;

import com.artificesoft.lab7.samples.readwrite.agent.Agent;
import com.artificesoft.lab7.samples.readwrite.agent.PriceUpdater;
import com.artificesoft.lab7.samples.readwrite.protocol.SeatData;
import com.artificesoft.lab7.samples.readwrite.protocol.SeatSnapshot;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ReadWriteSim {
    public static final ReadWriteSim INSTANCE = new ReadWriteSim();
    public static final int ROWS = 5;
    public static final int COLUMNS = 10;
    public final ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);
    private final SeatData[][] seating = new SeatData[ROWS][COLUMNS];
    public final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    public SeatSnapshot readFromPos(int row, int column) {
        SeatData data = seating[row][column];
        return SeatSnapshot.fromData(row, column, data);
    }

    public SeatSnapshot updatePriceAtPos(int row, int column, int newPrice) {
        SeatData data = seating[row][column];
        data.updateCost(newPrice);
        return SeatSnapshot.fromData(row, column, data);
    }

    public SeatSnapshot flagPurchasedAtPos(int row, int column) {
        SeatData data = seating[row][column];
        data.flagAsPurchased();
        return SeatSnapshot.fromData(row, column, data);
    }

    static void main() throws ExecutionException, InterruptedException {
        // Populate seating data
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLUMNS; j++) {
                int min = ThreadLocalRandom.current().nextInt(SeatData.LOWEST_MINIMUM, SeatData.HIGHEST_MINIMUM + 1);
                int max = ThreadLocalRandom.current().nextInt(SeatData.LOWEST_MAXIMUM, SeatData.HIGHEST_MAXIMUM + 1);
                INSTANCE.seating[i][j] = new SeatData(min, max);
            }
        }
        // create "progress map" to store agent state
        // async writing here should be safe since each agent thread only ever writes to its own entry,
        //  but for safety we're using a concurrent map here anyway
        ConcurrentMap<Integer, Boolean> progressMap = new ConcurrentHashMap<>();
        // spawn 30 agents
        for (int i = 0; i < 30; i++) {
            INSTANCE.executor.submit(new Agent(progressMap));
        }
        Instant then = Instant.now();
        while (!progressMap.isEmpty()) {
            if (progressMap.containsValue(false)) {
                // find false kv-pair(s) and prune them, since that thread is finished
                for (Map.Entry<Integer, Boolean> entry : progressMap.entrySet()) {
                    if (entry.getValue() == false) {
                        progressMap.remove(entry.getKey());
                    }
                }
            }
            Instant now = Instant.now();
            if (now.minus(250, ChronoUnit.MICROS).isAfter(then)) {
                // spawn price updater thread
                SeatSnapshot snapshot = null;
                while (snapshot == null) {
                    int r = ThreadLocalRandom.current().nextInt(0, ROWS);
                    int c = ThreadLocalRandom.current().nextInt(0, COLUMNS);
                    SeatSnapshot s = INSTANCE.readFromPos(r, c);
                    if (!s.isPurchased()) {
                        snapshot = s;
                    }
                }
                int newCost = ThreadLocalRandom.current().nextInt(snapshot.minCost(), snapshot.maxCost());
                Future<?> future = INSTANCE.executor.submit(new PriceUpdater(snapshot, newCost));
                Object collectedFuture = future.get();
                if (collectedFuture instanceof SeatSnapshot snap) {
                    System.out.println("Price update occurred: " + snapshot + " -> " + snap);
                } else throw new AssertionError("Bad price updater future read");
            }
        }
    }
}
