package com.artificesoft.lab7.samples.readwrite.protocol;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record TransactionResult(@NotNull TransactionState state, @Nullable SeatSnapshot snapshot) {
}