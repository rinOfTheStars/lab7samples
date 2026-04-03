package com.artificesoft.labseven.samples.readwrite.protocol;

public class SeatData {
    public static final int LOWEST_MINIMUM = 100;
    public static final int HIGHEST_MINIMUM = 300;
    public static final int LOWEST_MAXIMUM = 400;
    public static final int HIGHEST_MAXIMUM = 1000;

    public final int minCost;
    public final int maxCost;
    private int currentCost;
    private boolean purchased = false;

    public SeatData(int minCost, int maxCost) {
        this.minCost = minCost;
        this.maxCost = maxCost;
        currentCost = Math.ceilDivExact((this.minCost + this.maxCost), 2);
    }

    public void updateCost(int newCost) throws IllegalArgumentException, IllegalStateException {
        if (purchased) throw new IllegalStateException("Can't update cost for a seat that is already sold!");
        if (newCost < minCost || newCost > maxCost) {
            throw new IllegalArgumentException("Updated cost of " + newCost +
                    " falls outside bounds " + minCost + " to " + maxCost + " (inclusive)");
        } else {
            currentCost = newCost;
        }
    }

    public int getCurrentCost() {
        return currentCost;
    }

    public void flagAsPurchased() {
        purchased = true;
    }

    public boolean isPurchased() {
        return purchased;
    }

}
