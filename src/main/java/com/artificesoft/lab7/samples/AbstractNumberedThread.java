package com.artificesoft.lab7.samples;

public abstract class AbstractNumberedThread implements Runnable {
    private final int id;

    public AbstractNumberedThread(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
