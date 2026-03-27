package com.artificesoft.lab7.samples.kitchen;

import org.jetbrains.annotations.NotNull;

import java.time.Instant;

public record Meal(int number, Instant madeAt) implements Comparable<Meal> {
    @Override
    public int compareTo(@NotNull Meal o) {
        return o.madeAt.compareTo(this.madeAt);
    }
}
