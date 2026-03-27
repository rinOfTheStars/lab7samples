package com.artificesoft.lab7.samples.parking;

import com.artificesoft.lab7.samples.AbstractNumberedThread;

import java.util.concurrent.ThreadLocalRandom;

public class CarThread extends AbstractNumberedThread {

    public CarThread(int id) {
        super(id);
    }

    @Override
    public void run() {
        boolean parked = false;
        try {
            while (!parked) {
                if (ParkingSimulator.requestSpot()) {
                    parked = true;
                } else {
                    Thread.sleep(250);
                }
            }
            int wait = ThreadLocalRandom.current().nextInt(1000, 10001);
            System.out.println("Thread " + this.getId() + " got spot, sleeping for " + wait + " ms");
            Thread.sleep(wait);
            ParkingSimulator.releaseSpot(this.getId());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
