package com.artificesoft.labseven.samples.parking;

import java.util.concurrent.*;

public class ParkingSimulator {
    private static final int PARKING_SPOTS = 8;
    private static final int NUMBER_OF_CARS = 24;
    private static final Semaphore SEMAPHORE = new Semaphore(PARKING_SPOTS, true);

    public static synchronized boolean requestSpot() throws InterruptedException {
        if (SEMAPHORE.availablePermits() > 0) {
            System.out.println("Lending a spot with " + (SEMAPHORE.availablePermits() - 1) + " remaining after operation...");
            return SEMAPHORE.tryAcquire(250, TimeUnit.MILLISECONDS);
        } else return false;
    }

    public static synchronized void releaseSpot(int id) {
        System.out.println("Car " + id + " releasing spot, " + (SEMAPHORE.availablePermits() + 1) + " after operation!!!");
        SEMAPHORE.release();
    }

    static void main() {
        System.out.println("Starting simulation with " + PARKING_SPOTS + " spots and " + NUMBER_OF_CARS + " car sim threads");
        try (ExecutorService service = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < NUMBER_OF_CARS; i++) {
                System.out.println("Submitting new car thread with id " + i);
                service.submit(new CarThread(i));
            }
        }
    }
}
