package com.artificesoft.lab7.samples.readwrite.agent;

import com.artificesoft.lab7.samples.readwrite.ComparisonPredicate;
import com.artificesoft.lab7.samples.readwrite.ReadWriteSim;
import com.artificesoft.lab7.samples.readwrite.protocol.AgentTrait;
import com.artificesoft.lab7.samples.readwrite.protocol.SeatSnapshot;
import com.artificesoft.lab7.samples.readwrite.protocol.TransactionResult;
import com.artificesoft.lab7.samples.readwrite.protocol.TransactionState;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class Agent implements Runnable {

    private static final AtomicInteger ID_COUNTER = new AtomicInteger();
    private static final ReentrantLock ID_LOCK = new ReentrantLock();
    private static final int MINIMUM_BUDGET = 500;
    private static final int MAXIMUM_BUDGET = 1000;
    private boolean isActive = true;
    private final Set<AgentTrait> traits = new HashSet<>();
    private final int budget;
    private final int id;
    private final ComparisonPredicate<SeatSnapshot> predicate;
    private final ConcurrentMap<Integer, Boolean> progressMap;

    public Agent(ConcurrentMap<Integer, Boolean> progressMap) {
        budget = ThreadLocalRandom.current().nextInt(MINIMUM_BUDGET, MAXIMUM_BUDGET + 1);
        ID_LOCK.lock();
        id = ID_COUNTER.getAndIncrement();
        ID_LOCK.unlock();
        setUpTraits();
        predicate = derivePredicate();
        this.progressMap = progressMap;
        System.out.println("Created an agent thread w/ id " + id + ", budget" + budget +  ", and trait set " + traits);
        progressMap.put(id, true);
    }

    @Override
    public void run() {
        while (isActive) {
            // spawn a reading thread to find the best match out of currently available seats
            Future<?> future = ReadWriteSim.INSTANCE.executor.submit(new NonAtomicRead(predicate));
            try {
                Object collectedFuture = future.get();
                if (collectedFuture instanceof SeatSnapshot snap) {
                    // try to purchase that seat if we can afford it
                    isActive = evaluateAndAttemptPurchase(snap);
                } else {
                    throw new AssertionError("Bad reader future read");
                }
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private boolean evaluateAndAttemptPurchase(SeatSnapshot snap) throws ExecutionException, InterruptedException {
        System.out.println("Agent " + id + " found best match " + snap);
        if (snap.cost() <= budget) {
            Future<?> purchaseFuture = ReadWriteSim.INSTANCE.executor.submit(new SeatPurchaser(snap, snap.cost()));
            Object collectedPurchaseFuture = purchaseFuture.get();
            if (collectedPurchaseFuture instanceof TransactionResult(TransactionState state, SeatSnapshot snapshot)) {
                if (state == TransactionState.SUCCESS) {
                    System.out.println("Agent " + id + " successfully purchased " + snapshot);
                    progressMap.replace(id, false);
                    return false;
                } else {
                    System.out.println("Agent " + id + " failed to purchase " + snapshot + "; reason: " + state);
                    return true;
                }
            } else {
                throw new AssertionError("Bad purchaser future read");
            }
        } else {
            System.out.println("Agent " + id + " can't afford its best match! It will not be going to the movie!");
            progressMap.replace(id, false);
            return false;
        }
    }

    private void setUpTraits() {
        int firstTrait = ThreadLocalRandom.current().nextInt(0, 4);
        traits.add(AgentTrait.values()[firstTrait]);
        int decideSecondTrait = ThreadLocalRandom.current().nextInt(0, 5);
        if (decideSecondTrait == 0) {
            if (traits.contains(AgentTrait.AVOIDS_FRONT_ROW_SEATS) || traits.contains(AgentTrait.AVOIDS_BACK_ROW_SEATS)) {
                boolean secondTrait = ThreadLocalRandom.current().nextBoolean();
                if (secondTrait) {
                    traits.add(AgentTrait.FRUGAL);
                } else {
                    traits.add(AgentTrait.SUPERFAN);
                }
            } else if (traits.contains(AgentTrait.FRUGAL) || traits.contains(AgentTrait.SUPERFAN)) {
                boolean secondTrait = ThreadLocalRandom.current().nextBoolean();
                if (secondTrait) {
                    traits.add(AgentTrait.AVOIDS_FRONT_ROW_SEATS);
                } else {
                    traits.add(AgentTrait.AVOIDS_BACK_ROW_SEATS);
                }
            }

        }
    }

    private ComparisonPredicate<SeatSnapshot> derivePredicate() {
        Iterator<AgentTrait> traitIterator = traits.iterator();
        ComparisonPredicate<SeatSnapshot> fullPredicate = null;
        while (traitIterator.hasNext()) {
            AgentTrait trait = traitIterator.next();
            ComparisonPredicate<SeatSnapshot> predicate;
            switch (trait) {
                case FRUGAL -> predicate = ComparisonPredicates.FRUGAL;
                case SUPERFAN -> predicate = ComparisonPredicates.SUPERFAN;
                case AVOIDS_FRONT_ROW_SEATS -> predicate = ComparisonPredicates.NO_FRONT_ROWS;
                case AVOIDS_BACK_ROW_SEATS -> predicate = ComparisonPredicates.NO_BACK_ROWS;
                case null, default -> throw new AssertionError("This state should be impossible!!!");
            }
            if (fullPredicate == null) {
                fullPredicate = predicate;
            } else {
                fullPredicate = ComparisonPredicates.and(fullPredicate, predicate);
            }
        }
        return fullPredicate;
    }
}
