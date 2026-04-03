package com.artificesoft.lab7.samples.kitchen;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;

public class Sealable<E> implements Comparable<Sealable<E>> {
    public final E e;
    @Nullable private Instant timestamp = null;

    public Sealable(E e) {
        this.e = e;
    }

    @Override
    public int compareTo(@NotNull Sealable<E> o) {
        if (this.timestamp == null) return 1; // unsealed instances are "larger, and thus have lower priority"
        else if (o.timestamp == null) return -1;
        else return this.timestamp.compareTo(o.timestamp);
    }

    public boolean isSealed() {
        return this.timestamp != null;
    }

    public void seal() throws IllegalStateException {
        if (this.timestamp != null) throw new IllegalStateException("Can't seal an already sealed Sealable");
        else this.timestamp = Instant.now();
    }
}
